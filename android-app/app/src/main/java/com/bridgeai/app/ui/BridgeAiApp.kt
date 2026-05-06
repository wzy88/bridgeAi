@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
)

package com.bridgeai.app.ui

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.bridgeai.app.data.BridgeItem
import com.bridgeai.app.data.ComponentMedia
import com.bridgeai.app.data.ComponentStatus
import com.bridgeai.app.data.ComponentTask
import com.bridgeai.app.data.MediaType
import com.bridgeai.app.data.ProjectItem
import com.bridgeai.app.data.QuickCaptureSegment
import com.bridgeai.app.data.ReportItem
import com.bridgeai.app.ui.theme.AppBackground
import com.bridgeai.app.ui.theme.BorderGray
import com.bridgeai.app.ui.theme.BridgeAiTheme
import com.bridgeai.app.ui.theme.BridgeBlue
import com.bridgeai.app.ui.theme.CardText
import com.bridgeai.app.ui.theme.DangerRed
import com.bridgeai.app.ui.theme.LightBlue
import com.bridgeai.app.ui.theme.SubtleText
import com.bridgeai.app.ui.theme.SuccessGreen
import com.bridgeai.app.ui.theme.WarningOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun BridgeAiApp() {
    BridgeAiTheme {
        val navController = rememberNavController()
        val viewModel: BridgeAiViewModel = viewModel()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: TabRoute.Tasks.route
        val topLevelRoutes = TabRoute.entries.map { it.route }
        val activeRole = viewModel.activeRole

        if (activeRole == null) {
            RoleSelectionScreen(
                onRoleSelected = viewModel::selectRole,
            )
            return@BridgeAiTheme
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = AppBackground,
            bottomBar = {
                if (currentRoute in topLevelRoutes) {
                    BottomBar(
                        currentRoute = currentRoute,
                        activeRole = activeRole,
                        onTabClick = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = TabRoute.Tasks.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(TabRoute.Tasks.route) {
                    if (activeRole == AppRole.Member) {
                        FieldQuickWorkbenchScreen(
                            viewModel = viewModel,
                            onOpenPackages = { projectId -> navController.navigate("quick-packages/$projectId") },
                        )
                    } else {
                        TasksScreen(
                            viewModel = viewModel,
                            onProjectClick = { navController.navigate("project/$it") },
                        )
                    }
                }
                composable(TabRoute.Bridges.route) {
                    if (activeRole == AppRole.Leader) {
                        LeaderMaterialPackagesScreen(
                            viewModel = viewModel,
                            onOpenPackages = { projectId -> navController.navigate("quick-packages/$projectId") },
                            onOpenProject = { projectId -> navController.navigate("project/$projectId") },
                        )
                    } else {
                        BridgesScreen(
                            viewModel = viewModel,
                            onBridgeClick = { navController.navigate("bridge/$it") },
                            onQuickInspect = { bridgeId ->
                                viewModel.projects.firstOrNull { it.bridgeId == bridgeId }?.let {
                                    navController.navigate("project/${it.id}")
                                }
                            },
                        )
                    }
                }
                composable(TabRoute.Reports.route) {
                    ReportsScreen(
                        viewModel = viewModel,
                        onReportClick = { navController.navigate("report/$it") },
                    )
                }
                composable(TabRoute.Profile.route) {
                    ProfileScreen(
                        viewModel = viewModel,
                        onSwitchRole = viewModel::resetRoleSelection,
                        onSyncClick = { navController.navigate("sync") },
                    )
                }
                composable("quick-packages/{projectId}") { backStackEntry ->
                    val projectId = backStackEntry.arguments?.getString("projectId")?.toLongOrNull()
                    if (projectId == null) {
                        InvalidRouteScreen(title = "物料包", message = "任务参数无效，请返回上一页重试。", onBack = { navController.popBackStack() })
                    } else {
                        QuickPackageListScreen(
                            viewModel = viewModel,
                            projectId = projectId,
                            onBack = { navController.popBackStack() },
                            onOpenSegment = { segmentId -> navController.navigate("quick-package/$segmentId") },
                        )
                    }
                }
                composable("quick-package/{segmentId}") { backStackEntry ->
                    val segmentId = backStackEntry.arguments?.getString("segmentId")?.toLongOrNull()
                    if (segmentId == null) {
                        InvalidRouteScreen(title = "物料包详情", message = "物料包参数无效，请返回上一页重试。", onBack = { navController.popBackStack() })
                    } else {
                        QuickPackageDetailScreen(
                            viewModel = viewModel,
                            segmentId = segmentId,
                            onBack = { navController.popBackStack() },
                            onSupplement = {
                                navController.popBackStack(TabRoute.Tasks.route, inclusive = false)
                            },
                        )
                    }
                }
                composable("project/{projectId}") { backStackEntry ->
                    val projectId = backStackEntry.arguments?.getString("projectId")?.toLongOrNull()
                    if (projectId == null) {
                        InvalidRouteScreen(title = "桥梁检测", message = "项目参数无效，请返回上一页重试。", onBack = { navController.popBackStack() })
                    } else {
                        ProjectDetailScreen(
                            viewModel = viewModel,
                            projectId = projectId,
                            onBack = { navController.popBackStack() },
                            onComponentClick = { componentId, hasMedia ->
                                navController.navigate(
                                    if (hasMedia) "component/$componentId/result" else "component/$componentId/capture"
                                )
                            },
                            onGenerateReport = { generatedReportId ->
                                navController.navigate("report/$generatedReportId")
                            },
                        )
                    }
                }
                composable("bridge/{bridgeId}") { backStackEntry ->
                    val bridgeId = backStackEntry.arguments?.getString("bridgeId")?.toLongOrNull()
                    if (bridgeId == null) {
                        InvalidRouteScreen(title = "桥梁详情", message = "桥梁参数无效，请返回上一页重试。", onBack = { navController.popBackStack() })
                    } else {
                        BridgeDetailScreen(
                            viewModel = viewModel,
                            bridgeId = bridgeId,
                            onBack = { navController.popBackStack() },
                            onStartInspect = { projectId -> navController.navigate("project/$projectId") },
                        )
                    }
                }
                composable("component/{componentId}/capture") { backStackEntry ->
                    val componentId = backStackEntry.arguments?.getString("componentId")?.toLongOrNull()
                    if (componentId == null) {
                        InvalidRouteScreen(title = "素材采集", message = "构件参数无效，请返回上一页重试。", onBack = { navController.popBackStack() })
                    } else {
                        val projectId = viewModel.getComponent(componentId)?.projectId
                        ComponentCaptureScreen(
                            viewModel = viewModel,
                            componentId = componentId,
                            onBack = {
                                if (projectId != null) {
                                    navController.navigate("project/$projectId") {
                                        popUpTo("project/$projectId") {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.popBackStack()
                                }
                            },
                            onSaved = {
                                navController.navigate("component/$componentId/result") {
                                    popUpTo("component/$componentId/capture") {
                                        inclusive = true
                                    }
                                }
                            },
                        )
                    }
                }
                composable("component/{componentId}/result") { backStackEntry ->
                    val componentId = backStackEntry.arguments?.getString("componentId")?.toLongOrNull()
                    if (componentId == null) {
                        InvalidRouteScreen(title = "AI结果", message = "构件参数无效，请返回上一页重试。", onBack = { navController.popBackStack() })
                    } else {
                        val projectId = viewModel.getComponent(componentId)?.projectId
                        ComponentResultScreen(
                            viewModel = viewModel,
                            componentId = componentId,
                            onBack = {
                                if (projectId != null) {
                                    navController.navigate("project/$projectId") {
                                        popUpTo("project/$projectId") {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.popBackStack()
                                }
                            },
                            onCompleted = { projectId ->
                                navController.navigate("project/$projectId") {
                                    popUpTo("project/$projectId") {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            },
                            onRetake = {
                                navController.navigate("component/$componentId/capture") {
                                    popUpTo("component/$componentId/result") {
                                        inclusive = true
                                    }
                                }
                            },
                        )
                    }
                }
                composable("component/{componentId}/quick") { backStackEntry ->
                    val componentId = backStackEntry.arguments?.getString("componentId")?.toLongOrNull()
                    if (componentId == null) {
                        InvalidRouteScreen(title = "极速采集", message = "构件参数无效，请返回上一页重试。", onBack = { navController.popBackStack() })
                    } else {
                        QuickCollectScreen(
                            viewModel = viewModel,
                            componentId = componentId,
                            onBack = { navController.popBackStack() },
                            onOpenStandard = { navController.navigate("component/$componentId/capture") },
                        )
                    }
                }
                composable("report/{reportId}") { backStackEntry ->
                    val reportId = backStackEntry.arguments?.getString("reportId")?.toLongOrNull()
                    if (reportId == null) {
                        InvalidRouteScreen(title = "报告预览", message = "报告参数无效，请返回上一页重试。", onBack = { navController.popBackStack() })
                    } else {
                        ReportDetailScreen(
                            viewModel = viewModel,
                            reportId = reportId,
                            onBack = { navController.popBackStack() },
                            onEditReport = { navController.navigate("report/$reportId/edit") },
                            onEditComponent = { componentId -> navController.navigate("component/$componentId/result") },
                        )
                    }
                }
                composable("report/{reportId}/edit") { backStackEntry ->
                    val reportId = backStackEntry.arguments?.getString("reportId")?.toLongOrNull()
                    if (reportId == null) {
                        InvalidRouteScreen(title = "编辑报告", message = "报告参数无效，请返回上一页重试。", onBack = { navController.popBackStack() })
                    } else {
                        ReportEditScreen(
                            viewModel = viewModel,
                            reportId = reportId,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
                composable("sync") {
                    SyncScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

private enum class TabRoute(val route: String, val label: String) {
    Tasks("tasks", "AI检测"),
    Bridges("bridges", "项目/桥梁"),
    Reports("reports", "报告中心"),
    Profile("profile", "我的"),
}

@Composable
private fun BottomBar(
    currentRoute: String,
    activeRole: AppRole,
    onTabClick: (String) -> Unit,
) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Surface(
        color = Color.White,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, BorderGray),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = bottomInset)
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val tabs = if (activeRole == AppRole.Member) {
                listOf(
                    Triple(TabRoute.Tasks.route, Icons.Default.CameraAlt, "采集"),
                    Triple(TabRoute.Profile.route, Icons.Default.Person, TabRoute.Profile.label),
                )
            } else listOf(
                Triple(TabRoute.Tasks.route, Icons.Default.CameraAlt, "任务"),
                Triple(TabRoute.Bridges.route, Icons.Default.Layers, "物料包"),
                Triple(TabRoute.Reports.route, Icons.Default.Description, "报告"),
                Triple(TabRoute.Profile.route, Icons.Default.Person, TabRoute.Profile.label),
            )
            tabs.forEach { (route, icon, label) ->
                val selected = currentRoute == route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clickable { onTabClick(route) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) BridgeBlue else SubtleText,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        color = if (selected) BridgeBlue else SubtleText,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleSelectionScreen(
    onRoleSelected: (AppRole) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(title = "选择体验身份", compact = true)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionCard {
                    Text("BridgeAI", color = CardText, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "先选择身份，App 会自动进入对应的默认工作方式。",
                        color = SubtleText,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                    )
                }
            }
            item {
                RoleOptionCard(
                    title = "组长",
                    subtitle = "标准检测流程，适合分配任务、复核结果、生成报告。",
                    primaryAction = "进入标准检测",
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    accent = BridgeBlue,
                    onClick = { onRoleSelected(AppRole.Leader) },
                )
            }
            item {
                RoleOptionCard(
                    title = "组员",
                    subtitle = "极速采集流程，默认拍照、分组、语音备注，减少填表。",
                    primaryAction = "进入极速采集",
                    icon = Icons.Default.Mic,
                    accent = SuccessGreen,
                    onClick = { onRoleSelected(AppRole.Member) },
                )
            }
            item {
                CompactTipStrip("当前是演示身份选择；后续正式版可由账号权限自动进入对应工作台。")
            }
        }
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    subtitle: String,
    primaryAction: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
) {
    SectionCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(30.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = CardText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = SubtleText, fontSize = 13.sp, lineHeight = 20.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent),
        ) {
            Text(primaryAction)
        }
    }
}

@Composable
private fun BlueTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actionSlotWidth: Int = 40,
    compact: Boolean = false,
    rightContent: @Composable (() -> Unit)? = null,
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val barHeight = if (compact) 42.dp else 56.dp
    val horizontalPadding = if (compact) 8.dp else 12.dp
    val titleFontSize = if (compact) 16.sp else 18.sp
    val sideSlotWidth = if (compact) 32.dp else 40.dp
    val trailingSlotWidth = if (compact) actionSlotWidth.coerceAtMost(36).dp else actionSlotWidth.dp
    val backgroundBrush = if (compact) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF3E74F7),
                Color(0xFF2B62EF),
            ),
        )
    } else {
        Brush.verticalGradient(listOf(BridgeBlue, BridgeBlue))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundBrush),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topInset)
                .height(barHeight)
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.width(sideSlotWidth), contentAlignment = Alignment.CenterStart) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(if (compact) 30.dp else 40.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White,
                            modifier = Modifier.size(if (compact) 18.dp else 24.dp),
                        )
                    }
                }
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = if (compact) FontWeight.SemiBold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                fontSize = titleFontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box(modifier = Modifier.width(trailingSlotWidth), contentAlignment = Alignment.CenterEnd) {
                rightContent?.invoke()
            }
        }
        if (compact) {
            HorizontalDivider(
                modifier = Modifier.align(Alignment.BottomCenter),
                thickness = 0.6.dp,
                color = Color.White.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.8.dp),
        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.68f)),
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun StatusChip(text: String, color: Color, background: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text = text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
private fun InvalidRouteScreen(
    title: String,
    message: String,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(title = title, onBack = onBack)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            SectionCard {
                Text("页面暂时无法打开", color = CardText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(message, color = SubtleText, lineHeight = 22.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BridgeBlue),
                ) {
                    Text("返回上一页")
                }
            }
        }
    }
}

@Composable
private fun TasksScreen(
    viewModel: BridgeAiViewModel,
    onProjectClick: (Long) -> Unit,
) {
    var statusFilter by rememberSaveable { mutableStateOf("全部") }
    val filteredProjects = viewModel.projects.filter { project ->
        when (statusFilter) {
            "检测中" -> project.projectStatus == "in_progress"
            "待开始" -> project.projectStatus == "draft"
            "已完成" -> project.projectStatus == "completed"
            else -> true
        }
    }
    val activeProjects = viewModel.projects.count { it.projectStatus == "in_progress" }
    val pendingPackages = viewModel.quickCaptureSegments.count { !it.voided && it.statusText != "可入报告" }
    val draftReports = viewModel.reports.count { it.reportStatus == "draft" }
    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(title = "任务", compact = true)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionCard {
                    Text("组长工作台", color = CardText, fontWeight = FontWeight.Bold, fontSize = 21.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("看任务进度、人员采集状态和报告产出，不直接承担现场采集。", color = SubtleText, fontSize = 13.sp, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    SummaryMetricGrid(
                        metrics = listOf(
                            "在检项目" to activeProjects.toString(),
                            "待整理包" to pendingPackages.toString(),
                            "报告草稿" to draftReports.toString(),
                        ),
                    )
                }
            }
            stickyHeader {
                StickyListHeaderCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("共 ${filteredProjects.size} 个项目", color = CardText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("状态筛选", color = SubtleText, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FilterRow(
                        title = "",
                        options = listOf("全部", "检测中", "待开始", "已完成"),
                        selected = statusFilter,
                        onSelected = { statusFilter = it },
                    )
                }
            }
            if (filteredProjects.isEmpty()) {
                item {
                    SectionCard {
                        Text("当前筛选下暂无项目", color = CardText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("可以切换项目状态，或等待新的检测任务下发。", color = SubtleText, fontSize = 13.sp)
                    }
                }
            }
            items(filteredProjects) { project ->
                val bridge = viewModel.getBridge(project.bridgeId)
                val progress = viewModel.projectProgress(project.id)
                val projectPackages = viewModel.getQuickCaptureSegments(project.id)
                val teamMembers = viewModel.getProjectMembers(project.id)
                SectionCard(
                    modifier = Modifier.clickable { onProjectClick(project.id) },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                project.projectName,
                                fontWeight = FontWeight.Bold,
                                color = CardText,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${bridge?.bridgeName ?: ""} · ${project.projectType}",
                                color = SubtleText,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        StatusChip(
                            text = when (project.projectStatus) {
                                "completed" -> "已完成"
                                "in_progress" -> "检测中"
                                else -> "待开始"
                            },
                            color = when (project.projectStatus) {
                                "completed" -> SuccessGreen
                                "in_progress" -> WarningOrange
                                else -> SubtleText
                            },
                            background = when (project.projectStatus) {
                                "completed" -> SuccessGreen.copy(alpha = 0.12f)
                                "in_progress" -> WarningOrange.copy(alpha = 0.12f)
                                else -> SubtleText.copy(alpha = 0.12f)
                            },
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    SummaryMetricGrid(
                        metrics = listOf(
                            "总构件" to progress.total.toString(),
                            "已采集" to progress.collected.toString(),
                            "物料包" to projectPackages.count { !it.voided }.toString(),
                        ),
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(BorderGray),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (progress.total == 0) 0f else progress.completed.toFloat() / progress.total.toFloat())
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(BridgeBlue),
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "负责人：${project.leaderName} · 执行人员 ${teamMembers.count { it.roleLabel.contains("检测") }} 人 · ${project.startDate} - ${project.endDate}",
                        color = SubtleText,
                        fontSize = 12.sp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(LightBlue.copy(alpha = 0.52f))
                            .clickable { onProjectClick(project.id) }
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                when (project.projectStatus) {
                                    "completed" -> "查看检测结果"
                                    "in_progress" -> "查看进度与分配"
                                    else -> "分配任务"
                                },
                                color = BridgeBlue,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = BridgeBlue, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldQuickWorkbenchScreen(
    viewModel: BridgeAiViewModel,
    onOpenPackages: (Long) -> Unit,
) {
    val context = LocalContext.current
    val assignedProjects = viewModel.components
        .filter { it.responsibleUserId == viewModel.currentUser.id }
        .mapNotNull { viewModel.getProject(it.projectId) }
        .distinctBy { it.id }
    val availableProjects = assignedProjects.ifEmpty { viewModel.projects.take(1) }
    var selectedProjectId by rememberSaveable { mutableStateOf<Long?>(null) }
    val selectedProject = selectedProjectId?.let { id -> availableProjects.firstOrNull { it.id == id } }
    val selectedBridge = selectedProject?.let { viewModel.getBridge(it.bridgeId) }
    val pendingMedia = remember { mutableStateListOf<ComponentMedia>() }
    var voiceText by rememberSaveable(selectedProjectId ?: -1L) { mutableStateOf("") }
    var extractedText by rememberSaveable(selectedProjectId ?: -1L) { mutableStateOf("") }
    var reportText by rememberSaveable(selectedProjectId ?: -1L) { mutableStateOf("") }
    var voiceRound by rememberSaveable(selectedProjectId ?: -1L) { mutableStateOf(0) }
    val savedSegments = selectedProject?.let { viewModel.getQuickCaptureSegments(it.id) }.orEmpty()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap: Bitmap? ->
        val fileUri = bitmap?.let { saveBitmapToCache(context, it) }
        if (fileUri != null) {
            pendingMedia.add(ComponentMedia("field-photo-${System.nanoTime()}", fileUri.toString(), MediaType.Photo))
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 12),
    ) { uris ->
        if (uris.isNotEmpty()) {
            pendingMedia.addAll(uris.map { ComponentMedia("field-import-${System.nanoTime()}-${it.hashCode()}", it.toString(), MediaType.Photo) })
        }
    }

    if (selectedProject == null || selectedBridge == null) {
        QuickProjectPickerScreen(
            viewModel = viewModel,
            projects = availableProjects,
            onSelectProject = { project ->
                selectedProjectId = project.id
                pendingMedia.clear()
                voiceText = ""
                extractedText = ""
                reportText = ""
                voiceRound = 0
            },
        )
        return
    }

    val addVoiceNote = {
        voiceRound += 1
        voiceText = fieldVoiceTranscript(selectedBridge.bridgeName, selectedProject.projectType, voiceRound)
        extractedText = fieldExtractedText(selectedBridge.bridgeName, voiceRound)
        reportText = fieldReportText(selectedBridge.bridgeName, voiceRound)
        Toast.makeText(context, "已生成一段模拟语音识别", Toast.LENGTH_SHORT).show()
    }
    val saveCurrentPackage = {
        val saved = viewModel.saveQuickCaptureSegment(
            projectId = selectedProject.id,
            mediaItems = pendingMedia.toList(),
            voiceText = voiceText,
            extractedText = extractedText.ifBlank { "AI待整理：照片与语音已绑定，等待联网或人工触发后识别位置、病害和数量。" },
            reportText = reportText.ifBlank { "本段素材已完成离线留存，待AI整理后进入报告候选记录。" },
        )
        if (saved) {
            pendingMedia.clear()
            voiceText = ""
            extractedText = ""
            reportText = ""
            Toast.makeText(context, "本包已保存，可以继续采下一包", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "请至少拍照或补充语音备注", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(title = "采集", compact = true)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(selectedBridge.bridgeName, color = CardText, fontWeight = FontWeight.Bold, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${selectedProject.projectType} · 今天只采当前桥梁", color = SubtleText, fontSize = 13.sp)
                        }
                        TextButton(onClick = {
                            selectedProjectId = null
                            pendingMedia.clear()
                            voiceText = ""
                            extractedText = ""
                            reportText = ""
                            voiceRound = 0
                        }) {
                            Text("换任务")
                        }
                    }
                }
            }
            item {
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("新建物料包", color = CardText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("拍照和说话是主动作；系统会先暂存，保存只是明确完成这一包。", color = SubtleText, fontSize = 13.sp, lineHeight = 20.sp)
                        }
                        StatusChip(
                            if (pendingMedia.isEmpty() && voiceText.isBlank()) "空包" else "${pendingMedia.size}图 · ${if (voiceText.isBlank()) 0 else 1}语音",
                            BridgeBlue,
                            LightBlue.copy(alpha = 0.7f),
                        )
                    }
                    if (pendingMedia.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(pendingMedia, key = { it.id }) { media ->
                                QuickMediaThumb(
                                    media = media,
                                    label = "P${pendingMedia.indexOf(media) + 1}",
                                    onRemove = { pendingMedia.remove(media) },
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    DialogSectionTitle("语音文字")
                    DialogHighlightBlock(voiceText.ifBlank { "还没有语音。现场人员可以直接说：照片编号、位置、看到的问题、补充判断。系统先完整保存原话。" })
                    if (extractedText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        DialogSectionTitle("AI预整理")
                        DialogHighlightBlock(extractedText, accent = true)
                    }
                }
            }
            item {
                SectionCard(
                    modifier = Modifier.clickable { onOpenPackages(selectedProject.id) },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("已保存物料包", color = CardText, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("查看今天采回来的照片和语音包，不在这里改原包。", color = SubtleText, fontSize = 13.sp)
                        }
                        StatusChip("${savedSegments.count { !it.voided }} 包", BridgeBlue, LightBlue.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = BridgeBlue)
                    }
                    if (savedSegments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(savedSegments.first().extractedText, color = SubtleText, fontSize = 12.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        QuickCollectActionDock(
            mediaCount = pendingMedia.size,
            hasVoice = voiceText.isNotBlank(),
            canSave = pendingMedia.isNotEmpty() || voiceText.isNotBlank(),
            onCamera = { cameraLauncher.launch(null) },
            onPickImage = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onVoice = addVoiceNote,
            onSave = saveCurrentPackage,
        )
    }
}

@Composable
private fun QuickProjectPickerScreen(
    viewModel: BridgeAiViewModel,
    projects: List<ProjectItem>,
    onSelectProject: (ProjectItem) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(title = "采集", compact = true)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionCard {
                    Text("今天要去哪座桥？", color = CardText, fontWeight = FontWeight.Bold, fontSize = 21.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("先选当前现场。到桥上以后，只需要拍照、说话、保存物料包。", color = SubtleText, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
            items(projects) { project ->
                val bridge = viewModel.getBridge(project.bridgeId)
                val packageCount = viewModel.getQuickCaptureSegments(project.id).count { !it.voided }
                SectionCard(modifier = Modifier.clickable { onSelectProject(project) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BridgeBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BridgeBlue)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(bridge?.bridgeName ?: project.projectName, color = CardText, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${project.projectType} · ${project.startDate} 至 ${project.endDate}", color = SubtleText, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        StatusChip(if (packageCount > 0) "${packageCount}包" else "开始", BridgeBlue, LightBlue.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickCaptureActionButton(
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(tint.copy(alpha = 0.1f))
            .border(1.dp, tint.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.height(7.dp))
            Text(label, color = CardText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun QuickCollectActionDock(
    mediaCount: Int,
    hasVoice: Boolean,
    canSave: Boolean,
    onCamera: () -> Unit,
    onPickImage: () -> Unit,
    onVoice: () -> Unit,
    onSave: () -> Unit,
) {
    Surface(
        color = Color.White,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.8f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("本包：${mediaCount}张照片 · ${if (hasVoice) "已说话" else "未说话"}", color = SubtleText, fontSize = 12.sp)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onPickImage) {
                    Text("选照片")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onCamera,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("拍照")
                }
                Button(
                    onClick = onVoice,
                    modifier = Modifier.weight(1.18f).height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BridgeBlue),
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (hasVoice) "继续说" else "说话")
                }
                OutlinedButton(
                    onClick = onSave,
                    enabled = canSave,
                    modifier = Modifier.width(72.dp).height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, if (canSave) SuccessGreen.copy(alpha = 0.5f) else BorderGray),
                ) {
                    Icon(Icons.Default.Check, contentDescription = "保存", tint = if (canSave) SuccessGreen else SubtleText)
                }
            }
        }
    }
}

@Composable
private fun QuickMediaThumb(
    media: ComponentMedia,
    label: String,
    onRemove: (() -> Unit)? = null,
) {
    Box {
        AsyncImage(
            model = media.uri,
            contentDescription = null,
            modifier = Modifier
                .size(78.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop,
        )
        StatusChip(
            text = label,
            color = Color.White,
            background = Color.Black.copy(alpha = 0.48f),
        )
        if (onRemove != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.58f))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center,
            ) {
                Text("×", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuickPackageListScreen(
    viewModel: BridgeAiViewModel,
    projectId: Long,
    onBack: () -> Unit,
    onOpenSegment: (Long) -> Unit,
) {
    val project = viewModel.getProject(projectId)
    val bridge = project?.let { viewModel.getBridge(it.bridgeId) }
    if (project == null || bridge == null) {
        InvalidRouteScreen(title = "物料包", message = "任务数据不存在或已被移除。", onBack = onBack)
        return
    }
    val segments = viewModel.getQuickCaptureSegments(projectId)
    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(title = "物料包", onBack = onBack)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionCard {
                    Text(bridge.bridgeName, color = CardText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("原始物料包只做查看和作废；补充内容请新采一包，后续由 AI 和组长合并。", color = SubtleText, fontSize = 13.sp, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    SummaryMetricGrid(
                        metrics = listOf(
                            "全部包" to segments.size.toString(),
                            "有效包" to segments.count { !it.voided }.toString(),
                            "待整理" to segments.count { !it.voided && it.statusText == "AI待整理" }.toString(),
                        ),
                    )
                }
            }
            if (segments.isEmpty()) {
                item {
                    CompactTipStrip("还没有保存物料包。返回采集页，拍照或说一段话后点击保存即可生成。")
                }
            } else {
                items(segments, key = { it.id }) { segment ->
                    QuickPackageListCard(
                        segment = segment,
                        onClick = { onOpenSegment(segment.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickPackageListCard(
    segment: QuickCaptureSegment,
    onClick: () -> Unit,
) {
    SectionCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("物料包 ${segment.sequence}", color = CardText, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(segment.createdLabel, color = SubtleText, fontSize = 13.sp)
            }
            StatusChip(
                text = segment.statusText,
                color = if (segment.voided) SubtleText else WarningOrange,
                background = if (segment.voided) SubtleText.copy(alpha = 0.12f) else WarningOrange.copy(alpha = 0.12f),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip("照片 ${segment.mediaItems.size}", BridgeBlue, LightBlue.copy(alpha = 0.7f))
            StatusChip("语音 1 段", SuccessGreen, SuccessGreen.copy(alpha = 0.12f))
            StatusChip(if (segment.voided) "不参与合并" else "等待合并", SubtleText, SubtleText.copy(alpha = 0.12f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            segment.extractedText,
            color = CardText,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun QuickPackageDetailScreen(
    viewModel: BridgeAiViewModel,
    segmentId: Long,
    onBack: () -> Unit,
    onSupplement: () -> Unit,
) {
    val context = LocalContext.current
    val segment = viewModel.getQuickCaptureSegment(segmentId)
    if (segment == null) {
        InvalidRouteScreen(title = "物料包详情", message = "物料包不存在或已被移除。", onBack = onBack)
        return
    }
    val bridge = viewModel.getBridge(segment.bridgeId)
    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(title = "物料包 ${segment.sequence}", onBack = onBack)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(bridge?.bridgeName ?: "采集任务", color = CardText, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(segment.createdLabel, color = SubtleText, fontSize = 13.sp)
                        }
                        StatusChip(
                            text = segment.statusText,
                            color = if (segment.voided) SubtleText else WarningOrange,
                            background = if (segment.voided) SubtleText.copy(alpha = 0.12f) else WarningOrange.copy(alpha = 0.12f),
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("原始包不直接编辑。需要补充时新建一个补充包，后续自动或人工归并。", color = SubtleText, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
            if (segment.mediaItems.isNotEmpty()) {
                item {
                    SectionCard {
                        Text("照片", color = CardText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(segment.mediaItems, key = { it.id }) { media ->
                                QuickMediaThumb(media = media, label = "P${segment.mediaItems.indexOf(media) + 1}")
                            }
                        }
                    }
                }
            }
            item {
                SectionCard {
                    DialogSectionTitle("原始口述")
                    DialogHighlightBlock(segment.voiceText.ifBlank { "本包没有语音内容。" })
                    Spacer(modifier = Modifier.height(14.dp))
                    DialogSectionTitle("AI预整理")
                    DialogHighlightBlock(segment.extractedText, accent = true)
                    Spacer(modifier = Modifier.height(14.dp))
                    DialogSectionTitle("报告候选描述")
                    DialogHighlightBlock(segment.reportText, accent = true)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            viewModel.voidQuickCaptureSegment(segment.id)
                            Toast.makeText(context, "已作废此物料包，原始记录仍保留", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        enabled = !segment.voided,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("作废此包")
                    }
                    Button(
                        onClick = {
                            Toast.makeText(context, "回到采集页，新建一包作为补充", Toast.LENGTH_SHORT).show()
                            onSupplement()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    ) {
                        Text("补充一包")
                    }
                }
            }
        }
    }
}

private fun fieldVoiceTranscript(
    bridgeName: String,
    projectType: String,
    round: Int,
): String = when (round % 3) {
    1 -> "照片编号这一组先记为A$round，$bridgeName 右幅桥面靠近伸缩缝的位置有破损，边缘有松散，车辆经过的时候有明显跳车，建议重点看一下。"
    2 -> "这一段是在$bridgeName 第二跨下面拍的，靠近墩柱位置有渗水和泛白，裂缝不算特别宽，但是长度比较明显，我又补了几张远景方便定位。"
    else -> "$projectType 现场补充一组照片，这里疑似支座周边有老化和积水，具体构件编号我不确定，后面让AI根据照片和定位一起判断。"
}

private fun fieldExtractedText(
    bridgeName: String,
    round: Int,
): String = when (round % 3) {
    1 -> "AI预整理：$bridgeName，疑似桥面铺装/伸缩缝附近破损；关键词：破损、松散、跳车；位置需要结合照片编号A${round}确认。"
    2 -> "AI预整理：$bridgeName，疑似墩柱或梁底附近渗水泛白，并伴随纵向裂缝；建议进入待确认列表后补充构件编号。"
    else -> "AI预整理：$bridgeName，疑似支座区域老化、积水；当前口述位置不完整，建议通过照片远景和GPS补全定位。"
}

private fun fieldReportText(
    bridgeName: String,
    round: Int,
): String = when (round % 3) {
    1 -> "$bridgeName 右幅伸缩缝附近桥面铺装存在局部破损及松散现象，现场反馈车辆通行存在跳车感，建议列入后续重点复核。"
    2 -> "$bridgeName 第二跨下部结构附近可见渗水、泛白及裂缝迹象，建议结合近景与远景照片进一步确认病害范围。"
    else -> "$bridgeName 支座周边疑似存在老化及积水情况，需结合现场照片进一步核定构件位置与处置优先级。"
}

@Composable
private fun LeaderMaterialPackagesScreen(
    viewModel: BridgeAiViewModel,
    onOpenPackages: (Long) -> Unit,
    onOpenProject: (Long) -> Unit,
) {
    val context = LocalContext.current
    var packageFilter by rememberSaveable { mutableStateOf("全部") }
    val allSegments = viewModel.getQuickCaptureSegments()
    val filteredSegments = allSegments.filter { segment ->
        when (packageFilter) {
            "待整理" -> !segment.voided && segment.statusText == "AI待整理"
            "疑似合并" -> !segment.voided && segment.statusText == "疑似合并"
            "需补采" -> !segment.voided && segment.statusText == "需补采"
            "可入报告" -> !segment.voided && segment.statusText == "可入报告"
            "已作废" -> segment.voided
            else -> true
        }
    }
    val grouped = filteredSegments.groupBy { it.projectId }
    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(title = "物料包", compact = true)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionCard {
                    Text("采集物料工作台", color = CardText, fontWeight = FontWeight.Bold, fontSize = 21.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("一级只看项目级结论。点进项目后再看每个原始物料包。", color = SubtleText, fontSize = 13.sp, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    SummaryMetricGrid(
                        metrics = listOf(
                            "原始包" to allSegments.size.toString(),
                            "疑似合并" to allSegments.count { !it.voided && it.statusText == "疑似合并" }.toString(),
                            "需补采" to allSegments.count { !it.voided && it.statusText == "需补采" }.toString(),
                        ),
                    )
                }
            }
            stickyHeader {
                StickyListHeaderCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("共 ${grouped.size} 个项目", color = CardText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("处理状态", color = SubtleText, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FilterRow(
                        title = "",
                        options = listOf("全部", "待整理", "疑似合并", "需补采", "可入报告", "已作废"),
                        selected = packageFilter,
                        onSelected = { packageFilter = it },
                    )
                }
            }
            if (grouped.isEmpty()) {
                item {
                    SectionCard {
                        Text("当前筛选下暂无物料包", color = CardText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("可以切换状态，或等待组员完成新的现场采集。", color = SubtleText, fontSize = 13.sp)
                    }
                }
            }
            grouped.forEach { (projectId, segments) ->
                item {
                    LeaderPackageProjectCard(
                        viewModel = viewModel,
                        projectId = projectId,
                        segments = segments,
                        onOpenPackages = { onOpenPackages(projectId) },
                        onOpenProject = { onOpenProject(projectId) },
                        onConfirmMerge = {
                            Toast.makeText(context, "已标记为同一问题组，后续进入报告候选", Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderPackageProjectCard(
    viewModel: BridgeAiViewModel,
    projectId: Long,
    segments: List<QuickCaptureSegment>,
    onOpenPackages: () -> Unit,
    onOpenProject: () -> Unit,
    onConfirmMerge: () -> Unit,
) {
    val project = viewModel.getProject(projectId)
    val bridge = project?.let { viewModel.getBridge(it.bridgeId) }
    val validSegments = segments.filter { !it.voided }
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(bridge?.bridgeName ?: project?.projectName ?: "未知任务", color = CardText, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(project?.projectType ?: "采集任务", color = SubtleText, fontSize = 13.sp)
            }
            StatusChip("${validSegments.size} 包", BridgeBlue, LightBlue.copy(alpha = 0.7f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip("照片 ${validSegments.sumOf { it.mediaItems.size }}", BridgeBlue, LightBlue.copy(alpha = 0.7f))
            StatusChip("疑似合并 ${validSegments.count { it.statusText == "疑似合并" }}", WarningOrange, WarningOrange.copy(alpha = 0.12f))
            StatusChip("可入报告 ${validSegments.count { it.statusText == "可入报告" }}", SuccessGreen, SuccessGreen.copy(alpha = 0.12f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            validSegments.firstOrNull()?.extractedText ?: "暂无有效物料包。",
            color = CardText,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onOpenPackages,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("看全部包")
            }
            Button(
                onClick = onConfirmMerge,
                enabled = validSegments.size >= 2,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BridgeBlue),
            ) {
                Text("确认合并")
            }
        }
    }
}

@Composable
private fun LeaderPackageSegmentRow(
    segment: QuickCaptureSegment,
    onClick: () -> Unit,
) {
    SectionCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (segment.voided) BorderGray else LightBlue.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = if (segment.voided) SubtleText else BridgeBlue)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("物料包 ${segment.sequence} · ${segment.createdLabel}", color = CardText, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(segment.extractedText, color = SubtleText, fontSize = 12.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.width(8.dp))
            StatusChip(
                text = segment.statusText,
                color = when {
                    segment.voided -> SubtleText
                    segment.statusText == "可入报告" -> SuccessGreen
                    segment.statusText == "需补采" -> DangerRed
                    else -> WarningOrange
                },
                background = when {
                    segment.voided -> SubtleText.copy(alpha = 0.12f)
                    segment.statusText == "可入报告" -> SuccessGreen.copy(alpha = 0.12f)
                    segment.statusText == "需补采" -> DangerRed.copy(alpha = 0.1f)
                    else -> WarningOrange.copy(alpha = 0.12f)
                },
            )
        }
    }
}

@Composable
private fun BridgesScreen(
    viewModel: BridgeAiViewModel,
    onBridgeClick: (Long) -> Unit,
    onQuickInspect: (Long) -> Unit,
) {
    var keyword by rememberSaveable { mutableStateOf("") }
    var bridgeFilter by rememberSaveable { mutableStateOf("全部") }
    val filtered = viewModel.bridges.filter {
        (keyword.isBlank() || it.bridgeName.contains(keyword) || it.bridgeCode.contains(keyword)) &&
            when (bridgeFilter) {
                "检测中" -> it.statusText == "检测中"
                "待检测" -> it.statusText == "待检测"
                "已完成" -> it.statusText == "已完成"
                else -> true
            }
    }
    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(title = "项目/桥梁", compact = true)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                CompactTipStrip("先搜桥，再进项目；查档案看详情，开始作业优先进入在检项目。")
            }
            stickyHeader {
                StickyListHeaderCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("共 ${filtered.size} 座桥梁", color = CardText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("桥梁筛选", color = SubtleText, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = keyword,
                        onValueChange = { keyword = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp),
                        singleLine = true,
                        placeholder = { Text("搜索桥梁名称/编码", fontSize = 13.sp) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        shape = RoundedCornerShape(12.dp),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FilterRow(
                        title = "",
                        options = listOf("全部", "检测中", "待检测", "已完成"),
                        selected = bridgeFilter,
                        onSelected = { bridgeFilter = it },
                    )
                }
            }
            if (filtered.isEmpty()) {
                item {
                    SectionCard {
                        Text("未找到符合条件的桥梁", color = CardText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("可以调整搜索关键词或切换桥梁状态筛选。", color = SubtleText, fontSize = 13.sp)
                    }
                }
            }
            items(filtered) { bridge ->
                SectionCard(modifier = Modifier.clickable { onBridgeClick(bridge.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                bridge.bridgeName,
                                fontWeight = FontWeight.Bold,
                                color = CardText,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("编码：${bridge.bridgeCode}", color = SubtleText, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${bridge.structureType} · 建成 ${bridge.constructionYear} 年", color = CardText, fontSize = 13.sp)
                        }
                        StatusChip(
                            text = bridge.statusText,
                            color = when (bridge.statusText) {
                                "检测中" -> WarningOrange
                                "已完成" -> SuccessGreen
                                else -> SubtleText
                            },
                            background = when (bridge.statusText) {
                                "检测中" -> WarningOrange.copy(alpha = 0.12f)
                                "已完成" -> SuccessGreen.copy(alpha = 0.12f)
                                else -> SubtleText.copy(alpha = 0.12f)
                            },
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { onQuickInspect(bridge.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BridgeBlue),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(if (bridge.statusText == "待检测") "开始检测" else "查看在检项目")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportsScreen(
    viewModel: BridgeAiViewModel,
    onReportClick: (Long) -> Unit,
) {
    var reportFilter by rememberSaveable { mutableStateOf("全部") }
    val filteredReports = viewModel.reports.filter { report ->
        when (reportFilter) {
            "草稿" -> report.reportStatus == "draft"
            "已完成" -> report.reportStatus == "completed"
            "已上报" -> report.reportStatus == "reported"
            else -> true
        }
    }
    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(title = "报告中心", compact = true)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionCard {
                    Text("报告中心", color = CardText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(5.dp))
                    Text("这里只看报告状态和最终产出，编辑动作进入详情页完成。", color = SubtleText, fontSize = 13.sp, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    SummaryMetricGrid(
                        metrics = listOf(
                            "草稿" to viewModel.reports.count { it.reportStatus == "draft" }.toString(),
                            "已完成" to viewModel.reports.count { it.reportStatus == "completed" }.toString(),
                            "已上报" to viewModel.reports.count { it.reportStatus == "reported" }.toString(),
                        ),
                    )
                }
            }
            stickyHeader {
                StickyListHeaderCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("共 ${filteredReports.size} 份报告", color = CardText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("状态筛选", color = SubtleText, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FilterRow(
                        title = "",
                        options = listOf("全部", "草稿", "已完成", "已上报"),
                        selected = reportFilter,
                        onSelected = { reportFilter = it },
                    )
                }
            }
            if (filteredReports.isEmpty()) {
                item {
                    SectionCard {
                        Text("当前筛选下暂无报告", color = CardText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("可以切换报告状态，或先从项目页生成新的草稿报告。", color = SubtleText, fontSize = 13.sp)
                    }
                }
            }
            items(filteredReports) { report ->
                val bridge = viewModel.getBridge(report.bridgeId)
                SectionCard(modifier = Modifier.clickable { onReportClick(report.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LightBlue),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = BridgeBlue)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                report.reportTitle,
                                fontWeight = FontWeight.Bold,
                                color = CardText,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${bridge?.bridgeName ?: ""} · ${report.generatedDate}", color = SubtleText, fontSize = 13.sp)
                        }
                        StatusChip(
                            text = when (report.reportStatus) {
                                "reported" -> "已上报"
                                "completed" -> "已完成"
                                else -> "草稿"
                            },
                            color = when (report.reportStatus) {
                                "reported" -> BridgeBlue
                                "completed" -> SuccessGreen
                                else -> SubtleText
                            },
                            background = when (report.reportStatus) {
                                "reported" -> BridgeBlue.copy(alpha = 0.12f)
                                "completed" -> SuccessGreen.copy(alpha = 0.12f)
                                else -> SubtleText.copy(alpha = 0.12f)
                            },
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        report.summary,
                        color = CardText,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    viewModel: BridgeAiViewModel,
    onSwitchRole: () -> Unit,
    onSyncClick: () -> Unit,
) {
    val stats = viewModel.stats()
    val myProjects = viewModel.projects
        .filter { project -> viewModel.getProjectMembers(project.id).any { member -> member.id == viewModel.currentUser.id } }
        .take(4)
    val recentReports = viewModel.reports
        .sortedByDescending { it.generatedDate }
        .take(3)
    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(title = "我的", compact = true)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(BridgeBlue),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(viewModel.currentUser.realName, fontWeight = FontWeight.Bold, color = CardText, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("工号：${viewModel.currentUser.userCode}", color = SubtleText, fontSize = 13.sp)
                            Text(viewModel.currentUser.company, color = SubtleText, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            StatusChip(
                                text = if (viewModel.activeRole == AppRole.Member) "组员 · 极速采集" else "组长 · 标准流程",
                                color = if (viewModel.activeRole == AppRole.Member) SuccessGreen else BridgeBlue,
                                background = if (viewModel.activeRole == AppRole.Member) SuccessGreen.copy(alpha = 0.12f) else BridgeBlue.copy(alpha = 0.12f),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    SummaryMetricGrid(
                        metrics = listOf(
                            "项目数" to stats.completedProjects.toString(),
                            "病害识别" to stats.identifiedDefects.toString(),
                            "报告数" to stats.reportsCount.toString(),
                        ),
                    )
                }
            }
            item {
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("体验身份", fontWeight = FontWeight.Bold, color = CardText)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (viewModel.activeRole == AppRole.Member) {
                                    "当前默认进入极速采集；切换身份可体验组长的标准检测流程。"
                                } else {
                                    "当前默认进入标准检测；切换身份可体验组员的极速采集流程。"
                                },
                                color = SubtleText,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                            )
                        }
                        OutlinedButton(
                            onClick = onSwitchRole,
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("切换")
                        }
                    }
                }
            }
            item {
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("离线模式", fontWeight = FontWeight.Bold, color = CardText)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (viewModel.offlineMode) "已开启，检测数据先保存在本地" else "已关闭，检测后可直接同步",
                                color = SubtleText,
                                fontSize = 13.sp,
                            )
                        }
                        Switch(checked = viewModel.offlineMode, onCheckedChange = { viewModel.toggleOfflineMode() })
                    }
                }
            }
            item {
                MenuRow(icon = Icons.Default.CloudSync, title = "数据同步", subtitle = "${viewModel.pendingSyncComponents().size} 条待同步", onClick = onSyncClick)
            }
            item {
                SectionCard {
                    Text("我参与的项目", fontWeight = FontWeight.Bold, color = CardText)
                    Spacer(modifier = Modifier.height(10.dp))
                    myProjects.forEachIndexed { index, project ->
                        Text(project.projectName, color = CardText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${project.projectType} · ${project.startDate} - ${project.endDate}", color = SubtleText, fontSize = 12.sp)
                        if (index != myProjects.lastIndex) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BorderGray)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
            item {
                SectionCard {
                    Text("最近报告", fontWeight = FontWeight.Bold, color = CardText)
                    Spacer(modifier = Modifier.height(10.dp))
                    recentReports.forEachIndexed { index, report ->
                        Text(report.reportTitle, color = CardText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${report.generatedDate} · ${report.summary}", color = SubtleText, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
                        if (index != recentReports.lastIndex) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BorderGray)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
            item {
                MenuRow(icon = Icons.Default.Description, title = "帮助中心", subtitle = "查看检测流程和常见问题")
            }
            item {
                MenuRow(icon = Icons.Default.Share, title = "关于我们", subtitle = "BridgeAI v0.1 设计还原版")
            }
        }
    }
}

@Composable
private fun ProjectDetailScreen(
    viewModel: BridgeAiViewModel,
    projectId: Long,
    onBack: () -> Unit,
    onComponentClick: (Long, Boolean) -> Unit,
    onGenerateReport: (Long) -> Unit,
) {
    val context = LocalContext.current
    val project = viewModel.getProject(projectId)
    if (project == null) {
        InvalidRouteScreen(title = "桥梁检测", message = "项目数据不存在或已被移除。", onBack = onBack)
        return
    }
    val bridge = viewModel.getBridge(project.bridgeId)
    if (bridge == null) {
        InvalidRouteScreen(title = "桥梁检测", message = "桥梁数据不存在或已被移除。", onBack = onBack)
        return
    }
    val projectComponents = viewModel.getProjectComponents(projectId)
    val teamMembers = viewModel.getProjectMembers(projectId)
    val progress = viewModel.projectProgress(projectId)
    var componentFilter by rememberSaveable(projectId) { mutableStateOf("全部") }
    var showAddDialog by rememberSaveable(projectId) { mutableStateOf(false) }
    var showBridgeInfoDialog by rememberSaveable(projectId) { mutableStateOf(false) }
    var newCategory by rememberSaveable(projectId) { mutableStateOf("上部结构") }
    var newType by rememberSaveable(projectId) { mutableStateOf("") }
    var newNumber by rememberSaveable(projectId) { mutableStateOf("") }
    var newFocusDefects by rememberSaveable(projectId) { mutableStateOf("裂缝") }
    var selectedResponsibleUserId by rememberSaveable(projectId) { mutableStateOf(viewModel.currentUser.id) }
    val visibleComponents = projectComponents.filter { component ->
        when (componentFilter) {
            "我负责" -> component.responsibleUserId == viewModel.currentUser.id
            "待检测" -> component.status == ComponentStatus.Pending
            "待复核" -> component.status == ComponentStatus.AiDone
            "现场新增" -> component.isCustom
            "协作记录" -> component.actualInspectorUserId != null && component.actualInspectorUserId != component.responsibleUserId
            else -> true
        }
    }
    val groupedVisibleComponents = visibleComponents.groupBy { it.category }

    if (showAddDialog) {
        AppDialog(
            title = "新增检测项",
            subtitle = "补充现场发现的问题，保存后会立即进入当前项目列表。",
            onDismissRequest = { showAddDialog = false },
            actions = {
                DialogTextAction(
                    text = "取消",
                    onClick = { showAddDialog = false },
                )
                DialogPrimaryAction(
                    text = "保存",
                    onClick = {
                        if (newType.isBlank() || newNumber.isBlank()) {
                            Toast.makeText(context, "请补充构件类型和编号", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addCustomComponent(
                                projectId = projectId,
                                category = newCategory,
                                type = newType.trim(),
                                number = newNumber.trim(),
                                focusDefects = newFocusDefects.split("、", ",", "，").map { it.trim() }.filter { it.isNotBlank() },
                                responsibleUserId = selectedResponsibleUserId,
                            )
                            showAddDialog = false
                            newType = ""
                            newNumber = ""
                            newFocusDefects = "裂缝"
                            selectedResponsibleUserId = viewModel.currentUser.id
                            Toast.makeText(context, "已新增现场检测项", Toast.LENGTH_SHORT).show()
                        }
                    },
                )
            },
        ) {
            DialogSectionTitle("构件归类")
            SelectionGrid(
                options = listOf("上部结构", "下部结构", "附属设施", "其他"),
                selectedOption = newCategory,
                columns = 2,
                onSelect = { newCategory = it },
            )
            Spacer(modifier = Modifier.height(18.dp))
            AppFormField(
                value = newType,
                onValueChange = { newType = it },
                label = "构件类型",
                placeholder = "例如：主梁、墩柱、支座",
            )
            Spacer(modifier = Modifier.height(12.dp))
            AppFormField(
                value = newNumber,
                onValueChange = { newNumber = it },
                label = "构件编号/位置",
                placeholder = "例如：主梁左幅1跨",
            )
            Spacer(modifier = Modifier.height(12.dp))
            AppFormField(
                value = newFocusDefects,
                onValueChange = { newFocusDefects = it },
                label = "关注病害",
                placeholder = "裂缝、渗水、露筋",
                supporting = "多个病害请用顿号或逗号分隔",
                minLines = 2,
            )
            Spacer(modifier = Modifier.height(18.dp))
            DialogSectionTitle("负责人")
            SelectionGrid(
                options = teamMembers.map { it.realName },
                selectedOption = teamMembers.firstOrNull { it.id == selectedResponsibleUserId }?.realName.orEmpty(),
                columns = 3,
                onSelect = { selectedName ->
                    selectedResponsibleUserId = teamMembers.firstOrNull { it.realName == selectedName }?.id ?: selectedResponsibleUserId
                },
            )
        }
    }
    if (showBridgeInfoDialog) {
        BridgeInfoCorrectionDialog(
            bridge = bridge,
            correctionNote = viewModel.getBridgeCorrectionNote(bridge.id),
            onDismiss = { showBridgeInfoDialog = false },
            onSave = { totalLength, bridgeWidth, mainSpan, address, maintenanceUnit, note ->
                val saved = viewModel.updateBridgeCorrection(
                    bridgeId = bridge.id,
                    totalLengthText = totalLength,
                    bridgeWidthText = bridgeWidth,
                    mainSpanText = mainSpan,
                    address = address,
                    maintenanceUnit = maintenanceUnit,
                    correctionNote = note,
                )
                if (saved) {
                    showBridgeInfoDialog = false
                    Toast.makeText(context, "桥梁基础信息已更新，报告将使用最新信息", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "桥梁信息保存失败，请稍后重试", Toast.LENGTH_SHORT).show()
                }
            },
        )
    }
    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(
            title = "桥梁检测",
            onBack = onBack,
            actionSlotWidth = 88,
            rightContent = {
                TopBarActionButton(
                    text = "报告",
                    icon = Icons.Default.Description,
                    onClick = {
                        val reportId = viewModel.generateReport(projectId)
                        if (reportId > 0) {
                            onGenerateReport(reportId)
                        } else {
                            Toast.makeText(context, "报告生成失败，请稍后重试", Toast.LENGTH_SHORT).show()
                        }
                    },
                )
            },
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                CompactBridgeHeaderCard(
                    bridge = bridge,
                    project = project,
                    progress = progress,
                    correctionNote = viewModel.getBridgeCorrectionNote(bridge.id),
                    onOpenBridgeInfo = { showBridgeInfoDialog = true },
                )
            }
            item {
                CompactProjectProgressCard(
                    progress = progress,
                    hasCollectedPendingAi = projectComponents.any { it.status == ComponentStatus.Collected },
                    onBatchAi = { viewModel.runBatchAi(projectId) },
                    onAddComponent = { showAddDialog = true },
                )
            }
            item {
                FilterRow(
                    title = "按检测项查看",
                    options = listOf("全部", "我负责", "待检测", "待复核", "现场新增", "协作记录"),
                    selected = componentFilter,
                    onSelected = { componentFilter = it },
                )
            }
            if (groupedVisibleComponents.isEmpty()) {
                item {
                    SectionCard {
                        Text("当前筛选下暂无检测项", color = CardText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("可以切换筛选条件，或新增现场检测项继续补充。", color = SubtleText, fontSize = 13.sp)
                    }
                }
            }
            groupedVisibleComponents.forEach { (category, items) ->
                item {
                    Text(
                        text = category,
                        color = CardText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    )
                }
                items(items) { component ->
                    CompactDetectionTaskCard(
                        component = component,
                        currentUserId = viewModel.currentUser.id,
                        onClick = { onComponentClick(component.id, component.mediaItems.isNotEmpty()) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactBridgeHeaderCard(
    bridge: BridgeItem,
    project: ProjectItem,
    progress: ProjectProgress,
    correctionNote: String,
    onOpenBridgeInfo: () -> Unit,
) {
    val completionRate = if (progress.total == 0) 0f else progress.completed.toFloat() / progress.total.toFloat()
    SectionCard {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    bridge.bridgeName,
                    color = CardText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${bridge.bridgeCode} · ${project.projectType}",
                    color = SubtleText,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            StatusChip(project.projectStatus.displayProjectStatus(), BridgeBlue, BridgeBlue.copy(alpha = 0.12f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(BorderGray),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(completionRate)
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(BridgeBlue),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CompactMetricPill("总构件", progress.total.toString(), Modifier.weight(1f))
            CompactMetricPill("已采集", progress.collected.toString(), Modifier.weight(1f))
            CompactMetricPill("已完成", progress.completed.toString(), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(LightBlue.copy(alpha = 0.55f))
                .clickable(onClick = onOpenBridgeInfo)
                .padding(horizontal = 12.dp, vertical = 11.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("桥梁基础信息", color = CardText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        if (correctionNote.isBlank()) {
                            "${bridge.totalLength}m · ${bridge.bridgeWidth}m宽 · ${bridge.maintenanceUnit}"
                        } else {
                            "已现场修正，报告将使用最新信息"
                        },
                        color = SubtleText,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text("查看/修正", color = BridgeBlue, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = BridgeBlue)
            }
        }
    }
}

@Composable
private fun CompactProjectProgressCard(
    progress: ProjectProgress,
    hasCollectedPendingAi: Boolean,
    onBatchAi: () -> Unit,
    onAddComponent: () -> Unit,
) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("下一步操作", fontWeight = FontWeight.Bold, color = CardText)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    when {
                        progress.completed == progress.total && progress.total > 0 -> "检测已完成，可以生成报告"
                        progress.aiDone > progress.completed -> "优先复核 AI 结果"
                        hasCollectedPendingAi -> "已有素材，可批量识别"
                        else -> "从待检测构件开始采集"
                    },
                    color = SubtleText,
                    fontSize = 13.sp,
                )
            }
            StatusChip("待检 ${(progress.total - progress.collected).coerceAtLeast(0)}", WarningOrange, WarningOrange.copy(alpha = 0.12f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onBatchAi,
                enabled = hasCollectedPendingAi,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = BridgeBlue),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("批量AI")
            }
            OutlinedButton(
                onClick = onAddComponent,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("新增检测项")
            }
        }
    }
}

@Composable
private fun CompactDetectionTaskCard(
    component: ComponentTask,
    currentUserId: Long,
    onClick: () -> Unit,
) {
    SectionCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${component.type} · ${component.number}",
                    color = CardText,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    component.focusDefects.joinToString(" · "),
                    color = SubtleText,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusChip(
                        text = if (component.responsibleUserId == currentUserId) "我负责" else component.responsibleName,
                        color = if (component.responsibleUserId == currentUserId) BridgeBlue else SubtleText,
                        background = if (component.responsibleUserId == currentUserId) BridgeBlue.copy(alpha = 0.12f) else SubtleText.copy(alpha = 0.12f),
                    )
                    if (component.mediaItems.isNotEmpty()) {
                        StatusChip("素材 ${component.mediaItems.size}", SuccessGreen, SuccessGreen.copy(alpha = 0.12f))
                    }
                    if (component.isCustom) {
                        StatusChip("现场新增", WarningOrange, WarningOrange.copy(alpha = 0.12f))
                    }
                    if (component.actualInspectorUserId != null && component.actualInspectorUserId != component.responsibleUserId) {
                        StatusChip("协作", SuccessGreen, SuccessGreen.copy(alpha = 0.12f))
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            ComponentStatusChip(component.status)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                component.status.compactActionText(),
                color = BridgeBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = BridgeBlue)
        }
    }
}

@Composable
private fun CompactMetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF7F8FA))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(value, color = CardText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = SubtleText, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun BridgeInfoCorrectionDialog(
    bridge: BridgeItem,
    correctionNote: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit,
) {
    var totalLength by rememberSaveable(bridge.id) { mutableStateOf(bridge.totalLength.toString()) }
    var bridgeWidth by rememberSaveable(bridge.id) { mutableStateOf(bridge.bridgeWidth.toString()) }
    var mainSpan by rememberSaveable(bridge.id) { mutableStateOf(bridge.mainSpan.toString()) }
    var address by rememberSaveable(bridge.id) { mutableStateOf(bridge.address) }
    var maintenanceUnit by rememberSaveable(bridge.id) { mutableStateOf(bridge.maintenanceUnit) }
    var note by rememberSaveable(bridge.id) { mutableStateOf(correctionNote) }

    AppEditorDialog(
        title = "桥梁基础信息",
        subtitle = "现场发现档案不准时，可以直接修正。后续报告会优先采用这里保存后的真实信息。",
        onDismissRequest = onDismiss,
        actions = {
            DialogTextAction("关闭", onDismiss)
            DialogPrimaryAction("保存修正") {
                onSave(totalLength, bridgeWidth, mainSpan, address, maintenanceUnit, note)
            }
        },
    ) {
        DialogSectionTitle("档案信息")
        InfoRow("桥梁编码", bridge.bridgeCode)
        InfoRow("所属路线", bridge.route)
        InfoRow("桥梁类型", bridge.bridgeType)
        InfoRow("结构形式", bridge.structureType)
        InfoRow("建成年份", "${bridge.constructionYear} 年")
        Spacer(modifier = Modifier.height(8.dp))
        DialogSectionTitle("现场可修正")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            AppFormField(
                value = totalLength,
                onValueChange = { totalLength = it },
                label = "全长 m",
                placeholder = "例如 123",
                modifier = Modifier.weight(1f),
            )
            AppFormField(
                value = bridgeWidth,
                onValueChange = { bridgeWidth = it },
                label = "宽度 m",
                placeholder = "例如 30",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        AppFormField(
            value = mainSpan,
            onValueChange = { mainSpan = it },
            label = "主跨跨径 m",
            placeholder = "例如 60",
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppFormField(
            value = maintenanceUnit,
            onValueChange = { maintenanceUnit = it },
            label = "养护单位",
            placeholder = "请输入现场确认后的养护单位",
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppFormField(
            value = address,
            onValueChange = { address = it },
            label = "桥梁地址",
            placeholder = "请输入现场确认后的地理位置",
            minLines = 2,
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppFormField(
            value = note,
            onValueChange = { note = it },
            label = "现场修正说明",
            placeholder = "例如：现场复核桥梁全长为123m，原档案长度偏差3m",
            supporting = "这条说明会进入报告预览，方便后续审核追溯。",
            minLines = 3,
        )
    }
}

@Composable
private fun QuickCollectScreen(
    viewModel: BridgeAiViewModel,
    componentId: Long,
    onBack: () -> Unit,
    onOpenStandard: () -> Unit,
) {
    val context = LocalContext.current
    val component = viewModel.getComponent(componentId)
    if (component == null) {
        InvalidRouteScreen(title = "极速采集", message = "构件数据不存在或已被移除。", onBack = onBack)
        return
    }
    val project = viewModel.getProject(component.projectId)
    val bridge = project?.let { viewModel.getBridge(it.bridgeId) }
    val pendingMedia = remember(component.id) { mutableStateListOf<ComponentMedia>() }
    var voiceText by rememberSaveable(component.id) { mutableStateOf(quickVoiceTranscript(component)) }
    var extractedText by rememberSaveable(component.id) { mutableStateOf(quickExtractedText(component)) }
    var reportText by rememberSaveable(component.id) { mutableStateOf(quickReportText(component)) }
    var voiceRound by rememberSaveable(component.id) { mutableStateOf(1) }

    LaunchedEffect(component.id) {
        pendingMedia.clear()
        pendingMedia.addAll(component.mediaItems)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap: Bitmap? ->
        val fileUri = bitmap?.let { saveBitmapToCache(context, it) }
        if (fileUri != null) {
            pendingMedia.add(ComponentMedia("quick-photo-${System.nanoTime()}", fileUri.toString(), MediaType.Photo))
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
    ) { uris ->
        if (uris.isNotEmpty()) {
            pendingMedia.addAll(uris.map { ComponentMedia("quick-import-${System.nanoTime()}-${it.hashCode()}", it.toString(), MediaType.Photo) })
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(
            title = "极速采集",
            onBack = onBack,
            actionSlotWidth = 92,
            rightContent = {
                TopBarActionButton(
                    text = "标准",
                    icon = Icons.Default.Tune,
                    onClick = onOpenStandard,
                )
            },
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionCard {
                    Text("${component.type} · ${component.number}", color = CardText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${bridge?.bridgeName ?: "桥梁"} · ${project?.projectName ?: "检测任务"}", color = SubtleText, fontSize = 13.sp, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip("照片 ${pendingMedia.size}", BridgeBlue, LightBlue.copy(alpha = 0.7f))
                        StatusChip("语音 $voiceRound 段", SuccessGreen, SuccessGreen.copy(alpha = 0.12f))
                        StatusChip("已离线保存", WarningOrange, WarningOrange.copy(alpha = 0.12f))
                    }
                }
            }
            item {
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("照片采集", color = CardText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("自动编号", color = SubtleText, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (pendingMedia.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(LightBlue.copy(alpha = 0.58f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BridgeBlue, modifier = Modifier.size(42.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("还没有照片", color = CardText, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("先拍几张，再补一段语音备注", color = SubtleText, fontSize = 13.sp)
                            }
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(pendingMedia) { media ->
                                Box {
                                    AsyncImage(
                                        model = media.uri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(width = 132.dp, height = 150.dp)
                                            .clip(RoundedCornerShape(14.dp)),
                                        contentScale = ContentScale.Crop,
                                    )
                                    StatusChip(
                                        text = "P${pendingMedia.indexOf(media) + 1}",
                                        color = Color.White,
                                        background = Color.Black.copy(alpha = 0.48f),
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("拍照")
                        }
                        OutlinedButton(
                            onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("补选")
                        }
                    }
                }
            }
            item {
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("语音备注", color = CardText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        StatusChip("模拟识别", BridgeBlue, LightBlue.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            voiceRound += 1
                            voiceText = quickVoiceTranscript(component, voiceRound)
                            extractedText = quickExtractedText(component, voiceRound)
                            reportText = quickReportText(component, voiceRound)
                            Toast.makeText(context, "已生成一段模拟语音备注", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BridgeBlue),
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("添加一段语音备注")
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    DialogSectionTitle("原始口述")
                    DialogHighlightBlock(voiceText)
                    Spacer(modifier = Modifier.height(14.dp))
                    DialogSectionTitle("AI提炼")
                    DialogHighlightBlock(extractedText, accent = true)
                    Spacer(modifier = Modifier.height(14.dp))
                    DialogSectionTitle("报告化描述")
                    DialogHighlightBlock(reportText, accent = true)
                }
            }
            item {
                Button(
                    onClick = {
                        viewModel.replaceMedia(component.id, pendingMedia.toList())
                        if (pendingMedia.isNotEmpty()) {
                            viewModel.runAi(component.id)
                        }
                        Toast.makeText(context, "采集批次已保存，照片和语音已绑定", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    enabled = pendingMedia.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                ) {
                    Text("保存采集批次")
                }
            }
        }
    }
}

@Composable
private fun ComponentCaptureScreen(
    viewModel: BridgeAiViewModel,
    componentId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current
    val component = viewModel.getComponent(componentId)
    if (component == null) {
        InvalidRouteScreen(title = "素材采集", message = "构件数据不存在或已被移除。", onBack = onBack)
        return
    }
    val project = viewModel.getProject(component.projectId)
    val bridge = project?.let { viewModel.getBridge(it.bridgeId) }
    val pendingMedia = remember(component.id) { mutableStateListOf<ComponentMedia>() }

    LaunchedEffect(component.id) {
        pendingMedia.clear()
        pendingMedia.addAll(component.mediaItems)
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 6),
    ) { uris ->
        if (uris.isNotEmpty()) {
            pendingMedia.addAll(uris.map { ComponentMedia("media-${System.nanoTime()}-${it.hashCode()}", it.toString(), MediaType.Photo) })
        }
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            pendingMedia.add(ComponentMedia("video-${System.nanoTime()}", uri.toString(), MediaType.Video))
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap: Bitmap? ->
        val fileUri = bitmap?.let { saveBitmapToCache(context, it) }
        if (fileUri != null) {
            pendingMedia.add(ComponentMedia("photo-${System.nanoTime()}", fileUri.toString(), MediaType.Photo))
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        BlueTopBar(
            title = component.type,
            onBack = onBack,
            actionSlotWidth = 88,
            rightContent = {
                TopBarActionButton(
                    text = "保存",
                    icon = Icons.Default.Check,
                    onClick = {
                        if (pendingMedia.isEmpty()) {
                            Toast.makeText(context, "请至少添加一张照片或视频", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.replaceMedia(componentId, pendingMedia.toList())
                            onSaved()
                        }
                    },
                )
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                text = "${bridge?.bridgeName.orEmpty()} · ${component.number}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))
            SectionCard {
                Text("采集提示", color = CardText, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("先拍清楚病害区域，再补拍能说明位置关系的远景。保存后会自动进入 AI 结果页。", color = SubtleText, fontSize = 13.sp, lineHeight = 20.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF111111)),
                contentAlignment = Alignment.Center,
            ) {
                val primaryMedia = pendingMedia.lastOrNull()
                if (primaryMedia == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White.copy(alpha = 0.82f), modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("当前构件尚未采集素材", color = Color.White.copy(alpha = 0.82f))
                    }
                } else {
                    if (primaryMedia.type == MediaType.Photo) {
                        AsyncImage(
                            model = primaryMedia.uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(72.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("已选择视频素材", color = Color.White)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (pendingMedia.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(pendingMedia, key = { it.id }) { media ->
                        Box {
                            if (media.type == MediaType.Photo) {
                                AsyncImage(
                                    model = media.uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(14.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF252525)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .clickable { pendingMedia.remove(media) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("×", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ActionButton("拍照", Icons.Default.CameraAlt, BridgeBlue, Modifier.weight(1f)) { cameraLauncher.launch(null) }
                ActionButton("导入图片", Icons.Default.Image, Color.White, Modifier.weight(1f), textColor = CardText, border = BorderStroke(1.dp, BorderGray)) {
                    imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            ActionButton("导入视频", Icons.Default.PlayCircle, Color.White, Modifier.fillMaxWidth(), textColor = CardText, border = BorderStroke(1.dp, BorderGray)) {
                videoPicker.launch("video/*")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (pendingMedia.isEmpty()) {
                        Toast.makeText(context, "请至少添加一张照片或视频", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.replaceMedia(componentId, pendingMedia.toList())
                        onSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BridgeBlue),
            ) {
                Text("保存素材并进入AI结果")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "所有素材会自动关联到当前构件，不会混淆到其他检测对象。",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun ComponentResultScreen(
    viewModel: BridgeAiViewModel,
    componentId: Long,
    onBack: () -> Unit,
    onCompleted: (Long) -> Unit,
    onRetake: () -> Unit,
) {
    val context = LocalContext.current
    val component = viewModel.getComponent(componentId)
    if (component == null) {
        InvalidRouteScreen(title = "AI结果", message = "构件数据不存在或已被移除。", onBack = onBack)
        return
    }
    val aiType = component.aiDisplayType()
    val aiLevel = component.aiDisplayLevel()
    val aiLocation = component.aiDisplayLocation()
    val aiQuantitative = component.aiDisplayQuantitativeInfo()
    val aiRepairAdvice = component.aiDisplayRepairAdvice()
    var defectType by rememberSaveable(component.id, component.defectType) { mutableStateOf(component.defectType.takeIf { it != "待识别" } ?: aiType.ifBlank { component.focusDefects.firstOrNull().orEmpty() }) }
    var defectLevel by rememberSaveable(component.id, component.defectLevel) { mutableStateOf(component.defectLevel.ifBlank { aiLevel }) }
    var locationDesc by rememberSaveable(component.id, component.locationDesc) { mutableStateOf(component.locationDesc.ifBlank { aiLocation }) }
    var quantitativeInfo by rememberSaveable(component.id, component.quantitativeInfo) { mutableStateOf(component.quantitativeInfo.ifBlank { aiQuantitative }) }
    var repairAdvice by rememberSaveable(component.id, component.repairAdvice) { mutableStateOf(component.repairAdvice.ifBlank { aiRepairAdvice }) }
    var remarks by rememberSaveable(component.id, component.remarks) { mutableStateOf(component.remarks) }
    var isEditing by rememberSaveable(component.id) { mutableStateOf(false) }
    var showOptimizeDialog by rememberSaveable(component.id) { mutableStateOf(false) }
    var optimizationPrompt by rememberSaveable(component.id) { mutableStateOf("") }
    var candidateRepairAdvice by rememberSaveable(component.id) { mutableStateOf("") }

    if (component.status == ComponentStatus.Collected && component.aiSummary.isBlank()) {
        LaunchedEffect(component.id, component.status, component.aiSummary) {
            viewModel.runAi(component.id)
        }
    }

    LaunchedEffect(
        component.id,
        component.defectType,
        component.defectLevel,
        component.locationDesc,
        component.quantitativeInfo,
        component.repairAdvice,
        component.remarks,
    ) {
        defectType = component.defectType.takeIf { it != "待识别" } ?: aiType.ifBlank { component.focusDefects.firstOrNull().orEmpty() }
        defectLevel = component.defectLevel.ifBlank { aiLevel }
        locationDesc = component.locationDesc.ifBlank { aiLocation }
        quantitativeInfo = component.quantitativeInfo.ifBlank { aiQuantitative }
        repairAdvice = component.repairAdvice.ifBlank { aiRepairAdvice }
        remarks = component.remarks
    }

    val hasLocalAdjustment =
        defectType != aiType ||
            defectLevel != aiLevel ||
            locationDesc != aiLocation ||
            quantitativeInfo != aiQuantitative ||
            repairAdvice != aiRepairAdvice

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(
            title = "${component.type}${component.number}",
            onBack = onBack,
            actionSlotWidth = 88,
            rightContent = {
                TopBarActionButton(
                    text = "完成",
                    icon = Icons.Default.Check,
                    onClick = {
                        viewModel.saveComponentResult(
                            componentId = component.id,
                            defectType = defectType.ifBlank { "无病害" },
                            defectLevel = defectLevel,
                            locationDesc = locationDesc,
                            quantitativeInfo = quantitativeInfo,
                            repairAdvice = repairAdvice,
                            remarks = remarks,
                            completed = true,
                        )
                        Toast.makeText(context, "已保存并标记完成", Toast.LENGTH_SHORT).show()
                        onCompleted(component.projectId)
                    },
                )
            },
        )
        if (showOptimizeDialog) {
            AppEditorDialog(
                title = "优化修复建议",
                subtitle = "先描述你的现场判断，再让 AI 生成更贴近作业场景的新建议。",
                onDismissRequest = { showOptimizeDialog = false },
                actions = {
                    DialogTextAction(
                        text = "关闭",
                        onClick = { showOptimizeDialog = false },
                    )
                    DialogSecondaryAction(
                        text = if (candidateRepairAdvice.isBlank()) "AI生成" else "继续优化",
                        onClick = {
                            candidateRepairAdvice = viewModel.generateRepairAdviceCandidate(
                                baseAdvice = repairAdvice.ifBlank { aiRepairAdvice },
                                defectType = defectType.ifBlank { aiType },
                                defectLevel = defectLevel.ifBlank { aiLevel },
                                componentType = component.type,
                                optimizationPrompt = optimizationPrompt,
                            )
                        },
                    )
                    DialogPrimaryAction(
                        text = "采纳更新",
                        enabled = candidateRepairAdvice.isNotBlank(),
                        onClick = {
                            if (candidateRepairAdvice.isNotBlank()) {
                                repairAdvice = candidateRepairAdvice
                                showOptimizeDialog = false
                                isEditing = true
                            }
                        },
                    )
                },
            ) {
                DialogSectionTitle("当前建议")
                DialogHighlightBlock(
                    text = repairAdvice.ifBlank { aiRepairAdvice.ifBlank { "当前尚无修复建议" } },
                )
                Spacer(modifier = Modifier.height(16.dp))
                AppFormField(
                    value = optimizationPrompt,
                    onValueChange = { optimizationPrompt = it },
                    label = "你的优化想法",
                    placeholder = "例如：先清理剥落区域，再做防锈与表层修补",
                    supporting = "支持系统语音输入；也可点右上角关闭先退出编辑。",
                    minLines = 4,
                )
                if (candidateRepairAdvice.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    DialogSectionTitle("AI新版建议")
                    DialogHighlightBlock(
                        text = candidateRepairAdvice,
                        accent = true,
                    )
                }
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionCard {
                    Text("检测素材", fontWeight = FontWeight.Bold, color = CardText)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (component.mediaItems.isEmpty()) {
                        Text("尚未采集素材", color = SubtleText)
                    } else {
                        val coverPhoto = component.mediaItems.firstOrNull { it.type == MediaType.Photo }
                        if (coverPhoto != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                            ) {
                                AsyncImage(
                                    model = coverPhoto.uri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                                AiDetectionOverlay(
                                    defectLabel = aiType.ifBlank { component.focusDefects.firstOrNull() ?: "待识别" },
                                    measurementText = aiQuantitative.ifBlank { "等待 AI 输出量化信息" },
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color.Black.copy(alpha = 0.52f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text("AI识别图示意", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(component.mediaItems) { media ->
                                if (media.type == MediaType.Photo) {
                                    AsyncImage(
                                        model = media.uri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(14.dp)),
                                        contentScale = ContentScale.Crop,
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFF252525)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onRetake,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("重新拍摄")
                    }
                }
            }
            item {
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TaskAlt, contentDescription = null, tint = BridgeBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI基础信息", fontWeight = FontWeight.Bold, color = CardText)
                        Spacer(modifier = Modifier.weight(1f))
                        StatusChip(
                            text = if (hasLocalAdjustment) "已人工修订" else "采纳AI结果",
                            color = if (hasLocalAdjustment) WarningOrange else SuccessGreen,
                            background = if (hasLocalAdjustment) WarningOrange.copy(alpha = 0.12f) else SuccessGreen.copy(alpha = 0.12f),
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(component.aiSummary.ifBlank { "当前构件尚未完成AI识别。" }, color = CardText, lineHeight = 22.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { isEditing = !isEditing },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BridgeBlue),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isEditing) "收起编辑" else "编辑当前结果")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("快捷操作", color = SubtleText, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CompactActionChip(
                            text = "优化建议",
                            icon = Icons.Default.Tune,
                            onClick = {
                                optimizationPrompt = ""
                                candidateRepairAdvice = ""
                                showOptimizeDialog = true
                            },
                        )
                        CompactActionChip(
                            text = "恢复AI",
                            icon = Icons.Default.Refresh,
                            enabled = hasLocalAdjustment || remarks != "AI已完成初步识别，建议人工复核。",
                            onClick = {
                                defectType = aiType
                                defectLevel = aiLevel
                                locationDesc = aiLocation
                                quantitativeInfo = aiQuantitative
                                repairAdvice = aiRepairAdvice
                                remarks = "AI已完成初步识别，建议人工复核。"
                                isEditing = false
                                Toast.makeText(context, "已恢复为AI识别结果", Toast.LENGTH_SHORT).show()
                            },
                        )
                        CompactActionChip(
                            text = "重新识别",
                            icon = Icons.Default.Refresh,
                            onClick = {
                                viewModel.runAi(component.id)
                                isEditing = false
                                Toast.makeText(context, "AI已重新识别", Toast.LENGTH_SHORT).show()
                            },
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        if (isEditing) {
                            "正在编辑当前结果。先改病害类型和等级，再补位置、量化信息和修复建议。右上角“完成”用于结束该构件。"
                        } else {
                            "先核对 AI 结果；没问题可直接点右上角“完成”，有偏差再点“直接编辑当前结果”。"
                        },
                        color = SubtleText,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(
                            if (component.responsibleUserId == viewModel.currentUser.id) "我负责" else "${component.responsibleName}负责",
                            if (component.responsibleUserId == viewModel.currentUser.id) BridgeBlue else SubtleText,
                            if (component.responsibleUserId == viewModel.currentUser.id) BridgeBlue.copy(alpha = 0.12f) else SubtleText.copy(alpha = 0.12f),
                        )
                        if (component.actualInspectorUserId != null) {
                            StatusChip(
                                "执行：${component.actualInspectorName}",
                                SuccessGreen,
                                SuccessGreen.copy(alpha = 0.12f),
                            )
                        }
                        if (component.isCustom) {
                            StatusChip("现场新增", WarningOrange, WarningOrange.copy(alpha = 0.12f))
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    if (isEditing) {
                        Text("病害类型", color = CardText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("优先选择最贴近现场结论的一项，减少后续补录负担。", color = SubtleText, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectionGrid(
                            options = listOf("裂缝", "混凝土剥落/露筋", "锈蚀", "混凝土空洞", "表面劣化", "渗水/潮湿", "收缩裂缝", "施工缺陷", "无病害"),
                            selectedOption = defectType,
                            columns = 2,
                            stretchSingleLastItem = true,
                            onSelect = { defectType = it },
                        )
                        AiReferenceHint("AI初判：${aiType.ifBlank { "待识别" }}")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("病害等级", color = CardText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectionGrid(
                            options = listOf("轻微", "中等", "严重"),
                            selectedOption = defectLevel,
                            columns = 3,
                            onSelect = { defectLevel = it },
                        )
                        AiReferenceHint("AI初判：${aiLevel.ifBlank { "轻微" }}")
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = locationDesc,
                            onValueChange = { locationDesc = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            label = { Text("位置描述") },
                        )
                        AiReferenceHint("AI初判：${aiLocation.ifBlank { "待补充" }}")
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = quantitativeInfo,
                            onValueChange = { quantitativeInfo = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            minLines = 3,
                            label = { Text("量化信息") },
                        )
                        AiReferenceHint("AI初判：${aiQuantitative.ifBlank { "待补充" }}")
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = repairAdvice,
                            onValueChange = { repairAdvice = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            minLines = 4,
                            label = { Text("修复建议") },
                        )
                        AiReferenceHint("AI初判：${aiRepairAdvice.ifBlank { "待补充" }}")
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = remarks,
                            onValueChange = { remarks = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            minLines = 3,
                            label = { Text("复核备注") },
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                viewModel.saveComponentResult(
                                    componentId = component.id,
                                    defectType = defectType.ifBlank { "无病害" },
                                    defectLevel = defectLevel,
                                    locationDesc = locationDesc,
                                    quantitativeInfo = quantitativeInfo,
                                    repairAdvice = repairAdvice,
                                    remarks = remarks,
                                    completed = false,
                                )
                                isEditing = false
                                Toast.makeText(context, "当前修改已保存，可继续复核其他内容", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BridgeBlue),
                        ) {
                            Text("保存当前修改（不结束）")
                        }
                    } else {
                        ComparisonInfoRow("病害类型", defectType.ifBlank { "待识别" }, aiType.ifBlank { "待识别" })
                        ComparisonInfoRow("病害等级", defectLevel.ifBlank { "轻微" }, aiLevel.ifBlank { "轻微" })
                        ComparisonInfoRow("位置描述", locationDesc.ifBlank { "待补充" }, aiLocation.ifBlank { "待补充" })
                        ComparisonInfoRow("量化信息", quantitativeInfo.ifBlank { "待补充" }, aiQuantitative.ifBlank { "待补充" })
                        ComparisonInfoRow("修复建议", repairAdvice.ifBlank { "待补充" }, aiRepairAdvice.ifBlank { "待补充" })
                        InfoRow("复核备注", remarks.ifBlank { "无" })
                        component.aiConfidence?.let {
                            Text("识别置信度：${it}%", color = BridgeBlue, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BridgeDetailScreen(
    viewModel: BridgeAiViewModel,
    bridgeId: Long,
    onBack: () -> Unit,
    onStartInspect: (Long) -> Unit,
) {
    val bridge = viewModel.getBridge(bridgeId)
    if (bridge == null) {
        InvalidRouteScreen(title = "桥梁详情", message = "桥梁数据不存在或已被移除。", onBack = onBack)
        return
    }
    val relatedProjects = viewModel.projects.filter { it.bridgeId == bridgeId }
    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(title = "桥梁详情", onBack = onBack)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(BridgeBlue, BridgeBlue.copy(alpha = 0.8f)),
                                )
                            )
                            .padding(18.dp),
                    ) {
                        Column {
                            Text(
                                bridge.bridgeName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(bridge.bridgeCode, color = Color.White.copy(alpha = 0.88f))
                            Spacer(modifier = Modifier.height(16.dp))
                            HeaderMetricGrid(
                                metrics = listOf(
                                    "桥梁状态" to bridge.statusText,
                                    "已使用" to "${2026 - bridge.constructionYear}年",
                                    "主跨" to "${bridge.mainSpan}m",
                                ),
                            )
                        }
                    }
                }
            }
            item {
                SectionCard {
                    Text("基础信息", fontWeight = FontWeight.Bold, color = CardText)
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow("桥梁类型", bridge.bridgeType)
                    InfoRow("结构形式", bridge.structureType)
                    InfoRow("所属路线", bridge.route)
                    InfoRow("建成年份", "${bridge.constructionYear}年")
                    InfoRow("桥梁全长", "${bridge.totalLength}m")
                    InfoRow("桥面宽度", "${bridge.bridgeWidth}m")
                    InfoRow("地址", bridge.address)
                    InfoRow("养护单位", bridge.maintenanceUnit)
                }
            }
            item {
                SectionCard {
                    Text("关联项目", fontWeight = FontWeight.Bold, color = CardText)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (relatedProjects.isEmpty()) {
                        Text("当前桥梁暂无项目", color = SubtleText)
                    } else {
                        relatedProjects.forEachIndexed { index, project ->
                            if (index > 0) Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF7F8FA))
                                    .padding(14.dp),
                            ) {
                                Column {
                                    Text(
                                        project.projectName,
                                        color = CardText,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${project.projectType} · ${project.startDate} - ${project.endDate}", color = SubtleText, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { onStartInspect(project.id) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BridgeBlue),
                                    ) {
                                        Text("开始检测")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportDetailScreen(
    viewModel: BridgeAiViewModel,
    reportId: Long,
    onBack: () -> Unit,
    onEditReport: () -> Unit,
    onEditComponent: (Long) -> Unit,
) {
    val context = LocalContext.current
    val preview = viewModel.buildReportPreview(reportId)
    if (preview == null) {
        InvalidRouteScreen(title = "报告预览", message = "报告数据不存在或尚未生成。", onBack = onBack)
        return
    }
    val report = preview.report
    val bridge = preview.bridge
    val project = preview.project
    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(
            title = "报告预览",
            onBack = onBack,
            actionSlotWidth = 88,
            rightContent = {
                if (report.reportStatus != "reported") {
                    TopBarActionButton(
                        text = if (report.reportStatus == "draft") "编辑" else "完善",
                        icon = Icons.Default.Edit,
                        onClick = onEditReport,
                    )
                }
            },
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionCard {
                    Text("桥梁检测报告", color = SubtleText, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        report.reportTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = CardText,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("报告编号：${report.reportCode}", color = SubtleText, fontSize = 13.sp)
                    Text("桥梁：${bridge.bridgeName}", color = SubtleText, fontSize = 13.sp)
                    Text("项目：${project.projectName}", color = SubtleText, fontSize = 13.sp)
                    Text("生成日期：${report.generatedDate}", color = SubtleText, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    StatusChip(
                        text = when (report.reportStatus) {
                            "reported" -> "已上报"
                            "completed" -> "已完成"
                            else -> "草稿"
                        },
                        color = if (report.reportStatus == "reported") BridgeBlue else if (report.reportStatus == "completed") SuccessGreen else SubtleText,
                        background = if (report.reportStatus == "reported") BridgeBlue.copy(alpha = 0.12f) else if (report.reportStatus == "completed") SuccessGreen.copy(alpha = 0.12f) else SubtleText.copy(alpha = 0.12f),
                    )
                }
            }
            item {
                ReportSection("一、项目概况", listOf(
                    "项目名称：${project.projectName}",
                    "项目编号：${project.projectCode}",
                    "检测类型：${project.projectType}",
                    "检测桥梁：${bridge.bridgeName}",
                    "检测时段：${project.startDate} 至 ${project.endDate}",
                    "项目负责人：${project.leaderName}",
                    "检测方法：人工巡检 + AI辅助识别",
                ))
            }
            item {
                ReportSection(
                    "二、桥梁基本信息",
                    buildList {
                        add("桥梁编码：${bridge.bridgeCode}")
                        add("所属路线：${bridge.route}")
                        add("桥梁类型：${bridge.bridgeType}")
                        add("结构形式：${bridge.structureType}")
                        add("建成年份：${bridge.constructionYear} 年")
                        add("桥梁全长：${bridge.totalLength} m")
                        add("桥面宽度：${bridge.bridgeWidth} m")
                        add("主跨跨径：${bridge.mainSpan} m")
                        add("养护单位：${bridge.maintenanceUnit}")
                        add("桥梁地址：${bridge.address}")
                        if (preview.bridgeCorrectionNote.isNotBlank()) {
                            add("现场修正说明：${preview.bridgeCorrectionNote}")
                        }
                    },
                )
            }
            item {
                SectionCard {
                    Text("三、检测范围与完成情况", fontWeight = FontWeight.Bold, color = CardText)
                    Spacer(modifier = Modifier.height(12.dp))
                    SummaryMetricGrid(
                        metrics = listOf(
                            "总构件" to preview.totalComponents.toString(),
                            "已采集" to preview.collectedComponents.toString(),
                            "已完成" to preview.completedComponents.toString(),
                        ),
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(report.summary, color = CardText, lineHeight = 22.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("检测依据", color = CardText, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    preview.inspectionBasis.forEachIndexed { index, line ->
                        Text("${index + 1}. $line", color = SubtleText, lineHeight = 20.sp)
                    }
                }
            }
            item {
                SectionCard {
                    Text("四、病害统计", fontWeight = FontWeight.Bold, color = CardText)
                    Spacer(modifier = Modifier.height(12.dp))
                    SummaryMetricGrid(
                        metrics = listOf(
                            "严重" to preview.levelStats.severe.toString(),
                            "中等" to preview.levelStats.medium.toString(),
                            "轻微" to preview.levelStats.minor.toString(),
                            "无病害" to preview.levelStats.normal.toString(),
                        ),
                        columns = 2,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    if (preview.defectTypeStats.isEmpty()) {
                        Text("当前尚无可统计的病害类型。", color = SubtleText)
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            preview.defectTypeStats.forEach { item ->
                                StatusChip(
                                    text = "${item.name} x${item.count}",
                                    color = BridgeBlue,
                                    background = BridgeBlue.copy(alpha = 0.12f),
                                )
                            }
                        }
                    }
                }
            }
            item {
                SectionCard {
                    Text("五、构件病害明细", fontWeight = FontWeight.Bold, color = CardText)
                    Spacer(modifier = Modifier.height(10.dp))
                    if (preview.inspectedComponents.isEmpty()) {
                        Text("当前项目还没有形成可预览的构件检测结果。", color = SubtleText)
                    } else {
                        Text("以下为本次检测已采集或已完成AI/人工校核的构件结果。", color = SubtleText, fontSize = 13.sp)
                    }
                }
            }
            items(preview.inspectedComponents, key = { it.id }) { component ->
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${component.category} / ${component.type} / ${component.number}",
                                color = CardText,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("关注病害：${component.focusDefects.joinToString("、")}", color = SubtleText, fontSize = 12.sp)
                        }
                        ComponentStatusChip(component.status)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("AI初判", color = BridgeBlue, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("病害类型", component.aiDisplayType().ifBlank { "待识别" })
                    InfoRow("病害等级", component.aiDisplayLevel().ifBlank { "待识别" })
                    InfoRow("位置描述", component.aiDisplayLocation().ifBlank { "待补充" })
                    InfoRow("量化信息", component.aiDisplayQuantitativeInfo().ifBlank { "待补充" })
                    InfoRow("AI修复建议", component.aiDisplayRepairAdvice().ifBlank { "待补充" })
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("人工复核结论", color = CardText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StatusChip(
                            text = component.defectType.ifBlank { "待识别" },
                            color = if (component.defectType == "无病害") SuccessGreen else BridgeBlue,
                            background = if (component.defectType == "无病害") SuccessGreen.copy(alpha = 0.12f) else BridgeBlue.copy(alpha = 0.12f),
                        )
                        StatusChip(
                            text = component.defectLevel.ifBlank { "轻微" },
                            color = when (component.defectLevel) {
                                "严重" -> DangerRed
                                "中等" -> WarningOrange
                                else -> SuccessGreen
                            },
                            background = when (component.defectLevel) {
                                "严重" -> DangerRed.copy(alpha = 0.12f)
                                "中等" -> WarningOrange.copy(alpha = 0.12f)
                                else -> SuccessGreen.copy(alpha = 0.12f)
                            },
                        )
                        StatusChip(
                            text = "素材 ${component.mediaItems.size}",
                            color = SubtleText,
                            background = SubtleText.copy(alpha = 0.12f),
                        )
                        StatusChip(
                            text = if (component.hasManualAdjustment()) "已人工调整" else "采纳AI结果",
                            color = if (component.hasManualAdjustment()) WarningOrange else SuccessGreen,
                            background = if (component.hasManualAdjustment()) WarningOrange.copy(alpha = 0.12f) else SuccessGreen.copy(alpha = 0.12f),
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow("位置", component.locationDesc.ifBlank { "待补充" })
                    InfoRow("量化信息", component.quantitativeInfo.ifBlank { "待补充" })
                    InfoRow("人工修复建议", component.repairAdvice.ifBlank { "待补充" })
                    InfoRow("负责人", component.responsibleName.ifBlank { "未指定" })
                    InfoRow("实际执行", component.actualInspectorName.ifBlank { component.responsibleName.ifBlank { "未记录" } })
                    InfoRow("备注", component.remarks.ifBlank { "无" })
                    component.aiConfidence?.let {
                        Text("AI识别置信度：${it}%", color = BridgeBlue, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    if (report.reportStatus != "reported") {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { onEditComponent(component.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("去构件修正")
                        }
                    }
                }
            }
            item {
                ReportSection("六、综合评定与结论", listOf(preview.conclusion, preview.reviewOpinion))
            }
            item {
                ReportSection("七、养护建议", report.maintenanceAdvice)
            }
            item {
                SectionCard {
                    Text("八、签字栏", fontWeight = FontWeight.Bold, color = CardText)
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow("检测人员", "${viewModel.currentUser.realName} / ${project.leaderName}")
                    InfoRow("复核意见", preview.reviewOpinion)
                    InfoRow("归档状态", if (report.reportStatus == "reported") "已上报归档" else "待上报")
                    if (report.reportStatus != "reported") {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            OutlinedButton(
                                onClick = onEditReport,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Text(if (report.reportStatus == "draft") "编辑报告" else "继续完善")
                            }
                            Button(
                                onClick = {
                                    viewModel.markReportReported(reportId)
                                    Toast.makeText(context, "报告已标记为已上报", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BridgeBlue),
                            ) {
                                Text("正式上报")
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    Button(
                        onClick = {
                            Toast.makeText(context, "当前为预览版，PDF导出会在下一步接正式能力", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BridgeBlue),
                    ) {
                        Text("导出 PDF（预览版）")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportEditScreen(
    viewModel: BridgeAiViewModel,
    reportId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val report = viewModel.getReport(reportId)
    val preview = viewModel.buildReportPreview(reportId)
    if (report == null || preview == null) {
        InvalidRouteScreen(title = "编辑报告", message = "报告数据不存在或尚未生成。", onBack = onBack)
        return
    }
    if (report.reportStatus == "reported") {
        InvalidRouteScreen(title = "编辑报告", message = "已上报报告当前仅支持查看，如需修改请先退回后再编辑。", onBack = onBack)
        return
    }

    var reportTitle by rememberSaveable(report.id, report.reportTitle) { mutableStateOf(report.reportTitle) }
    var summary by rememberSaveable(report.id, report.summary) { mutableStateOf(report.summary) }
    var conclusion by rememberSaveable(report.id, preview.conclusion) { mutableStateOf(preview.conclusion) }
    var reviewOpinion by rememberSaveable(report.id, preview.reviewOpinion) { mutableStateOf(preview.reviewOpinion) }
    var maintenanceAdviceText by rememberSaveable(report.id, report.maintenanceAdvice.joinToString("\n")) {
        mutableStateOf(report.maintenanceAdvice.joinToString("\n"))
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(
            title = "编辑报告",
            onBack = onBack,
            actionSlotWidth = 88,
            rightContent = {
                TopBarActionButton(
                    text = "保存",
                    icon = Icons.Default.Check,
                    onClick = {
                        if (reportTitle.isBlank() || summary.isBlank()) {
                            Toast.makeText(context, "请至少补充报告标题和摘要", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateReportDraft(
                                reportId = reportId,
                                title = reportTitle,
                                summary = summary,
                                conclusion = conclusion,
                                reviewOpinion = reviewOpinion,
                                maintenanceAdvice = maintenanceAdviceText
                                    .split("\n")
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() },
                            )
                            Toast.makeText(context, "报告内容已保存", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    },
                )
            },
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                CompactTipStrip("这里编辑的是报告成文内容；如果某个构件事实结果有误，请回报告详情点“去构件修正”。")
            }
            item {
                SectionCard {
                    Text("报告基础信息", fontWeight = FontWeight.Bold, color = CardText)
                    Spacer(modifier = Modifier.height(14.dp))
                    AppFormField(
                        value = reportTitle,
                        onValueChange = { reportTitle = it },
                        label = "报告标题",
                        placeholder = "请输入报告标题",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppFormField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = "摘要",
                        placeholder = "简要概括本次报告的阶段成果",
                        minLines = 3,
                    )
                }
            }
            item {
                SectionCard {
                    Text("综合评定", fontWeight = FontWeight.Bold, color = CardText)
                    Spacer(modifier = Modifier.height(14.dp))
                    AppFormField(
                        value = conclusion,
                        onValueChange = { conclusion = it },
                        label = "检测结论",
                        placeholder = "请输入综合检测结论",
                        minLines = 4,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppFormField(
                        value = reviewOpinion,
                        onValueChange = { reviewOpinion = it },
                        label = "复核意见",
                        placeholder = "请输入复核意见或归档建议",
                        minLines = 3,
                    )
                }
            }
            item {
                SectionCard {
                    Text("养护建议", fontWeight = FontWeight.Bold, color = CardText)
                    Spacer(modifier = Modifier.height(14.dp))
                    AppFormField(
                        value = maintenanceAdviceText,
                        onValueChange = { maintenanceAdviceText = it },
                        label = "建议条目",
                        placeholder = "每行一条养护建议",
                        supporting = "建议一行填写一条，保存后会自动整理成报告条目。",
                        minLines = 5,
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncScreen(
    viewModel: BridgeAiViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncQueue = viewModel.syncQueueEntries()
    val queueCount = viewModel.syncQueueCount()
    val failedCount = viewModel.failedSyncCount()
    val waitingWifiCount = viewModel.waitingWifiCount()
    val networkLabel = syncNetworkLabel(viewModel.networkProfile)
    val lastTriggerLabel = syncTriggerLabel(viewModel.lastSyncTrigger)
    val autoSyncAvailable = viewModel.canAutoSyncNow()
    val workStatusLabel = if (viewModel.workInProgress) "作业中" else "空闲"
    val notice = viewModel.lastSyncNotice

    LaunchedEffect(notice?.id) {
        notice?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(autoSyncAvailable) {
        if (autoSyncAvailable) {
            delay(450)
            if (viewModel.startSync(SyncTrigger.Auto)) {
                delay(900)
                viewModel.finishSync(SyncTrigger.Auto)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        BlueTopBar(title = "数据同步", onBack = onBack)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (notice != null) {
                item {
                    SyncNoticeCard(
                        notice = notice,
                        onDismiss = viewModel::dismissSyncNotice,
                    )
                }
            }
            item {
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(LightBlue),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = BridgeBlue)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                StatusChip("第一期", BridgeBlue, LightBlue.copy(alpha = 0.8f))
                                Text("一键同步", fontWeight = FontWeight.Bold, color = CardText)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (viewModel.networkProfile == SyncNetworkProfile.Offline) {
                                    "当前处于离线模式，数据会继续保存在本地。"
                                } else {
                                    "保留一键同步作为主入口，同时把失败和等待项收进同一队列。"
                                },
                                color = SubtleText,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    SummaryMetricGrid(
                        metrics = listOf(
                            "待同步" to queueCount.toString(),
                            "失败项" to failedCount.toString(),
                            "等 Wi‑Fi" to waitingWifiCount.toString(),
                            "当前网络" to networkLabel,
                        ),
                        columns = 4,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    CompactTipStrip(
                        when {
                            viewModel.isSyncing -> "最近触发：$lastTriggerLabel，系统正在处理队列。"
                            queueCount == 0 -> "当前没有待同步数据，后续新增检测内容会自动进入队列。"
                            else -> "最近触发：$lastTriggerLabel，当前共有 $queueCount 条待处理数据。"
                        },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (viewModel.isSyncing) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(BorderGray),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(viewModel.syncProgress)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(BridgeBlue),
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                if (viewModel.startSync(SyncTrigger.Manual)) {
                                    delay(900)
                                    viewModel.finishSync(SyncTrigger.Manual)
                                }
                            }
                        },
                        enabled = queueCount > 0 &&
                            !viewModel.isSyncing &&
                            viewModel.networkProfile != SyncNetworkProfile.Offline,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BridgeBlue),
                    ) {
                        Text(
                            when {
                                viewModel.isSyncing -> "同步中..."
                                viewModel.networkProfile == SyncNetworkProfile.Offline -> "离线中，暂不可同步"
                                else -> "立即同步"
                            },
                        )
                    }
                }
            }
            item {
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                StatusChip("第二期", SuccessGreen, SuccessGreen.copy(alpha = 0.12f))
                                Text("智能同步策略", fontWeight = FontWeight.Bold, color = CardText)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "自动同步负责“什么时候同步”，一键同步负责“我现在就要同步”。两者并存，更符合现场使用习惯。",
                                color = SubtleText,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                            )
                        }
                        Icon(Icons.Default.Tune, contentDescription = null, tint = BridgeBlue)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    FilterRow(
                        title = "当前网络",
                        options = listOf("离线", "移动网络", "Wi‑Fi"),
                        selected = networkLabel,
                        onSelected = { selected ->
                            viewModel.updateNetworkProfile(
                                when (selected) {
                                    "移动网络" -> SyncNetworkProfile.Mobile
                                    "Wi‑Fi" -> SyncNetworkProfile.Wifi
                                    else -> SyncNetworkProfile.Offline
                                },
                            )
                        },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FilterRow(
                        title = "当前作业状态",
                        options = listOf("空闲", "作业中"),
                        selected = workStatusLabel,
                        onSelected = { selected ->
                            viewModel.updateWorkInProgress(selected == "作业中")
                        },
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    SyncPolicySwitch(
                        title = "网络恢复后自动同步",
                        subtitle = "适合现场采集后不想反复点同步的场景。",
                        checked = viewModel.autoSyncEnabled,
                        onCheckedChange = viewModel::updateAutoSyncEnabled,
                    )
                    HorizontalDivider(color = BorderGray)
                    SyncPolicySwitch(
                        title = "仅 Wi‑Fi 上传媒体",
                        subtitle = "移动网络下优先同步结果与报告，图片/视频等待 Wi‑Fi。",
                        checked = viewModel.mediaSyncOnWifiOnly,
                        onCheckedChange = viewModel::updateMediaSyncOnWifiOnly,
                    )
                    HorizontalDivider(color = BorderGray)
                    SyncPolicySwitch(
                        title = "作业中暂停自动同步",
                        subtitle = "避免正在检测时频繁占用网络和注意力。",
                        checked = viewModel.pauseAutoSyncDuringWork,
                        onCheckedChange = viewModel::updatePauseAutoSyncDuringWork,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CompactTipStrip(viewModel.autoSyncStatusText())
                }
            }
            item {
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("同步队列", fontWeight = FontWeight.Bold, color = CardText)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "把构件素材和报告内容统一收进一个队列，用户更容易判断还差什么没有上云。",
                                color = SubtleText,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                            )
                        }
                        Icon(Icons.Default.Sync, contentDescription = null, tint = BridgeBlue)
                    }
                    if (syncQueue.isEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        SyncEmptyStateCard(
                            title = if (notice?.tone == SyncNoticeTone.Success) "当前已同步完成" else "当前队列为空",
                            message = when {
                                notice != null -> notice.message
                                viewModel.autoSyncEnabled -> "当前没有待处理数据，后续新增内容会自动进入队列并按策略继续同步。"
                                else -> "当前没有待处理数据，后续新增内容会进入队列，等你手动发起同步。"
                            },
                            caption = "最近触发：$lastTriggerLabel",
                        )
                    }
                }
            }
            items(syncQueue, key = { it.key }) { entry ->
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                StatusChip(
                                    text = entry.category,
                                    color = BridgeBlue,
                                    background = LightBlue.copy(alpha = 0.72f),
                                )
                                SyncStateChip(entry.state)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = entry.title,
                                fontWeight = FontWeight.Bold,
                                color = CardText,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = entry.subtitle,
                                color = SubtleText,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                            )
                            if (entry.state == SyncEntryState.WaitingWifi) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "当前为移动网络，媒体素材会在连接 Wi‑Fi 后自动继续上传。",
                                    color = BridgeBlue,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                )
                            }
                        }
                        if (entry.state == SyncEntryState.Failed) {
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedButton(
                                onClick = { viewModel.retrySyncEntry(entry.key) },
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("重试")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncPolicySwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = CardText, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = SubtleText, fontSize = 12.sp, lineHeight = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SyncStateChip(state: SyncEntryState) {
    val (text, color, background) = when (state) {
        SyncEntryState.Failed -> Triple("失败", DangerRed, DangerRed.copy(alpha = 0.12f))
        SyncEntryState.Syncing -> Triple("同步中", BridgeBlue, LightBlue.copy(alpha = 0.9f))
        SyncEntryState.Pending -> Triple("待同步", WarningOrange, WarningOrange.copy(alpha = 0.12f))
        SyncEntryState.WaitingWifi -> Triple("等 Wi‑Fi", SubtleText, BorderGray.copy(alpha = 0.55f))
    }
    StatusChip(text = text, color = color, background = background)
}

@Composable
private fun SyncNoticeCard(
    notice: SyncNotice,
    onDismiss: () -> Unit,
) {
    val (iconBackground, titleColor, messageColor) = when (notice.tone) {
        SyncNoticeTone.Success -> Triple(SuccessGreen.copy(alpha = 0.14f), SuccessGreen, CardText)
        SyncNoticeTone.Info -> Triple(LightBlue.copy(alpha = 0.95f), BridgeBlue, CardText)
        SyncNoticeTone.Warning -> Triple(WarningOrange.copy(alpha = 0.14f), WarningOrange, CardText)
    }
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.TaskAlt, contentDescription = null, tint = titleColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notice.title, color = titleColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(notice.message, color = messageColor, fontSize = 13.sp, lineHeight = 20.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onDismiss) {
                Text("收起")
            }
        }
    }
}

@Composable
private fun SyncEmptyStateCard(
    title: String,
    message: String,
    caption: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightBlue.copy(alpha = 0.55f))
            .padding(horizontal = 16.dp, vertical = 18.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.TaskAlt, contentDescription = null, tint = SuccessGreen)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = CardText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                color = SubtleText,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(10.dp))
            StatusChip(
                text = caption,
                color = BridgeBlue,
                background = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF7F8FA))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, fontWeight = FontWeight.Bold, color = CardText, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                color = SubtleText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SummaryMetricGrid(
    metrics: List<Pair<String, String>>,
    columns: Int = 3,
) {
    metrics.chunked(columns).forEachIndexed { rowIndex, row ->
        if (rowIndex > 0) Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            row.forEach { (label, value) ->
                SummaryMetric(
                    label = label,
                    value = value,
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(columns - row.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FilterRow(
    title: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    Column {
        if (title.isNotBlank()) {
            Text(title, color = CardText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    label = option,
                    onClick = { onSelected(option) },
                )
            }
        }
    }
}

@Composable
private fun CompactTipStrip(
    text: String,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightBlue.copy(alpha = 0.55f)),
        border = BorderStroke(1.dp, BridgeBlue.copy(alpha = 0.08f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            color = CardText,
            fontSize = 12.sp,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun StickyListHeaderCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(color = AppBackground) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun HeaderMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HeaderMetricGrid(
    metrics: List<Pair<String, String>>,
    columns: Int = 3,
) {
    metrics.chunked(columns).forEachIndexed { rowIndex, row ->
        if (rowIndex > 0) Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            row.forEach { (label, value) ->
                HeaderMetric(
                    label = label,
                    value = value,
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(columns - row.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
) {
    SectionCard(modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightBlue),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = BridgeBlue)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = CardText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = SubtleText, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (onClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = SubtleText,
                )
            }
        }
    }
}

@Composable
private fun ComponentStatusChip(status: ComponentStatus) {
    val (text, color) = when (status) {
        ComponentStatus.Pending -> "未检测" to SubtleText
        ComponentStatus.Collected -> "已采集" to BridgeBlue
        ComponentStatus.AiDone -> "已识别" to WarningOrange
        ComponentStatus.Completed -> "已完成" to SuccessGreen
    }
    StatusChip(text = text, color = color, background = color.copy(alpha = 0.12f))
}

private fun ComponentStatus.compactActionText(): String = when (this) {
    ComponentStatus.Pending -> "开始检测"
    ComponentStatus.Collected -> "查看素材并识别"
    ComponentStatus.AiDone -> "核对AI结果"
    ComponentStatus.Completed -> "查看结果"
}

private fun String.displayProjectStatus(): String = when (this) {
    "completed" -> "已完成"
    "in_progress" -> "检测中"
    "draft" -> "待开始"
    else -> "检测中"
}

@Composable
private fun TopBarActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AppDialog(
    title: String,
    subtitle: String? = null,
    onDismissRequest: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .clip(RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp)) {
                Text(
                    text = title,
                    color = CardText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = subtitle,
                        color = SubtleText,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                content()
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions,
                )
            }
        }
    }
}

@Composable
private fun AppEditorDialog(
    title: String,
    subtitle: String? = null,
    onDismissRequest: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val overlayInteraction = remember { MutableInteractionSource() }
    val panelInteraction = remember { MutableInteractionSource() }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.26f))
                .clickable(
                    interactionSource = overlayInteraction,
                    indication = null,
                ) {
                    focusManager.clearFocus(force = true)
                    onDismissRequest()
                }
                .padding(horizontal = 12.dp, vertical = 18.dp)
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .clickable(
                        interactionSource = panelInteraction,
                        indication = null,
                    ) {
                        focusManager.clearFocus(force = true)
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp, vertical = 20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                color = CardText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            if (!subtitle.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = subtitle,
                                    color = SubtleText,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = {
                                focusManager.clearFocus(force = true)
                                onDismissRequest()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = SubtleText,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    CompactTipStrip("点空白处可收起输入焦点，点右上角可直接退出当前编辑。")
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        content()
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    HorizontalDivider(color = BorderGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions,
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogSectionTitle(text: String) {
    Text(
        text = text,
        color = CardText,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun DialogHighlightBlock(
    text: String,
    accent: Boolean = false,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (accent) LightBlue else Color(0xFFF7F8FA))
            .border(
                1.dp,
                if (accent) BridgeBlue.copy(alpha = 0.16f) else BorderGray,
                RoundedCornerShape(18.dp),
            )
            .padding(16.dp),
    ) {
        Text(
            text = text,
            color = CardText,
            fontSize = 15.sp,
            lineHeight = 24.sp,
        )
    }
}

@Composable
private fun AppFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    supporting: String? = null,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = minLines == 1,
        minLines = minLines,
        shape = RoundedCornerShape(18.dp),
        label = { Text(label) },
        placeholder = { Text(placeholder, color = SubtleText.copy(alpha = 0.85f)) },
        supportingText = supporting?.let { { Text(it) } },
    )
}

@Composable
private fun DialogTextAction(
    text: String,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick) {
        Text(text)
    }
}

@Composable
private fun DialogSecondaryAction(
    text: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderGray),
    ) {
        Text(text, color = BridgeBlue)
    }
}

@Composable
private fun DialogPrimaryAction(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BridgeBlue),
    ) {
        Text(text)
    }
}

@Composable
private fun CompactActionChip(
    text: String,
    icon: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val contentColor = if (enabled) BridgeBlue else SubtleText
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (enabled) Color.White else AppBackground)
            .border(1.dp, if (enabled) BorderGray else BorderGray.copy(alpha = 0.7f), RoundedCornerShape(999.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
            Text(text, color = contentColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SelectionGrid(
    options: List<String>,
    selectedOption: String,
    columns: Int,
    stretchSingleLastItem: Boolean = false,
    onSelect: (String) -> Unit,
) {
    options.chunked(columns).forEachIndexed { rowIndex, row ->
        if (rowIndex > 0) Spacer(modifier = Modifier.height(10.dp))
        if (stretchSingleLastItem && row.size == 1) {
            SelectionGridChip(
                label = row.first(),
                selected = selectedOption == row.first(),
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSelect(row.first()) },
            )
            return@forEachIndexed
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            row.forEach { option ->
                SelectionGridChip(
                    label = option,
                    selected = selectedOption == option,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelect(option) },
                )
            }
            repeat(columns - row.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SelectionGridChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) BridgeBlue else Color(0xFFF9FAFC))
            .border(1.dp, if (selected) BridgeBlue else BorderGray.copy(alpha = 0.9f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else CardText,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun FilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) BridgeBlue else Color.White)
            .border(1.dp, if (selected) BridgeBlue else BorderGray, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(label, color = if (selected) Color.White else CardText, fontSize = 13.sp)
    }
}

@Composable
private fun AiDetectionOverlay(
    defectLabel: String,
    measurementText: String,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = 34.dp, y = 46.dp)
                .size(width = 148.dp, height = 64.dp)
                .border(2.dp, WarningOrange, RoundedCornerShape(12.dp)),
        )
        Box(
            modifier = Modifier
                .offset(x = 188.dp, y = 118.dp)
                .size(width = 108.dp, height = 52.dp)
                .border(2.dp, DangerRed, RoundedCornerShape(12.dp)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.56f))
                .padding(12.dp),
        ) {
            Column {
                Text(
                    defectLabel,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    measurementText,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun ReportSection(title: String, lines: List<String>) {
    SectionCard {
        Text(title, fontWeight = FontWeight.Bold, color = CardText)
        Spacer(modifier = Modifier.height(10.dp))
        lines.forEachIndexed { index, line ->
            Text(line, color = CardText, lineHeight = 22.sp)
            if (index != lines.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    border: BorderStroke? = null,
    onClick: () -> Unit,
) {
    if (background == Color.White) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(54.dp),
            shape = RoundedCornerShape(14.dp),
            border = border,
        ) {
            Icon(icon, contentDescription = null, tint = textColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = textColor)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier.height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = background, contentColor = textColor),
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                label,
                color = SubtleText,
                modifier = Modifier.width(92.dp),
                fontSize = 13.sp,
                lineHeight = 20.sp,
            )
            Text(
                value,
                color = CardText,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = BorderGray)
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun AiReferenceHint(text: String) {
    Spacer(modifier = Modifier.height(6.dp))
    Text(text, color = SubtleText, fontSize = 12.sp, lineHeight = 18.sp)
}

@Composable
private fun ComparisonInfoRow(
    label: String,
    currentValue: String,
    aiValue: String,
) {
    InfoRow(label, currentValue)
    if (currentValue != aiValue) {
        Text("AI初判参考：$aiValue", color = SubtleText, fontSize = 12.sp, lineHeight = 18.sp)
        Spacer(modifier = Modifier.height(10.dp))
    }
}

internal fun ComponentStatus.toDisplayText(): String = when (this) {
    ComponentStatus.Pending -> "未检测"
    ComponentStatus.Collected -> "已采集"
    ComponentStatus.AiDone -> "已识别"
    ComponentStatus.Completed -> "已完成"
}

private fun ComponentTask.aiDisplayType(): String = aiDetectedType.ifBlank { defectType }

private fun ComponentTask.aiDisplayLevel(): String = aiDetectedLevel.ifBlank { defectLevel }

private fun ComponentTask.aiDisplayLocation(): String = aiDetectedLocation.ifBlank { locationDesc }

private fun ComponentTask.aiDisplayQuantitativeInfo(): String = aiQuantitativeInfo.ifBlank { quantitativeInfo }

private fun ComponentTask.aiDisplayRepairAdvice(): String = aiRepairAdvice.ifBlank { repairAdvice }

private fun ComponentTask.hasManualAdjustment(): Boolean {
    return defectType != aiDisplayType() ||
        defectLevel != aiDisplayLevel() ||
        locationDesc != aiDisplayLocation() ||
        quantitativeInfo != aiDisplayQuantitativeInfo() ||
        repairAdvice != aiDisplayRepairAdvice()
}

private fun syncNetworkLabel(profile: SyncNetworkProfile): String = when (profile) {
    SyncNetworkProfile.Offline -> "离线"
    SyncNetworkProfile.Mobile -> "移动网络"
    SyncNetworkProfile.Wifi -> "Wi‑Fi"
}

private fun syncTriggerLabel(trigger: SyncTrigger?): String = when (trigger) {
    SyncTrigger.Auto -> "自动同步"
    SyncTrigger.Manual -> "手动同步"
    null -> "尚未触发"
}

private fun quickCollectSummary(component: ComponentTask): String {
    val defect = component.defectType.takeIf { it != "待识别" } ?: component.focusDefects.firstOrNull() ?: "待判断病害"
    val place = component.locationDesc.ifBlank { "${component.number}${component.type}" }
    return "$place 已建立采集批次，重点关注 $defect。照片、语音和 AI 提炼结果会一起离线保存。"
}

private fun quickVoiceTranscript(component: ComponentTask, round: Int = 1): String {
    val defect = component.focusDefects.firstOrNull() ?: component.defectType.ifBlank { "裂缝" }
    return if (round % 2 == 0) {
        "这几张是${component.number}${component.type}的补拍，照片编号接着上一组，主要看${defect}范围有没有扩大，现场看着比上午那组更明显一点。"
    } else {
        "我是${component.responsibleName.ifBlank { "现场检测员" }}，现在拍的是${component.number}${component.type}，这组照片先记为一组，位置在构件表面，主要问题是${defect}，后面需要复核尺寸和处置建议。"
    }
}

private fun quickExtractedText(component: ComponentTask, round: Int = 1): String {
    val defect = component.focusDefects.firstOrNull() ?: component.defectType.ifBlank { "裂缝" }
    val photoRange = if (round % 2 == 0) "本组补拍照片" else "本组照片 P1-P${component.mediaItems.size.coerceAtLeast(3)}"
    return "关联照片：$photoRange\n位置：${component.number}${component.type}\n病害：$defect\n记录方式：语音转写后自动提炼，待人工确认。"
}

private fun quickReportText(component: ComponentTask, round: Int = 1): String {
    val defect = component.focusDefects.firstOrNull() ?: component.defectType.ifBlank { "裂缝" }
    val action = if (round % 2 == 0) "建议与前序照片对比，复核病害范围变化。" else "建议结合现场量测结果完善尺寸记录，并纳入报告候选图片。"
    return "${component.number}${component.type}现场采集照片显示，构件局部存在${defect}相关异常。$action"
}

private fun saveBitmapToCache(context: android.content.Context, bitmap: Bitmap): Uri? {
    return try {
        val file = File(context.cacheDir, "bridge_ai_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, stream)
        }
        Uri.fromFile(file)
    } catch (_: Exception) {
        null
    }
}
