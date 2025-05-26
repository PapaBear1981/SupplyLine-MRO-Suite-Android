package com.example.supplyline_mro_suite.presentation.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Hilt temporarily removed
// import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.supplyline_mro_suite.presentation.components.BottomNavigationBar
import com.example.supplyline_mro_suite.presentation.navigation.Screen
import com.example.supplyline_mro_suite.presentation.viewmodel.DashboardViewModel
import com.example.supplyline_mro_suite.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    // viewModel: DashboardViewModel = hiltViewModel() // Temporarily disabled
) {
    // Temporary placeholder values
    val isRefreshing = false
    // val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dashboard",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Welcome back, Chris",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Scanner.route) }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                    }
                    IconButton(onClick = { /* Open notifications */ }) {
                        Badge(
                            containerColor = AerospaceError
                        ) {
                            Text("3")
                        }
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { /* viewModel.refresh() */ },
            state = pullToRefreshState,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Temporarily show content directly
            DashboardContent(
                uiState = null,
                navController = navController,
                onAlertDismiss = { /* alertId -> viewModel.dismissAlert(alertId) */ }
            )
        }

        // Error handling temporarily disabled
        // uiState.error?.let { error ->
        //     LaunchedEffect(error) {
        //         // Show snackbar or error dialog
        //         viewModel.clearError()
        //     }
        // }
    }
}

@Composable
fun DashboardContent(
    uiState: Any? = null, // Placeholder for now
    modifier: Modifier = Modifier,
    navController: NavController,
    onAlertDismiss: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick stats cards
        item {
            Text(
                text = "Quick Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(getQuickStats()) { index, stat ->
                    AnimatedStatsCard(
                        stat = stat,
                        delay = index * 100L
                    )
                }
            }
        }

        // Action buttons
        item {
            Text(
                text = "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionCard(
                    title = "Scan Tool",
                    icon = Icons.Default.QrCodeScanner,
                    color = AerospacePrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Scanner.route) }
                )

                ActionCard(
                    title = "View Tools",
                    icon = Icons.Default.Build,
                    color = AerospaceAccent,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Tools.route) }
                )
            }
        }

        // Recent activity
        item {
            Text(
                text = "Recent Activity",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    getRecentActivities().forEach { activity ->
                        ActivityItem(activity = activity)
                        if (activity != getRecentActivities().last()) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        // Alerts section
        item {
            Text(
                text = "Alerts & Notifications",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AlertCard(
                    title = "3 Tools Overdue",
                    description = "Tools need to be returned",
                    icon = Icons.Default.Warning,
                    color = AerospaceWarning
                )

                AlertCard(
                    title = "5 Chemicals Expiring Soon",
                    description = "Check expiration dates",
                    icon = Icons.Default.Science,
                    color = AerospaceInfo
                )
            }
        }
    }
}

@Composable
fun AnimatedStatsCard(
    stat: StatItem,
    delay: Long
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "stats_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "stats_alpha"
    )

    Card(
        modifier = Modifier
            .width(140.dp)
            .scale(scale)
            .alpha(alpha),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = stat.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = stat.icon,
                contentDescription = null,
                tint = stat.color,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stat.value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = stat.color
            )

            Text(
                text = stat.label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
fun ActivityItem(activity: ActivityData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = activity.icon,
            contentDescription = null,
            tint = activity.color,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = activity.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Text(
            text = activity.time,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun AlertCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = color
            )
        }
    }
}

// Data classes and sample data
data class StatItem(
    val value: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

data class ActivityData(
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val color: Color
)

fun getQuickStats() = listOf(
    StatItem("142", "Total Tools", Icons.Default.Build, AerospacePrimary),
    StatItem("23", "Checked Out", Icons.Default.Assignment, AerospaceWarning),
    StatItem("3", "Overdue", Icons.Default.Warning, AerospaceError),
    StatItem("89", "Chemicals", Icons.Default.Science, AerospaceAccent)
)

fun getRecentActivities() = listOf(
    ActivityData(
        "Tool HT001 checked out",
        "Torque wrench checked out by John Doe",
        "2 hours ago",
        Icons.Default.Build,
        AerospacePrimary
    ),
    ActivityData(
        "Chemical issued",
        "Sealant PR-1422 issued to Hangar 3",
        "4 hours ago",
        Icons.Default.Science,
        AerospaceAccent
    ),
    ActivityData(
        "Tool returned",
        "Drill HT045 returned by Jane Smith",
        "6 hours ago",
        Icons.Default.CheckCircle,
        AerospaceSuccess
    )
)
