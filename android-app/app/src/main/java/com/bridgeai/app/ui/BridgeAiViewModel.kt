package com.bridgeai.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bridgeai.app.data.BridgeItem
import com.bridgeai.app.data.ComponentMedia
import com.bridgeai.app.data.ComponentStatus
import com.bridgeai.app.data.ComponentTask
import com.bridgeai.app.data.MediaType
import com.bridgeai.app.data.NamedCount
import com.bridgeai.app.data.ProjectItem
import com.bridgeai.app.data.ProjectMember
import com.bridgeai.app.data.ReportLevelStats
import com.bridgeai.app.data.ReportItem
import com.bridgeai.app.data.ReportPreviewData
import com.bridgeai.app.data.UserProfile
import kotlin.random.Random

class BridgeAiViewModel : ViewModel() {

    var currentUser by mutableStateOf(
        UserProfile(
            id = 1L,
            realName = "张三",
            userCode = "2024001",
            company = "XX交通养护公司",
            role = "inspector",
        )
    )
        private set

    var activeRole by mutableStateOf<AppRole?>(null)
        private set

    var offlineMode by mutableStateOf(true)
        private set

    var isSyncing by mutableStateOf(false)
        private set

    var syncProgress by mutableStateOf(0f)
        private set

    var networkProfile by mutableStateOf(SyncNetworkProfile.Offline)
        private set

    var autoSyncEnabled by mutableStateOf(true)
        private set

    var mediaSyncOnWifiOnly by mutableStateOf(true)
        private set

    var pauseAutoSyncDuringWork by mutableStateOf(true)
        private set

    var workInProgress by mutableStateOf(false)
        private set

    var lastSyncTrigger by mutableStateOf<SyncTrigger?>(null)
        private set

    var lastSyncNotice by mutableStateOf<SyncNotice?>(null)
        private set

    private var currentSyncSnapshot: SyncRunSnapshot? by mutableStateOf(null)

    var bridges by mutableStateOf(sampleBridges())
        private set

    var projects by mutableStateOf(sampleProjects())
        private set

    var projectMembers by mutableStateOf(sampleProjectMembers())
        private set

    var components by mutableStateOf(seedInitialComponentSyncStates(sampleComponents()))
        private set

    var reports by mutableStateOf(sampleReports())
        private set

    var reportSyncStates by mutableStateOf(seedInitialReportSyncStates(sampleReports()))
        private set

    fun toggleOfflineMode() {
        updateNetworkProfile(
            if (networkProfile == SyncNetworkProfile.Offline) {
                SyncNetworkProfile.Wifi
            } else {
                SyncNetworkProfile.Offline
            },
        )
    }

    fun updateNetworkProfile(profile: SyncNetworkProfile) {
        networkProfile = profile
        offlineMode = profile == SyncNetworkProfile.Offline
    }

    fun updateAutoSyncEnabled(enabled: Boolean) {
        autoSyncEnabled = enabled
    }

    fun updateMediaSyncOnWifiOnly(enabled: Boolean) {
        mediaSyncOnWifiOnly = enabled
    }

    fun updatePauseAutoSyncDuringWork(enabled: Boolean) {
        pauseAutoSyncDuringWork = enabled
    }

    fun updateWorkInProgress(enabled: Boolean) {
        workInProgress = enabled
    }

    fun selectRole(role: AppRole) {
        activeRole = role
        currentUser = when (role) {
            AppRole.Leader -> UserProfile(
                id = 3L,
                realName = "王工",
                userCode = "L-2026001",
                company = "XX交通养护公司",
                role = "leader",
            )
            AppRole.Member -> UserProfile(
                id = 2L,
                realName = "李工",
                userCode = "I-2026018",
                company = "XX交通养护公司",
                role = "inspector",
            )
        }
    }

    fun resetRoleSelection() {
        activeRole = null
    }

    fun dismissSyncNotice() {
        lastSyncNotice = null
    }

    fun getBridge(bridgeId: Long): BridgeItem? = bridges.firstOrNull { it.id == bridgeId }

    fun getProject(projectId: Long): ProjectItem? = projects.firstOrNull { it.id == projectId }

    fun getProjectMembers(projectId: Long): List<ProjectMember> =
        projectMembers[projectId].orEmpty()

    fun getProjectComponents(projectId: Long): List<ComponentTask> =
        components
            .filter { it.projectId == projectId }
            .sortedWith(
                compareByDescending<ComponentTask> { it.responsibleUserId == currentUser.id }
                    .thenByDescending { it.status == ComponentStatus.Pending }
                    .thenBy { it.category }
                    .thenBy { it.number },
            )

    fun getComponent(componentId: Long): ComponentTask? = components.firstOrNull { it.id == componentId }

    fun getReport(reportId: Long): ReportItem? = reports.firstOrNull { it.id == reportId }

    fun projectProgress(projectId: Long): ProjectProgress {
        val projectComponents = getProjectComponents(projectId)
        val total = projectComponents.size
        val collected = projectComponents.count { it.status.isCollectedOrLater() }
        val aiDone = projectComponents.count { it.status.isAiDoneOrLater() }
        val completed = projectComponents.count { it.status == ComponentStatus.Completed }
        return ProjectProgress(total, collected, aiDone, completed)
    }

    fun pendingSyncComponents(): List<ComponentTask> =
        components.filter { it.mediaItems.isNotEmpty() && it.syncStatus != "synced" }

    fun pendingSyncReports(): List<ReportItem> =
        reports.filter { reportSyncStateOf(it) != "synced" }

    fun syncQueueEntries(): List<SyncQueueEntry> {
        val componentEntries = pendingSyncComponents().map { component ->
            val deferredByPolicy =
                mediaSyncOnWifiOnly &&
                    networkProfile == SyncNetworkProfile.Mobile &&
                    component.syncStatus == "pending"
            SyncQueueEntry(
                key = "component-${component.id}",
                title = "${component.type} · ${component.number}",
                subtitle = "状态：${component.status.toDisplayText()} · 待上传 ${component.mediaItems.size} 个素材",
                category = "构件素材",
                state = when {
                    component.syncStatus == "failed" -> SyncEntryState.Failed
                    component.syncStatus == "syncing" -> SyncEntryState.Syncing
                    deferredByPolicy -> SyncEntryState.WaitingWifi
                    else -> SyncEntryState.Pending
                },
            )
        }
        val reportEntries = pendingSyncReports().map { report ->
            val state = reportSyncEntryState(report)
            SyncQueueEntry(
                key = "report-${report.id}",
                title = report.reportTitle,
                subtitle = "状态：${report.reportStatus.toReportStatusText()} · 待同步报告内容",
                category = "报告内容",
                state = state,
            )
        }
        return (componentEntries + reportEntries)
            .sortedWith(
                compareBy<SyncQueueEntry> { it.state.priority }
                    .thenBy { it.category }
                    .thenBy { it.title },
            )
    }

    fun syncQueueCount(): Int = syncQueueEntries().size

    fun failedSyncCount(): Int = syncQueueEntries().count { it.state == SyncEntryState.Failed }

    fun waitingWifiCount(): Int = syncQueueEntries().count { it.state == SyncEntryState.WaitingWifi }

    fun canAutoSyncNow(): Boolean =
        autoSyncEnabled &&
            networkProfile != SyncNetworkProfile.Offline &&
            !(pauseAutoSyncDuringWork && workInProgress) &&
            !isSyncing &&
            syncQueueEntries().any { entry ->
                entry.state == SyncEntryState.Pending ||
                    entry.state == SyncEntryState.Syncing
            }

    fun autoSyncStatusText(): String = when {
        isSyncing && lastSyncTrigger == SyncTrigger.Auto -> "自动同步执行中，请稍候。"
        isSyncing && lastSyncTrigger == SyncTrigger.Manual -> "手动同步执行中，请稍候。"
        !autoSyncEnabled -> "已关闭自动同步，仅支持手动一键同步。"
        networkProfile == SyncNetworkProfile.Offline -> "当前离线，恢复网络后可自动同步。"
        pauseAutoSyncDuringWork && workInProgress -> "当前处于作业中，自动同步暂缓执行。"
        mediaSyncOnWifiOnly && networkProfile == SyncNetworkProfile.Mobile ->
            "当前为移动网络，将自动同步结果与报告，媒体素材等待 Wi‑Fi。"
        else -> "当前条件满足，系统会自动继续同步。"
    }

    fun stats(): UserStats {
        val completedComponents = components.count { it.status == ComponentStatus.Completed }
        val identifiedDefects = components.count { it.status.isAiDoneOrLater() }
        return UserStats(
            completedProjects = projects.count { projectProgress(it.id).completed > 0 },
            identifiedDefects = identifiedDefects,
            reportsCount = reports.size,
            completedComponents = completedComponents,
        )
    }

    fun addMedia(componentId: Long, media: List<ComponentMedia>) {
        val index = components.indexOfFirst { it.id == componentId }
        if (index < 0) return
        val component = components[index]
        val merged = (component.mediaItems + media).distinctBy { "${it.type}:${it.uri}" }
        components = components.toMutableList().also {
            it[index] = component.copy(
                mediaItems = merged,
                status = if (merged.isEmpty()) ComponentStatus.Pending else ComponentStatus.Collected,
                actualInspectorUserId = currentUser.id,
                actualInspectorName = currentUser.realName,
                syncStatus = "pending",
            )
        }
    }

    fun replaceMedia(componentId: Long, media: List<ComponentMedia>) {
        val index = components.indexOfFirst { it.id == componentId }
        if (index < 0) return
        val component = components[index]
        val normalizedMedia = media.distinctBy { "${it.type}:${it.uri}" }
        components = components.toMutableList().also {
            it[index] = component.copy(
                mediaItems = normalizedMedia,
                status = if (normalizedMedia.isEmpty()) ComponentStatus.Pending else ComponentStatus.Collected,
                actualInspectorUserId = currentUser.id,
                actualInspectorName = currentUser.realName,
                syncStatus = "pending",
            )
        }
    }

    fun removeMedia(componentId: Long, mediaId: String) {
        val index = components.indexOfFirst { it.id == componentId }
        if (index < 0) return
        val component = components[index]
        val remaining = component.mediaItems.filterNot { it.id == mediaId }
        components = components.toMutableList().also {
            it[index] = component.copy(
                mediaItems = remaining,
                status = when {
                    remaining.isEmpty() -> ComponentStatus.Pending
                    component.status == ComponentStatus.Completed -> ComponentStatus.Collected
                    component.status == ComponentStatus.AiDone -> ComponentStatus.Collected
                    else -> ComponentStatus.Collected
                },
                actualInspectorUserId = if (remaining.isEmpty()) null else currentUser.id,
                actualInspectorName = if (remaining.isEmpty()) "" else currentUser.realName,
                syncStatus = "pending",
            )
        }
    }

    fun runAi(componentId: Long) {
        val index = components.indexOfFirst { it.id == componentId }
        if (index < 0) return
        val component = components[index]
        if (component.mediaItems.isEmpty()) return
        val focusOptions = component.focusDefects.ifEmpty { listOf("裂缝") }
        val defect = focusOptions[(componentId % focusOptions.size).toInt()]
        val confidence = Random(componentId).nextInt(83, 98)
        val level = when (component.id % 3L) {
            0L -> "严重"
            1L -> "中等"
            else -> "轻微"
        }
        val location = "${component.number}${component.type}表面"
        val quantitativeInfo = defaultQuantitativeInfo(defect, component.number, confidence)
        val repairAdvice = defaultRepairAdvice(defect, level, component.type)
        components = components.toMutableList().also {
            it[index] = component.copy(
                status = ComponentStatus.AiDone,
                aiDetectedType = defect,
                aiDetectedLevel = level,
                aiDetectedLocation = location,
                aiQuantitativeInfo = quantitativeInfo,
                aiRepairAdvice = repairAdvice,
                defectType = defect,
                defectLevel = level,
                locationDesc = location,
                quantitativeInfo = quantitativeInfo,
                repairAdvice = repairAdvice,
                remarks = "AI已完成初步识别，建议人工复核。",
                aiConfidence = confidence,
                aiSummary = "$defect 识别置信度 ${confidence}%",
                actualInspectorUserId = currentUser.id,
                actualInspectorName = currentUser.realName,
                syncStatus = "pending",
            )
        }
    }

    fun runBatchAi(projectId: Long) {
        getProjectComponents(projectId)
            .filter { it.status == ComponentStatus.Collected }
            .forEach { runAi(it.id) }
    }

    fun saveComponentResult(
        componentId: Long,
        defectType: String,
        defectLevel: String,
        locationDesc: String,
        quantitativeInfo: String,
        repairAdvice: String,
        remarks: String,
        completed: Boolean,
    ) {
        val index = components.indexOfFirst { it.id == componentId }
        if (index < 0) return
        val component = components[index]
        components = components.toMutableList().also {
            it[index] = component.copy(
                status = if (completed) ComponentStatus.Completed else ComponentStatus.AiDone,
                defectType = defectType,
                defectLevel = defectLevel,
                locationDesc = locationDesc,
                quantitativeInfo = quantitativeInfo,
                repairAdvice = repairAdvice,
                remarks = remarks,
                actualInspectorUserId = currentUser.id,
                actualInspectorName = currentUser.realName,
                syncStatus = "pending",
            )
        }
    }

    fun addCustomComponent(
        projectId: Long,
        category: String,
        type: String,
        number: String,
        focusDefects: List<String>,
        responsibleUserId: Long,
    ): Long {
        val members = getProjectMembers(projectId)
        val responsible = members.firstOrNull { it.id == responsibleUserId }
        val newId = (components.maxOfOrNull { it.id } ?: 300L) + 1L
        val newComponent = ComponentTask(
            id = newId,
            projectId = projectId,
            category = category,
            type = type,
            number = number,
            focusDefects = focusDefects.ifEmpty { listOf("裂缝") },
            isCustom = true,
            createdByUserId = currentUser.id,
            createdByName = currentUser.realName,
            responsibleUserId = responsible?.id ?: currentUser.id,
            responsibleName = responsible?.realName ?: currentUser.realName,
            status = ComponentStatus.Pending,
        )
        components = (components + newComponent)
        return newId
    }

    fun generateRepairAdviceCandidate(
        baseAdvice: String,
        defectType: String,
        defectLevel: String,
        componentType: String,
        optimizationPrompt: String,
    ): String {
        val effectiveBaseAdvice = baseAdvice.ifBlank {
            defaultRepairAdvice(defectType, defectLevel, componentType)
        }
        if (optimizationPrompt.isBlank()) return effectiveBaseAdvice
        return buildString {
            append(effectiveBaseAdvice)
            append("\n\n")
            append("结合人工优化意见：")
            append(optimizationPrompt.trim())
            append("\n")
            append("AI新版建议：建议结合现场工况、病害发展趋势与作业窗口优化修复工序，并在处置完成后安排复检确认效果。")
        }
    }

    fun regenerateRepairAdvice(componentId: Long, optimizationPrompt: String) {
        val index = components.indexOfFirst { it.id == componentId }
        if (index < 0) return
        val component = components[index]
        val baseAdvice = component.aiRepairAdvice.ifBlank {
            defaultRepairAdvice(component.aiDetectedType.ifBlank { component.defectType }, component.aiDetectedLevel.ifBlank { component.defectLevel }, component.type)
        }
        val optimizedAdvice = buildString {
            append(baseAdvice)
            if (optimizationPrompt.isNotBlank()) {
                append("；结合人工补充意见：")
                append(optimizationPrompt.trim())
                append("。建议按现场实际工况优化工序、材料与复检时点。")
            }
        }
        components = components.toMutableList().also {
            it[index] = component.copy(
                repairAdvice = optimizedAdvice,
                remarks = if (optimizationPrompt.isBlank()) component.remarks else "AI已根据人工优化建议生成新版修复建议。",
                status = if (component.status == ComponentStatus.Pending) ComponentStatus.AiDone else component.status,
                syncStatus = "pending",
            )
        }
    }

    fun generateReport(projectId: Long): Long {
        val project = getProject(projectId) ?: return -1
        val bridge = getBridge(project.bridgeId) ?: return -1
        val allComponents = getProjectComponents(projectId)
        val componentList = allComponents.filter { it.status == ComponentStatus.Completed }
        val reportSummary = if (componentList.isEmpty()) {
            "当前项目尚未形成有效病害记录。"
        } else {
            "共归集 ${componentList.size} 个构件检测结果，覆盖上部结构、下部结构与附属设施。"
        }
        val maintenance = componentList.take(4).map {
            it.repairAdvice.ifBlank {
                defaultRepairAdvice(it.defectType, it.defectLevel, it.type)
            }
        }.ifEmpty { listOf("建议继续保持周期巡检，补充构件图像资料。") }

        val existingIndex = reports.indexOfFirst { it.projectId == projectId }
        val existingReport = reports.getOrNull(existingIndex)
        val report = ReportItem(
            id = existingReport?.id ?: System.currentTimeMillis(),
            projectId = projectId,
            bridgeId = bridge.id,
            reportCode = "REP-${projectId}-${bridge.id}",
            reportTitle = existingReport?.reportTitle ?: "${bridge.bridgeName}${project.projectType}报告",
            reportStatus = "draft",
            generatedDate = "2026-04-17",
            summary = existingReport?.summary ?: reportSummary,
            maintenanceAdvice = existingReport?.maintenanceAdvice?.takeIf { it.isNotEmpty() } ?: maintenance,
            conclusion = existingReport?.conclusion.orEmpty(),
            reviewOpinion = existingReport?.reviewOpinion.orEmpty(),
            synced = false,
        )
        reports = reports.toMutableList().also {
            if (existingIndex >= 0) {
                it[existingIndex] = report
            } else {
                it.add(0, report)
            }
        }
        reportSyncStates = reportSyncStates.toMutableMap().also {
            it[report.id] = "pending"
        }
        val nextProjectStatus = when {
            allComponents.isEmpty() -> project.projectStatus
            componentList.size == allComponents.size -> "completed"
            componentList.isNotEmpty() -> "in_progress"
            else -> project.projectStatus
        }
        projects = projects.map {
            if (it.id == projectId) it.copy(projectStatus = nextProjectStatus) else it
        }
        return report.id
    }

    fun buildReportPreview(reportId: Long): ReportPreviewData? {
        val report = getReport(reportId) ?: return null
        val project = getProject(report.projectId) ?: return null
        val bridge = getBridge(report.bridgeId) ?: return null
        val projectComponents = getProjectComponents(project.id)
        val inspectedComponents = projectComponents
            .filter { it.mediaItems.isNotEmpty() || it.status.isAiDoneOrLater() }
            .sortedWith(compareBy<ComponentTask> { it.category }.thenBy { it.number })

        val levelStats = ReportLevelStats(
            severe = inspectedComponents.count { it.defectLevel == "严重" },
            medium = inspectedComponents.count { it.defectLevel == "中等" },
            minor = inspectedComponents.count { it.defectLevel == "轻微" && it.defectType != "无病害" },
            normal = inspectedComponents.count { it.defectType == "无病害" },
        )

        val defectTypeStats = inspectedComponents
            .map { it.defectType.ifBlank { "待识别" } }
            .filter { it != "无病害" }
            .groupingBy { it }
            .eachCount()
            .map { NamedCount(name = it.key, count = it.value) }
            .sortedByDescending { it.count }

        val completionRate = if (projectComponents.isEmpty()) {
            0
        } else {
            (projectComponents.count { it.status == ComponentStatus.Completed } * 100f / projectComponents.size).toInt()
        }

        val generatedConclusion = when {
            inspectedComponents.isEmpty() -> "本次项目已建立桥梁与构件检测任务，但尚未形成有效病害识别成果，建议继续补充现场采集后再形成正式结论。"
            levelStats.severe > 0 -> "本次检测发现严重病害 ${levelStats.severe} 处，桥梁当前养护风险较高，建议尽快安排专项复核与处置。"
            levelStats.medium > 0 -> "本次检测已发现多处中等及以下病害，整体结构可继续运营，但应在近期组织针对性维修。"
            else -> "本次检测未发现明显高风险病害，桥梁总体处于可控状态，建议保持周期性巡检与持续观测。"
        }

        val generatedReviewOpinion = when {
            completionRate >= 100 -> "检测记录完整，报告内容可作为本阶段巡检成果归档。"
            completionRate >= 60 -> "当前报告可用于阶段性汇报，但仍建议补齐剩余构件检测后形成正式版。"
            else -> "当前报告为过程预览版，建议继续完善构件检测与人工校核。"
        }

        return ReportPreviewData(
            report = report,
            project = project,
            bridge = bridge,
            inspectedComponents = inspectedComponents,
            totalComponents = projectComponents.size,
            collectedComponents = projectComponents.count { it.status.isCollectedOrLater() },
            completedComponents = projectComponents.count { it.status == ComponentStatus.Completed },
            levelStats = levelStats,
            defectTypeStats = defectTypeStats,
            inspectionBasis = listOf(
                "《公路桥涵养护规范》",
                "《公路桥梁技术状况评定标准》",
                "项目现场采集图像与视频资料",
                "BridgeAI AI辅助识别与人工复核结果",
            ),
            conclusion = report.conclusion.ifBlank { generatedConclusion },
            reviewOpinion = report.reviewOpinion.ifBlank { generatedReviewOpinion },
        )
    }

    fun updateReportDraft(
        reportId: Long,
        title: String,
        summary: String,
        conclusion: String,
        reviewOpinion: String,
        maintenanceAdvice: List<String>,
    ) {
        val index = reports.indexOfFirst { it.id == reportId }
        if (index < 0) return
        val report = reports[index]
        reports = reports.toMutableList().also {
            it[index] = report.copy(
                reportTitle = title.trim(),
                summary = summary.trim(),
                conclusion = conclusion.trim(),
                reviewOpinion = reviewOpinion.trim(),
                maintenanceAdvice = maintenanceAdvice.map { advice -> advice.trim() }.filter { advice -> advice.isNotBlank() },
                synced = false,
            )
        }
        reportSyncStates = reportSyncStates.toMutableMap().also {
            it[reportId] = "pending"
        }
    }

    fun markReportReported(reportId: Long) {
        val index = reports.indexOfFirst { it.id == reportId }
        if (index < 0) return
        val report = reports[index]
        reports = reports.toMutableList().also {
            it[index] = report.copy(reportStatus = "reported", synced = false)
        }
        reportSyncStates = reportSyncStates.toMutableMap().also {
            it[reportId] = "pending"
        }
    }

    fun retrySyncEntry(key: String) {
        if (networkProfile == SyncNetworkProfile.Offline) return
        when {
            key.startsWith("component-") -> {
                val componentId = key.removePrefix("component-").toLongOrNull() ?: return
                components = components.map { component ->
                    if (component.id == componentId) component.copy(syncStatus = "synced") else component
                }
                lastSyncNotice = SyncNotice(
                    title = "已重试成功",
                    message = "失败的构件素材已重新加入成功队列。",
                    tone = SyncNoticeTone.Info,
                )
            }
            key.startsWith("report-") -> {
                val reportId = key.removePrefix("report-").toLongOrNull() ?: return
                reportSyncStates = reportSyncStates.toMutableMap().also {
                    it[reportId] = "synced"
                }
                reports = reports.map { report ->
                    if (report.id == reportId) report.copy(synced = true) else report
                }
                lastSyncNotice = SyncNotice(
                    title = "已重试成功",
                    message = "失败的报告内容已重新同步。",
                    tone = SyncNoticeTone.Info,
                )
            }
        }
    }

    fun startSync(trigger: SyncTrigger = SyncTrigger.Manual): Boolean {
        if (networkProfile == SyncNetworkProfile.Offline || isSyncing) return false
        if (trigger == SyncTrigger.Auto && !canAutoSyncNow()) return false
        val pendingComponents = components.count { component ->
            component.mediaItems.isNotEmpty() && component.syncStatus == "pending"
        }
        val deferredComponents =
            if (
                trigger == SyncTrigger.Auto &&
                mediaSyncOnWifiOnly &&
                networkProfile == SyncNetworkProfile.Mobile
            ) {
                pendingComponents
            } else {
                0
            }
        val readyComponents = pendingComponents - deferredComponents
        val pendingReports = reports.count { reportSyncStateOf(it) == "pending" }
        val readyCount = readyComponents + pendingReports
        if (readyCount == 0) {
            lastSyncNotice = when {
                waitingWifiCount() > 0 -> SyncNotice(
                    title = "等待 Wi‑Fi",
                    message = "当前没有可立即上传的数据，媒体素材会在连接 Wi‑Fi 后自动继续同步。",
                    tone = SyncNoticeTone.Warning,
                )
                failedSyncCount() > 0 -> SyncNotice(
                    title = "仍有失败项",
                    message = "当前队列里只有失败项，请优先单条重试。",
                    tone = SyncNoticeTone.Warning,
                )
                else -> SyncNotice(
                    title = "无需同步",
                    message = "当前没有新的待同步数据。",
                    tone = SyncNoticeTone.Info,
                )
            }
            return false
        }
        currentSyncSnapshot = SyncRunSnapshot(
            trigger = trigger,
            readyCount = readyCount,
            deferredCount = deferredComponents,
        )
        lastSyncNotice = null
        isSyncing = true
        syncProgress = if (trigger == SyncTrigger.Auto) 0.32f else 0.18f
        lastSyncTrigger = trigger
        components = components.map { component ->
            val shouldDeferToWifi =
                trigger == SyncTrigger.Auto &&
                    mediaSyncOnWifiOnly &&
                    networkProfile == SyncNetworkProfile.Mobile &&
                    component.syncStatus == "pending"
            when {
                component.mediaItems.isEmpty() -> component
                component.syncStatus == "pending" && !shouldDeferToWifi -> component.copy(syncStatus = "syncing")
                else -> component
            }
        }
        reportSyncStates = reportSyncStates.toMutableMap().also { stateMap ->
            reports.forEach { report ->
                if (stateMap[report.id] == null && !report.synced) {
                    stateMap[report.id] = "pending"
                }
                if (stateMap[report.id] == "pending") {
                    stateMap[report.id] = "syncing"
                }
            }
        }
        return true
    }

    fun finishSync(trigger: SyncTrigger = lastSyncTrigger ?: SyncTrigger.Manual) {
        if (!isSyncing) return
        components = components.map { component ->
            if (component.mediaItems.isEmpty()) {
                component
            } else {
                val shouldDeferToWifi =
                    trigger == SyncTrigger.Auto &&
                        mediaSyncOnWifiOnly &&
                        networkProfile == SyncNetworkProfile.Mobile &&
                        component.syncStatus == "pending"
                when (component.syncStatus) {
                    "syncing" -> component.copy(syncStatus = "synced")
                    "failed" -> component
                    else -> if (shouldDeferToWifi) component else component
                }
            }
        }
        reportSyncStates = reportSyncStates.toMutableMap().also { stateMap ->
            reports.forEach { report ->
                when (stateMap[report.id]) {
                    "syncing" -> stateMap[report.id] = "synced"
                    "failed" -> stateMap[report.id] = "failed"
                    null -> if (!report.synced) stateMap[report.id] = "synced"
                }
            }
        }
        reports = reports.map { report ->
            val synced = reportSyncStates[report.id] == "synced"
            if (report.synced == synced) report else report.copy(synced = synced)
        }
        val snapshot = currentSyncSnapshot
        val remainingCount = syncQueueCount()
        val remainingFailed = failedSyncCount()
        val remainingWaitingWifi = waitingWifiCount()
        isSyncing = false
        syncProgress = 0f
        currentSyncSnapshot = null
        lastSyncNotice = snapshot?.let {
            when {
                it.readyCount > 0 && remainingCount == 0 -> SyncNotice(
                    title = "同步已完成",
                    message = "本地待同步数据已全部上云，本轮共完成 ${it.readyCount} 项。",
                    tone = SyncNoticeTone.Success,
                )
                it.readyCount > 0 && remainingWaitingWifi > 0 -> SyncNotice(
                    title = "已完成部分同步",
                    message = "本轮已同步 ${it.readyCount} 项，另有 $remainingWaitingWifi 项媒体等待 Wi‑Fi。",
                    tone = SyncNoticeTone.Success,
                )
                it.readyCount > 0 && remainingFailed > 0 -> SyncNotice(
                    title = "同步基本完成",
                    message = "本轮已同步 ${it.readyCount} 项，仍有 $remainingFailed 项失败待重试。",
                    tone = SyncNoticeTone.Warning,
                )
                it.readyCount > 0 -> SyncNotice(
                    title = "同步已推进",
                    message = "本轮已同步 ${it.readyCount} 项，当前还剩 $remainingCount 项待处理。",
                    tone = SyncNoticeTone.Success,
                )
                else -> null
            }
        }
    }

    companion object {
        private fun sampleBridges() = listOf(
            BridgeItem(
                id = 101,
                bridgeCode = "G105-001",
                bridgeName = "长江大桥",
                route = "G105国道",
                bridgeType = "国省干线",
                structureType = "斜拉桥",
                constructionYear = 2005,
                totalLength = 900,
                bridgeWidth = 32,
                mainSpan = 500,
                statusText = "检测中",
                address = "江苏省南京市XX路段",
                maintenanceUnit = "XX交通养护公司",
            ),
            BridgeItem(
                id = 102,
                bridgeCode = "G107-002",
                bridgeName = "黄河大桥",
                route = "G107国道",
                bridgeType = "国省干线",
                structureType = "悬索桥",
                constructionYear = 2010,
                totalLength = 1200,
                bridgeWidth = 28,
                mainSpan = 800,
                statusText = "待检测",
                address = "山东省济南市XX路段",
                maintenanceUnit = "XX高速养护中心",
            ),
            BridgeItem(
                id = 103,
                bridgeCode = "SZ-003",
                bridgeName = "市政跨河桥",
                route = "市政主干道",
                bridgeType = "市政桥梁",
                structureType = "梁式桥",
                constructionYear = 2015,
                totalLength = 240,
                bridgeWidth = 18,
                mainSpan = 60,
                statusText = "已完成",
                address = "上海市浦东新区XX路段",
                maintenanceUnit = "市政设施管理中心",
            ),
            BridgeItem(
                id = 104,
                bridgeCode = "G228-004",
                bridgeName = "海湾跨线桥",
                route = "G228沿海通道",
                bridgeType = "国省干线",
                structureType = "连续箱梁桥",
                constructionYear = 2012,
                totalLength = 460,
                bridgeWidth = 24,
                mainSpan = 90,
                statusText = "检测中",
                address = "浙江省宁波市XX路段",
                maintenanceUnit = "沿海养护分公司",
            ),
            BridgeItem(
                id = 105,
                bridgeCode = "CS-005",
                bridgeName = "城区高架桥",
                route = "城市快速路",
                bridgeType = "市政桥梁",
                structureType = "预应力混凝土桥",
                constructionYear = 2018,
                totalLength = 320,
                bridgeWidth = 22,
                mainSpan = 45,
                statusText = "检测中",
                address = "湖南省长沙市XX高架",
                maintenanceUnit = "城建养护中心",
            ),
            BridgeItem(
                id = 106,
                bridgeCode = "G312-006",
                bridgeName = "西关互通桥",
                route = "G312国道",
                bridgeType = "国省干线",
                structureType = "T梁桥",
                constructionYear = 2008,
                totalLength = 680,
                bridgeWidth = 26,
                mainSpan = 70,
                statusText = "待检测",
                address = "陕西省西安市XX互通",
                maintenanceUnit = "西北高速养护中心",
            ),
            BridgeItem(
                id = 107,
                bridgeCode = "BJ-007",
                bridgeName = "滨江特大桥",
                route = "沿江快速路",
                bridgeType = "市政桥梁",
                structureType = "钢箱梁桥",
                constructionYear = 2016,
                totalLength = 760,
                bridgeWidth = 30,
                mainSpan = 180,
                statusText = "待检测",
                address = "江苏省苏州市滨江新区XX段",
                maintenanceUnit = "滨江养护管理所",
            ),
            BridgeItem(
                id = 108,
                bridgeCode = "BH-008",
                bridgeName = "北环立交桥",
                route = "城市北环快速路",
                bridgeType = "市政桥梁",
                structureType = "匝道桥",
                constructionYear = 2019,
                totalLength = 410,
                bridgeWidth = 20,
                mainSpan = 55,
                statusText = "检测中",
                address = "湖北省武汉市北环快速路XX互通",
                maintenanceUnit = "北环桥隧养护中心",
            ),
        )

        private fun sampleProjects() = listOf(
            ProjectItem(
                id = 201,
                projectCode = "PRJ-202604-001",
                projectName = "长江大桥2026年定期检测",
                bridgeId = 101,
                projectType = "定期检测",
                projectStatus = "in_progress",
                leaderName = "王工",
                startDate = "2026-04-16",
                endDate = "2026-04-20",
            ),
            ProjectItem(
                id = 202,
                projectCode = "PRJ-202604-002",
                projectName = "黄河大桥专项检测",
                bridgeId = 102,
                projectType = "特殊检测",
                projectStatus = "draft",
                leaderName = "王工",
                startDate = "2026-04-21",
                endDate = "2026-04-23",
            ),
            ProjectItem(
                id = 203,
                projectCode = "PRJ-202604-003",
                projectName = "市政跨河桥年度巡检",
                bridgeId = 103,
                projectType = "经常检查",
                projectStatus = "completed",
                leaderName = "陈工",
                startDate = "2026-04-08",
                endDate = "2026-04-10",
            ),
            ProjectItem(
                id = 204,
                projectCode = "PRJ-202604-004",
                projectName = "海湾跨线桥病害复核",
                bridgeId = 104,
                projectType = "复核检测",
                projectStatus = "in_progress",
                leaderName = "周工",
                startDate = "2026-04-18",
                endDate = "2026-04-22",
            ),
            ProjectItem(
                id = 205,
                projectCode = "PRJ-202604-005",
                projectName = "城区高架桥汛前专项排查",
                bridgeId = 105,
                projectType = "专项排查",
                projectStatus = "in_progress",
                leaderName = "王工",
                startDate = "2026-04-19",
                endDate = "2026-04-24",
            ),
            ProjectItem(
                id = 206,
                projectCode = "PRJ-202604-006",
                projectName = "西关互通桥定期检测准备",
                bridgeId = 106,
                projectType = "定期检测",
                projectStatus = "draft",
                leaderName = "赵工",
                startDate = "2026-04-24",
                endDate = "2026-04-28",
            ),
            ProjectItem(
                id = 207,
                projectCode = "PRJ-202604-007",
                projectName = "滨江特大桥专项巡检",
                bridgeId = 107,
                projectType = "专项巡检",
                projectStatus = "draft",
                leaderName = "赵工",
                startDate = "2026-04-25",
                endDate = "2026-04-27",
            ),
            ProjectItem(
                id = 208,
                projectCode = "PRJ-202604-008",
                projectName = "北环立交桥月度复核",
                bridgeId = 108,
                projectType = "复核检测",
                projectStatus = "in_progress",
                leaderName = "周工",
                startDate = "2026-04-17",
                endDate = "2026-04-21",
            ),
        )

        private fun sampleProjectMembers() = mapOf(
            201L to listOf(
                ProjectMember(1L, "张三", "检测员"),
                ProjectMember(2L, "李工", "检测员"),
                ProjectMember(3L, "王工", "项目负责人"),
            ),
            202L to listOf(
                ProjectMember(1L, "张三", "检测员"),
                ProjectMember(4L, "赵工", "检测员"),
                ProjectMember(3L, "王工", "项目负责人"),
            ),
            203L to listOf(
                ProjectMember(1L, "张三", "检测员"),
                ProjectMember(2L, "李工", "检测员"),
                ProjectMember(5L, "陈工", "项目负责人"),
            ),
            204L to listOf(
                ProjectMember(2L, "李工", "检测员"),
                ProjectMember(4L, "赵工", "检测员"),
                ProjectMember(6L, "周工", "项目负责人"),
            ),
            205L to listOf(
                ProjectMember(1L, "张三", "检测员"),
                ProjectMember(2L, "李工", "检测员"),
                ProjectMember(3L, "王工", "项目负责人"),
            ),
            206L to listOf(
                ProjectMember(1L, "张三", "检测员"),
                ProjectMember(4L, "赵工", "检测员"),
                ProjectMember(3L, "王工", "项目负责人"),
            ),
            207L to listOf(
                ProjectMember(1L, "张三", "检测员"),
                ProjectMember(2L, "李工", "检测员"),
                ProjectMember(4L, "赵工", "项目负责人"),
            ),
            208L to listOf(
                ProjectMember(1L, "张三", "检测员"),
                ProjectMember(2L, "李工", "检测员"),
                ProjectMember(6L, "周工", "项目负责人"),
            ),
        )

        private fun sampleComponents() = listOf(
            ComponentTask(301, 201, "上部结构", "主梁", "左幅1跨", listOf("裂缝", "混凝土剥落/露筋"), responsibleUserId = 1L, responsibleName = "张三", actualInspectorUserId = 2L, actualInspectorName = "李工", status = ComponentStatus.Completed, aiDetectedType = "裂缝", aiDetectedLevel = "中等", aiDetectedLocation = "主梁底部距墩2m", aiQuantitativeInfo = "裂缝长度约 2.1m，最大宽度约 1.0mm，共识别 3 条。", aiRepairAdvice = "主梁建议在 30 日内完成裂缝封闭处理，并补做定点复测。", defectType = "裂缝", defectLevel = "中等", locationDesc = "主梁底部距墩2m", quantitativeInfo = "裂缝长度约 2.3m，最大宽度约 1.2mm，共识别 3 条。", repairAdvice = "建议先进行裂缝封闭处理，并在 30 日内完成表面修补与复测。", remarks = "已人工确认，建议月内修复", aiConfidence = 95, aiSummary = "裂缝识别置信度 95%", syncStatus = "pending"),
            ComponentTask(302, 201, "上部结构", "桥面板", "左幅2跨", listOf("表面劣化", "渗水/潮湿"), responsibleUserId = 2L, responsibleName = "李工", actualInspectorUserId = 1L, actualInspectorName = "张三", status = ComponentStatus.Collected, syncStatus = "pending"),
            ComponentTask(303, 201, "上部结构", "伸缩缝", "全线", listOf("施工缺陷", "渗水/潮湿"), responsibleUserId = 1L, responsibleName = "张三", status = ComponentStatus.Pending),
            ComponentTask(304, 201, "下部结构", "桥墩", "1#墩", listOf("裂缝", "锈蚀"), responsibleUserId = 2L, responsibleName = "李工", actualInspectorUserId = 1L, actualInspectorName = "张三", status = ComponentStatus.AiDone, aiDetectedType = "锈蚀", aiDetectedLevel = "轻微", aiDetectedLocation = "墩身侧面", aiQuantitativeInfo = "锈蚀区域约 0.16m2，局部钢筋外露长度约 0.3m。", aiRepairAdvice = "桥墩建议先清理锈蚀区域，再进行阻锈、防护与表层修补。", defectType = "锈蚀", defectLevel = "轻微", locationDesc = "墩身侧面", quantitativeInfo = "锈蚀区域约 0.18m2，局部钢筋外露长度约 0.4m。", repairAdvice = "建议清理锈蚀部位并进行阻锈、防护与表层修补，纳入下一轮复检重点。", remarks = "AI建议关注，待人工确认", aiConfidence = 88, aiSummary = "锈蚀识别置信度 88%", syncStatus = "pending"),
            ComponentTask(305, 201, "下部结构", "桥台", "右岸桥台", listOf("混凝土空洞", "裂缝"), responsibleUserId = 1L, responsibleName = "张三", status = ComponentStatus.Pending),
            ComponentTask(306, 201, "附属设施", "栏杆", "东侧全线", listOf("锈蚀", "施工缺陷"), responsibleUserId = 2L, responsibleName = "李工", status = ComponentStatus.Pending),
            ComponentTask(307, 201, "附属设施", "排水系统", "南侧", listOf("渗水/潮湿"), isCustom = true, createdByUserId = 2L, createdByName = "李工", responsibleUserId = 2L, responsibleName = "李工", status = ComponentStatus.Pending),
            ComponentTask(308, 202, "上部结构", "主梁", "右幅1跨", listOf("裂缝", "混凝土剥落/露筋"), responsibleUserId = 1L, responsibleName = "张三", status = ComponentStatus.Pending),
            ComponentTask(309, 202, "下部结构", "桥墩", "2#墩", listOf("锈蚀", "裂缝"), responsibleUserId = 4L, responsibleName = "赵工", status = ComponentStatus.Pending),
            ComponentTask(310, 202, "下部结构", "桥台", "西岸桥台", listOf("渗水/潮湿"), responsibleUserId = 4L, responsibleName = "赵工", status = ComponentStatus.Collected, syncStatus = "pending"),
            ComponentTask(311, 202, "附属设施", "支座", "2#墩上游侧", listOf("锈蚀", "施工缺陷"), responsibleUserId = 1L, responsibleName = "张三", status = ComponentStatus.Pending),
            ComponentTask(312, 202, "附属设施", "防撞护栏", "引桥段", listOf("表面劣化", "裂缝"), responsibleUserId = 4L, responsibleName = "赵工", status = ComponentStatus.Pending),
            ComponentTask(313, 203, "上部结构", "桥面铺装", "全桥", listOf("表面劣化"), responsibleUserId = 1L, responsibleName = "张三", actualInspectorUserId = 1L, actualInspectorName = "张三", status = ComponentStatus.Completed, aiDetectedType = "表面劣化", aiDetectedLevel = "轻微", aiDetectedLocation = "桥面铺装中段", aiQuantitativeInfo = "局部表面劣化面积约 0.22m2。", aiRepairAdvice = "建议对表面劣化区域进行局部修补并纳入年度跟踪。", defectType = "表面劣化", defectLevel = "轻微", locationDesc = "桥面铺装中段", quantitativeInfo = "局部表面劣化面积约 0.25m2。", repairAdvice = "建议进行局部修补处理并持续观察。", remarks = "不影响通行，建议例行维护", aiConfidence = 90, aiSummary = "表面劣化识别置信度 90%", syncStatus = "synced"),
            ComponentTask(314, 203, "下部结构", "桥墩", "3#墩", listOf("无病害"), responsibleUserId = 2L, responsibleName = "李工", actualInspectorUserId = 2L, actualInspectorName = "李工", status = ComponentStatus.Completed, aiDetectedType = "无病害", aiDetectedLevel = "轻微", aiDetectedLocation = "墩身整体", aiQuantitativeInfo = "未见明显异常特征。", aiRepairAdvice = "保持周期性检查即可。", defectType = "无病害", defectLevel = "轻微", locationDesc = "墩身整体", quantitativeInfo = "未见明显异常特征。", repairAdvice = "保持常规巡检。", remarks = "现场复核无异常", aiConfidence = 93, aiSummary = "无病害识别置信度 93%", syncStatus = "synced"),
            ComponentTask(315, 203, "附属设施", "排水沟", "北侧全线", listOf("渗水/潮湿"), responsibleUserId = 1L, responsibleName = "张三", actualInspectorUserId = 1L, actualInspectorName = "张三", status = ComponentStatus.Completed, aiDetectedType = "渗水/潮湿", aiDetectedLevel = "轻微", aiDetectedLocation = "北侧排水沟接口", aiQuantitativeInfo = "潮湿痕迹约 0.9m。", aiRepairAdvice = "建议疏通排水沟并检查接缝密封。", defectType = "渗水/潮湿", defectLevel = "轻微", locationDesc = "北侧排水沟接口", quantitativeInfo = "潮湿痕迹约 1.1m。", repairAdvice = "建议进行疏通与密封维护。", remarks = "已通知养护班组处理", aiConfidence = 86, aiSummary = "渗水/潮湿识别置信度 86%", syncStatus = "synced"),
            ComponentTask(316, 203, "附属设施", "栏杆", "西侧", listOf("锈蚀"), responsibleUserId = 2L, responsibleName = "李工", actualInspectorUserId = 2L, actualInspectorName = "李工", status = ComponentStatus.Completed, aiDetectedType = "锈蚀", aiDetectedLevel = "轻微", aiDetectedLocation = "栏杆底座", aiQuantitativeInfo = "轻微锈蚀面积约 0.08m2。", aiRepairAdvice = "建议打磨除锈并补涂防护层。", defectType = "锈蚀", defectLevel = "轻微", locationDesc = "栏杆底座", quantitativeInfo = "轻微锈蚀面积约 0.10m2。", repairAdvice = "建议在常规维护窗口中完成除锈补漆。", remarks = "已纳入月度养护计划", aiConfidence = 89, aiSummary = "锈蚀识别置信度 89%", syncStatus = "synced"),
            ComponentTask(317, 204, "上部结构", "箱梁腹板", "2联中跨", listOf("裂缝", "混凝土空洞"), responsibleUserId = 2L, responsibleName = "李工", status = ComponentStatus.Collected, syncStatus = "pending"),
            ComponentTask(318, 204, "上部结构", "横隔板", "1联", listOf("裂缝"), responsibleUserId = 4L, responsibleName = "赵工", status = ComponentStatus.AiDone, aiDetectedType = "裂缝", aiDetectedLevel = "中等", aiDetectedLocation = "横隔板下缘", aiQuantitativeInfo = "裂缝长度约 1.6m，最大宽度约 0.8mm。", aiRepairAdvice = "建议阶段内完成封闭处理并复测。", defectType = "裂缝", defectLevel = "中等", locationDesc = "横隔板下缘", quantitativeInfo = "裂缝长度约 1.7m，最大宽度约 0.9mm。", repairAdvice = "建议月内完成裂缝处理并补做观测记录。", remarks = "待负责人终审", aiConfidence = 91, aiSummary = "裂缝识别置信度 91%", syncStatus = "pending"),
            ComponentTask(319, 204, "下部结构", "桥墩", "5#墩", listOf("锈蚀", "混凝土剥落/露筋"), responsibleUserId = 2L, responsibleName = "李工", status = ComponentStatus.Pending),
            ComponentTask(320, 204, "附属设施", "泄水孔", "上游侧", listOf("渗水/潮湿", "施工缺陷"), responsibleUserId = 4L, responsibleName = "赵工", isCustom = true, createdByUserId = 4L, createdByName = "赵工", status = ComponentStatus.Collected, syncStatus = "pending"),
            ComponentTask(321, 205, "上部结构", "桥面板", "K3+220", listOf("表面劣化", "裂缝"), responsibleUserId = 1L, responsibleName = "张三", status = ComponentStatus.Completed, actualInspectorUserId = 1L, actualInspectorName = "张三", aiDetectedType = "表面劣化", aiDetectedLevel = "轻微", aiDetectedLocation = "桥面板行车带", aiQuantitativeInfo = "劣化区域约 0.30m2。", aiRepairAdvice = "建议进行局部铣刨修补。", defectType = "表面劣化", defectLevel = "轻微", locationDesc = "桥面板行车带", quantitativeInfo = "劣化区域约 0.35m2。", repairAdvice = "建议配合汛前养护完成局部修补。", remarks = "已完成处理建议录入", aiConfidence = 87, aiSummary = "表面劣化识别置信度 87%", syncStatus = "pending"),
            ComponentTask(322, 205, "下部结构", "盖梁", "4#墩", listOf("裂缝"), responsibleUserId = 2L, responsibleName = "李工", status = ComponentStatus.Collected, syncStatus = "pending"),
            ComponentTask(323, 205, "附属设施", "防抛网", "南侧全线", listOf("施工缺陷", "锈蚀"), responsibleUserId = 2L, responsibleName = "李工", status = ComponentStatus.Pending),
            ComponentTask(324, 205, "附属设施", "排水口", "东段", listOf("渗水/潮湿"), responsibleUserId = 1L, responsibleName = "张三", status = ComponentStatus.AiDone, aiDetectedType = "渗水/潮湿", aiDetectedLevel = "轻微", aiDetectedLocation = "排水口接口", aiQuantitativeInfo = "潮湿痕迹约 1.2m。", aiRepairAdvice = "建议排查堵塞与密封老化问题。", defectType = "渗水/潮湿", defectLevel = "轻微", locationDesc = "排水口接口", quantitativeInfo = "潮湿痕迹约 1.4m。", repairAdvice = "建议结合汛前维护完成清疏与密封修复。", remarks = "待现场复核", aiConfidence = 84, aiSummary = "渗水/潮湿识别置信度 84%", syncStatus = "pending"),
            ComponentTask(325, 206, "上部结构", "T梁", "左幅3跨", listOf("裂缝"), responsibleUserId = 1L, responsibleName = "张三", status = ComponentStatus.Pending),
            ComponentTask(326, 206, "下部结构", "桥墩", "6#墩", listOf("锈蚀", "裂缝"), responsibleUserId = 4L, responsibleName = "赵工", status = ComponentStatus.Pending),
            ComponentTask(327, 206, "附属设施", "伸缩装置", "中幅", listOf("施工缺陷", "渗水/潮湿"), responsibleUserId = 1L, responsibleName = "张三", status = ComponentStatus.Pending),
            ComponentTask(328, 202, "上部结构", "吊杆", "北岸侧", listOf("锈蚀", "裂缝"), responsibleUserId = 1L, responsibleName = "张三", status = ComponentStatus.Collected, syncStatus = "pending"),
            ComponentTask(329, 202, "附属设施", "伸缩缝", "主跨中部", listOf("施工缺陷", "渗水/潮湿"), responsibleUserId = 4L, responsibleName = "赵工", actualInspectorUserId = 4L, actualInspectorName = "赵工", status = ComponentStatus.AiDone, aiDetectedType = "施工缺陷", aiDetectedLevel = "中等", aiDetectedLocation = "缝体接缝处", aiQuantitativeInfo = "接缝错台约 8mm，局部伴随渗水痕迹。", aiRepairAdvice = "建议先复测平整度，再安排接缝修复与止水处理。", defectType = "施工缺陷", defectLevel = "中等", locationDesc = "缝体接缝处", quantitativeInfo = "接缝错台约 9mm，渗水长度约 0.8m。", repairAdvice = "建议结合专项检测完成伸缩缝局部修整与密封维护。", remarks = "建议纳入本轮专项处置", aiConfidence = 89, aiSummary = "施工缺陷识别置信度 89%", syncStatus = "pending"),
            ComponentTask(330, 204, "下部结构", "承台", "5#墩", listOf("裂缝", "渗水/潮湿"), responsibleUserId = 2L, responsibleName = "李工", status = ComponentStatus.Pending),
            ComponentTask(331, 204, "附属设施", "防撞墙", "下行侧", listOf("表面劣化", "裂缝"), responsibleUserId = 4L, responsibleName = "赵工", actualInspectorUserId = 2L, actualInspectorName = "李工", status = ComponentStatus.Completed, aiDetectedType = "表面劣化", aiDetectedLevel = "轻微", aiDetectedLocation = "墙身转角位置", aiQuantitativeInfo = "表面劣化面积约 0.26m2。", aiRepairAdvice = "建议进行局部修补并补涂防护层。", defectType = "表面劣化", defectLevel = "轻微", locationDesc = "墙身转角位置", quantitativeInfo = "表面劣化面积约 0.30m2。", repairAdvice = "建议结合本次复核同步完成表层修补。", remarks = "已录入维护建议", aiConfidence = 85, aiSummary = "表面劣化识别置信度 85%", syncStatus = "pending"),
            ComponentTask(332, 205, "上部结构", "箱梁腹板", "K3+260", listOf("裂缝", "混凝土剥落/露筋"), responsibleUserId = 2L, responsibleName = "李工", status = ComponentStatus.Collected, syncStatus = "pending"),
            ComponentTask(333, 205, "下部结构", "支座", "4#墩", listOf("锈蚀", "施工缺陷"), responsibleUserId = 1L, responsibleName = "张三", status = ComponentStatus.Pending),
            ComponentTask(334, 206, "上部结构", "T梁", "右幅2跨", listOf("裂缝", "表面劣化"), responsibleUserId = 1L, responsibleName = "张三", status = ComponentStatus.Pending),
            ComponentTask(335, 206, "附属设施", "泄水孔", "北侧", listOf("渗水/潮湿"), responsibleUserId = 4L, responsibleName = "赵工", status = ComponentStatus.Collected, syncStatus = "pending"),
            ComponentTask(336, 207, "上部结构", "钢箱梁", "南幅中跨", listOf("裂缝", "锈蚀"), responsibleUserId = 1L, responsibleName = "张三", status = ComponentStatus.Pending),
            ComponentTask(337, 207, "下部结构", "桥墩", "7#墩", listOf("裂缝", "混凝土剥落/露筋"), responsibleUserId = 2L, responsibleName = "李工", status = ComponentStatus.Collected, syncStatus = "pending"),
            ComponentTask(338, 207, "附属设施", "栏杆", "东侧引桥", listOf("锈蚀", "施工缺陷"), responsibleUserId = 1L, responsibleName = "张三", status = ComponentStatus.Pending),
            ComponentTask(339, 208, "上部结构", "匝道梁", "A匝道", listOf("裂缝", "表面劣化"), responsibleUserId = 1L, responsibleName = "张三", actualInspectorUserId = 1L, actualInspectorName = "张三", status = ComponentStatus.Completed, aiDetectedType = "裂缝", aiDetectedLevel = "轻微", aiDetectedLocation = "腹板中部", aiQuantitativeInfo = "细微裂缝长度约 0.7m，宽度小于 0.3mm。", aiRepairAdvice = "建议纳入月度观测，无需立即封闭处理。", defectType = "裂缝", defectLevel = "轻微", locationDesc = "腹板中部", quantitativeInfo = "细微裂缝长度约 0.9m，宽度约 0.3mm。", repairAdvice = "建议加强观测并在下轮巡检复核。", remarks = "已完成现场复核", aiConfidence = 92, aiSummary = "裂缝识别置信度 92%", syncStatus = "pending"),
            ComponentTask(340, 208, "下部结构", "盖梁", "B2#墩", listOf("裂缝", "渗水/潮湿"), responsibleUserId = 2L, responsibleName = "李工", actualInspectorUserId = 2L, actualInspectorName = "李工", status = ComponentStatus.AiDone, aiDetectedType = "裂缝", aiDetectedLevel = "中等", aiDetectedLocation = "盖梁端部", aiQuantitativeInfo = "裂缝长度约 1.4m，最大宽度约 0.7mm。", aiRepairAdvice = "建议安排封闭处理并补做裂缝观测。", defectType = "裂缝", defectLevel = "中等", locationDesc = "盖梁端部", quantitativeInfo = "裂缝长度约 1.6m，最大宽度约 0.8mm。", repairAdvice = "建议在本轮复核内完成裂缝封闭与复测。", remarks = "待负责人确认处置级别", aiConfidence = 90, aiSummary = "裂缝识别置信度 90%", syncStatus = "pending"),
            ComponentTask(341, 208, "附属设施", "支座", "B2#墩下游侧", listOf("锈蚀", "施工缺陷"), responsibleUserId = 2L, responsibleName = "李工", status = ComponentStatus.Collected, syncStatus = "pending"),
            ComponentTask(342, 208, "附属设施", "排水沟", "北侧匝道", listOf("渗水/潮湿"), responsibleUserId = 1L, responsibleName = "张三", status = ComponentStatus.Pending),
        ).map {
            when (it.id) {
                301L -> it.copy(
                    mediaItems = listOf(
                        samplePhoto("m301-1"),
                        samplePhoto("m301-2"),
                    ),
                )
                302L -> it.copy(
                    mediaItems = listOf(samplePhoto("m302-1")),
                )
                304L -> it.copy(
                    mediaItems = listOf(
                        samplePhoto("m304-1"),
                    ),
                )
                310L -> it.copy(
                    mediaItems = listOf(
                        samplePhoto("m310-1"),
                        sampleVideo("m310-v1"),
                    ),
                )
                313L -> it.copy(
                    mediaItems = listOf(samplePhoto("m313-1")),
                )
                314L -> it.copy(
                    mediaItems = listOf(samplePhoto("m314-1")),
                )
                315L -> it.copy(
                    mediaItems = listOf(samplePhoto("m315-1")),
                )
                316L -> it.copy(
                    mediaItems = listOf(samplePhoto("m316-1")),
                )
                317L -> it.copy(
                    mediaItems = listOf(
                        samplePhoto("m317-1"),
                        samplePhoto("m317-2"),
                    ),
                )
                318L -> it.copy(
                    mediaItems = listOf(samplePhoto("m318-1")),
                )
                320L -> it.copy(
                    mediaItems = listOf(samplePhoto("m320-1")),
                )
                321L -> it.copy(
                    mediaItems = listOf(samplePhoto("m321-1")),
                )
                322L -> it.copy(
                    mediaItems = listOf(samplePhoto("m322-1")),
                )
                324L -> it.copy(
                    mediaItems = listOf(samplePhoto("m324-1")),
                )
                328L -> it.copy(
                    mediaItems = listOf(samplePhoto("m328-1")),
                )
                329L -> it.copy(
                    mediaItems = listOf(
                        samplePhoto("m329-1"),
                        sampleVideo("m329-v1"),
                    ),
                )
                331L -> it.copy(
                    mediaItems = listOf(samplePhoto("m331-1")),
                )
                332L -> it.copy(
                    mediaItems = listOf(
                        samplePhoto("m332-1"),
                        samplePhoto("m332-2"),
                    ),
                )
                335L -> it.copy(
                    mediaItems = listOf(samplePhoto("m335-1")),
                )
                337L -> it.copy(
                    mediaItems = listOf(samplePhoto("m337-1")),
                )
                339L -> it.copy(
                    mediaItems = listOf(samplePhoto("m339-1")),
                )
                340L -> it.copy(
                    mediaItems = listOf(samplePhoto("m340-1")),
                )
                341L -> it.copy(
                    mediaItems = listOf(
                        samplePhoto("m341-1"),
                        sampleVideo("m341-v1"),
                    ),
                )
                else -> it
            }
        }

        private fun sampleReports() = listOf(
            ReportItem(
                id = 901,
                projectId = 201,
                bridgeId = 101,
                reportCode = "REP-201-101",
                reportTitle = "长江大桥定期检测报告",
                reportStatus = "draft",
                generatedDate = "2026-04-16",
                summary = "已归集 2 个构件检测结论，当前为草稿版报告。",
                maintenanceAdvice = listOf(
                    "建议先进行裂缝封闭处理，并在 30 日内完成表面修补与复测。",
                    "建议清理锈蚀部位并进行阻锈、防护与表层修补，纳入下一轮复检重点。",
                ),
                synced = false,
            ),
            ReportItem(
                id = 902,
                projectId = 203,
                bridgeId = 103,
                reportCode = "REP-203-103",
                reportTitle = "市政跨河桥年度巡检报告",
                reportStatus = "reported",
                generatedDate = "2026-04-10",
                summary = "本次巡检完成 4 个核心构件检查，整体病害轻微，已形成正式归档报告。",
                maintenanceAdvice = listOf(
                    "建议在月度养护窗口中完成栏杆除锈补漆。",
                    "建议对局部排水潮湿区域进行清疏与密封维护。",
                ),
                synced = true,
            ),
            ReportItem(
                id = 903,
                projectId = 204,
                bridgeId = 104,
                reportCode = "REP-204-104",
                reportTitle = "海湾跨线桥病害复核阶段报告",
                reportStatus = "completed",
                generatedDate = "2026-04-20",
                summary = "已完成 2 个重点构件复核，发现 1 处中等裂缝病害，建议继续补充其余点位。",
                maintenanceAdvice = listOf(
                    "建议优先处理横隔板裂缝并补做观测记录。",
                    "建议补充桥墩锈蚀与剥落区域的图像资料。",
                ),
                synced = false,
            ),
            ReportItem(
                id = 904,
                projectId = 205,
                bridgeId = 105,
                reportCode = "REP-205-105",
                reportTitle = "城区高架桥汛前专项排查报告",
                reportStatus = "draft",
                generatedDate = "2026-04-21",
                summary = "已形成 3 条构件检测结论，当前仍有排水与盖梁点位待补采。",
                maintenanceAdvice = listOf(
                    "建议先完成排水口疏通与密封修复。",
                    "建议对桥面板表面劣化区域进行局部修补。",
                ),
                synced = false,
            ),
            ReportItem(
                id = 905,
                projectId = 202,
                bridgeId = 102,
                reportCode = "REP-202-102",
                reportTitle = "黄河大桥专项检测预检报告",
                reportStatus = "draft",
                generatedDate = "2026-04-21",
                summary = "已完成桥台、吊杆、伸缩缝等 3 个点位资料归集，待补充桥墩与支座数据。",
                maintenanceAdvice = listOf(
                    "建议优先复核伸缩缝错台和渗水问题。",
                    "建议补充吊杆近景照片与锈蚀量化记录。",
                ),
                synced = false,
            ),
            ReportItem(
                id = 906,
                projectId = 206,
                bridgeId = 106,
                reportCode = "REP-206-106",
                reportTitle = "西关互通桥检测准备清单",
                reportStatus = "draft",
                generatedDate = "2026-04-21",
                summary = "已建立本轮检测任务与构件清单，待现场正式采集后生成初版检测报告。",
                maintenanceAdvice = listOf(
                    "建议提前校验采集路线和互通区交通组织。",
                    "建议优先覆盖泄水孔与伸缩装置点位。",
                ),
                synced = false,
            ),
            ReportItem(
                id = 907,
                projectId = 208,
                bridgeId = 108,
                reportCode = "REP-208-108",
                reportTitle = "北环立交桥月度复核报告",
                reportStatus = "completed",
                generatedDate = "2026-04-19",
                summary = "已完成 3 个重点构件复核，当前存在 1 处中等裂缝病害待安排处置。",
                maintenanceAdvice = listOf(
                    "建议优先处理盖梁端部裂缝。",
                    "建议补充支座锈蚀与位移状态记录。",
                ),
                synced = false,
            ),
            ReportItem(
                id = 908,
                projectId = 207,
                bridgeId = 107,
                reportCode = "REP-207-107",
                reportTitle = "滨江特大桥巡检计划草案",
                reportStatus = "draft",
                generatedDate = "2026-04-21",
                summary = "已生成专项巡检草案，当前已布置钢箱梁、桥墩与栏杆点位的检测任务。",
                maintenanceAdvice = listOf(
                    "建议按南幅中跨到东侧引桥顺序组织采集。",
                    "建议预留钢箱梁近景拍摄与墩顶裂缝复测时间。",
                ),
                synced = false,
            ),
        )

        private fun samplePhoto(id: String) =
            ComponentMedia(id, "android.resource://com.bridgeai.app/drawable/ic_launcher_foreground", MediaType.Photo)

        private fun sampleVideo(id: String) =
            ComponentMedia(id, "android.resource://com.bridgeai.app/drawable/ic_launcher_foreground", MediaType.Video)
    }

    private fun reportSyncStateOf(report: ReportItem): String =
        reportSyncStates[report.id] ?: if (report.synced) "synced" else "pending"

    private fun reportSyncEntryState(report: ReportItem): SyncEntryState = when (reportSyncStateOf(report)) {
        "failed" -> SyncEntryState.Failed
        "syncing" -> SyncEntryState.Syncing
        else -> SyncEntryState.Pending
    }

    private fun seedInitialComponentSyncStates(items: List<ComponentTask>): List<ComponentTask> =
        items.map { component ->
            when (component.id) {
                329L -> component.copy(syncStatus = "failed")
                341L -> component.copy(syncStatus = "pending")
                else -> component
            }
        }

    private fun seedInitialReportSyncStates(items: List<ReportItem>): Map<Long, String> =
        items.associate { report ->
            val state = when {
                report.id == 905L -> "failed"
                report.synced -> "synced"
                else -> "pending"
            }
            report.id to state
        }
}

private fun defaultQuantitativeInfo(defect: String, number: String, confidence: Int): String = when (defect) {
    "裂缝" -> "AI测得 $number 构件裂缝长度约 2.1m，最大宽度约 1.1mm，识别可信度 ${confidence}% 。"
    "锈蚀" -> "AI测得锈蚀区域约 0.16m2，影响范围集中在构件边缘，识别可信度 ${confidence}% 。"
    "混凝土剥落/露筋" -> "AI测得剥落区域约 0.09m2，局部存在露筋风险，识别可信度 ${confidence}% 。"
    "渗水/潮湿" -> "AI识别潮湿渗水痕迹长度约 1.4m，主要集中于接缝边缘，识别可信度 ${confidence}% 。"
    "表面劣化" -> "AI识别表面劣化区域约 0.28m2，表层纹理磨损较明显，识别可信度 ${confidence}% 。"
    "施工缺陷" -> "AI识别 $number 构件存在局部错台与边角不顺直问题，建议结合现场尺量复核，识别可信度 ${confidence}% 。"
    "混凝土空洞" -> "AI识别疑似空洞区域约 0.12m2，主要分布于构件边缘，识别可信度 ${confidence}% 。"
    else -> "AI已识别 $defect，当前已完成基础量化测算，识别可信度 ${confidence}% 。"
}

private fun defaultRepairAdvice(defect: String, level: String, type: String): String = when (defect) {
    "裂缝" -> when (level) {
        "严重" -> "$type 存在严重裂缝，建议 7 日内安排专项封闭、灌缝与结构复核。"
        "中等" -> "$type 建议在 30 日内完成裂缝封闭处理，并补做定点复测。"
        else -> "$type 建议持续观测裂缝发展，纳入下次巡检重点。"
    }
    "锈蚀" -> "$type 建议先清理锈蚀区域，再进行阻锈、防护与表层修补。"
    "混凝土剥落/露筋" -> "$type 建议尽快修补剥落区域，并核查保护层与露筋情况。"
    "渗水/潮湿" -> "$type 建议排查渗水源头并修复防排水系统，必要时补强接缝密封。"
    "表面劣化" -> "$type 建议进行局部修补处理，并在下轮巡检中复核劣化范围变化。"
    "施工缺陷" -> "$type 建议复测构造尺寸和平整度，并结合现场情况进行局部调整与修补。"
    "混凝土空洞" -> "$type 建议尽快复核空洞范围，必要时采用注浆或局部修补措施。"
    "无病害" -> "$type 当前未见明显病害，建议保持常规巡检与周期复核。"
    else -> "$type 建议结合现场情况制定专项修复措施，并安排复检。"
}

private fun ComponentStatus.isCollectedOrLater(): Boolean = when (this) {
    ComponentStatus.Pending -> false
    ComponentStatus.Collected,
    ComponentStatus.AiDone,
    ComponentStatus.Completed -> true
}

private fun ComponentStatus.isAiDoneOrLater(): Boolean = when (this) {
    ComponentStatus.Pending,
    ComponentStatus.Collected -> false
    ComponentStatus.AiDone,
    ComponentStatus.Completed -> true
}

enum class SyncNetworkProfile {
    Offline,
    Mobile,
    Wifi,
}

enum class SyncTrigger {
    Manual,
    Auto,
}

enum class SyncEntryState(val priority: Int) {
    Failed(0),
    Syncing(1),
    Pending(2),
    WaitingWifi(3),
}

data class SyncQueueEntry(
    val key: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val state: SyncEntryState,
)

enum class SyncNoticeTone {
    Success,
    Info,
    Warning,
}

data class SyncNotice(
    val title: String,
    val message: String,
    val tone: SyncNoticeTone,
    val id: Long = System.currentTimeMillis(),
)

data class SyncRunSnapshot(
    val trigger: SyncTrigger,
    val readyCount: Int,
    val deferredCount: Int,
)

enum class AppRole {
    Leader,
    Member,
}

data class ProjectProgress(
    val total: Int,
    val collected: Int,
    val aiDone: Int,
    val completed: Int,
)

data class UserStats(
    val completedProjects: Int,
    val identifiedDefects: Int,
    val reportsCount: Int,
    val completedComponents: Int,
)

private fun String.toReportStatusText(): String = when (this) {
    "reported" -> "已上报"
    "completed" -> "已完成"
    else -> "草稿"
}
