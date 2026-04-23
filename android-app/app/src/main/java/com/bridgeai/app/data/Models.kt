package com.bridgeai.app.data

data class UserProfile(
    val id: Long,
    val realName: String,
    val userCode: String,
    val company: String,
    val role: String,
)

data class ProjectMember(
    val id: Long,
    val realName: String,
    val roleLabel: String,
)

data class BridgeItem(
    val id: Long,
    val bridgeCode: String,
    val bridgeName: String,
    val route: String,
    val bridgeType: String,
    val structureType: String,
    val constructionYear: Int,
    val totalLength: Int,
    val bridgeWidth: Int,
    val mainSpan: Int,
    val statusText: String,
    val address: String,
    val maintenanceUnit: String,
)

data class ProjectItem(
    val id: Long,
    val projectCode: String,
    val projectName: String,
    val bridgeId: Long,
    val projectType: String,
    val projectStatus: String,
    val leaderName: String,
    val startDate: String,
    val endDate: String,
)

data class ComponentMedia(
    val id: String,
    val uri: String,
    val type: MediaType,
)

enum class MediaType {
    Photo,
    Video,
}

enum class ComponentStatus {
    Pending,
    Collected,
    AiDone,
    Completed,
}

data class ComponentTask(
    val id: Long,
    val projectId: Long,
    val category: String,
    val type: String,
    val number: String,
    val focusDefects: List<String>,
    val isCustom: Boolean = false,
    val createdByUserId: Long = 0L,
    val createdByName: String = "",
    val responsibleUserId: Long = 0L,
    val responsibleName: String = "",
    val actualInspectorUserId: Long? = null,
    val actualInspectorName: String = "",
    val status: ComponentStatus,
    val mediaItems: List<ComponentMedia> = emptyList(),
    val aiDetectedType: String = "",
    val aiDetectedLevel: String = "",
    val aiDetectedLocation: String = "",
    val aiQuantitativeInfo: String = "",
    val aiRepairAdvice: String = "",
    val defectType: String = "待识别",
    val defectLevel: String = "轻微",
    val locationDesc: String = "",
    val quantitativeInfo: String = "",
    val repairAdvice: String = "",
    val remarks: String = "",
    val aiConfidence: Int? = null,
    val aiSummary: String = "",
    val syncStatus: String = "pending",
)

data class ReportItem(
    val id: Long,
    val projectId: Long,
    val bridgeId: Long,
    val reportCode: String,
    val reportTitle: String,
    val reportStatus: String,
    val generatedDate: String,
    val summary: String,
    val maintenanceAdvice: List<String>,
    val conclusion: String = "",
    val reviewOpinion: String = "",
    val synced: Boolean = false,
)

data class NamedCount(
    val name: String,
    val count: Int,
)

data class ReportLevelStats(
    val severe: Int,
    val medium: Int,
    val minor: Int,
    val normal: Int,
)

data class ReportPreviewData(
    val report: ReportItem,
    val project: ProjectItem,
    val bridge: BridgeItem,
    val inspectedComponents: List<ComponentTask>,
    val totalComponents: Int,
    val collectedComponents: Int,
    val completedComponents: Int,
    val levelStats: ReportLevelStats,
    val defectTypeStats: List<NamedCount>,
    val inspectionBasis: List<String>,
    val conclusion: String,
    val reviewOpinion: String,
)
