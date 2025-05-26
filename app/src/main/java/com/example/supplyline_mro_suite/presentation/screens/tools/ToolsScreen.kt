package com.example.supplyline_mro_suite.presentation.screens.tools

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.supplyline_mro_suite.presentation.components.BottomNavigationBar
import com.example.supplyline_mro_suite.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var isLoading by remember { mutableStateOf(true) }

    // Simulate loading
    LaunchedEffect(Unit) {
        delay(800)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tools",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* Open search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* Open filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add new tool */ },
                containerColor = AerospacePrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Tool")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = AerospacePrimary
                )
            }
        } else {
            ToolsContent(
                modifier = Modifier.padding(paddingValues),
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it }
            )
        }
    }
}

@Composable
fun ToolsContent(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search tools...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        )

        // Filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(getToolFilters()) { filter ->
                FilterChip(
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter) },
                    selected = selectedFilter == filter,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AerospacePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Tools list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(getSampleTools()) { tool ->
                AnimatedToolCard(tool = tool)
            }
        }
    }
}

@Composable
fun AnimatedToolCard(tool: ToolItem) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(tool) {
        delay(100)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tool_card_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "tool_card_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { /* Navigate to tool detail */ }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tool icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = tool.statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = tool.statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Tool info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.toolNumber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tool.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Serial: ${tool.serialNumber}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Status badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = tool.statusColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = tool.status,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = tool.statusColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// Data classes and sample data
data class ToolItem(
    val id: Int,
    val toolNumber: String,
    val serialNumber: String,
    val description: String,
    val category: String,
    val status: String,
    val statusColor: Color
)

fun getToolFilters() = listOf("All", "Available", "Checked Out", "Maintenance", "CL415", "RJ85", "Q400")

fun getSampleTools() = listOf(
    ToolItem(1, "HT001", "SN123456", "Torque Wrench 1/2\"", "General", "Available", ToolAvailable),
    ToolItem(2, "HT002", "SN123457", "Drill Set Complete", "General", "Checked Out", ToolCheckedOut),
    ToolItem(3, "HT003", "SN123458", "Hydraulic Jack", "CL415", "Available", ToolAvailable),
    ToolItem(4, "HT004", "SN123459", "Engine Hoist", "Engine", "Maintenance", ToolMaintenance),
    ToolItem(5, "HT005", "SN123460", "Rivet Gun", "Sheetmetal", "Available", ToolAvailable),
    ToolItem(6, "HT006", "SN123461", "Multimeter", "General", "Checked Out", ToolCheckedOut),
    ToolItem(7, "HT007", "SN123462", "Pneumatic Wrench", "General", "Available", ToolAvailable),
    ToolItem(8, "HT008", "SN123463", "Borescope", "Engine", "Available", ToolAvailable)
)
