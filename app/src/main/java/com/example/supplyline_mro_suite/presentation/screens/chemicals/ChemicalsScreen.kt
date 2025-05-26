package com.example.supplyline_mro_suite.presentation.screens.chemicals

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
fun ChemicalsScreen(navController: NavController) {
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
                        text = "Chemicals",
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
                onClick = { /* Add new chemical */ },
                containerColor = AerospaceAccent,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Chemical")
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
                    color = AerospaceAccent
                )
            }
        } else {
            ChemicalsContent(
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
fun ChemicalsContent(
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
            label = { Text("Search chemicals...") },
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
            items(getChemicalFilters()) { filter ->
                FilterChip(
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter) },
                    selected = selectedFilter == filter,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AerospaceAccent,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        
        // Chemicals list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(getSampleChemicals()) { chemical ->
                AnimatedChemicalCard(chemical = chemical)
            }
        }
    }
}

@Composable
fun AnimatedChemicalCard(chemical: ChemicalItem) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(chemical) {
        delay(100)
        visible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chemical_card_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "chemical_card_alpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { /* Navigate to chemical detail */ }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chemical icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = chemical.statusColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = chemical.statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Chemical info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chemical.partNumber,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = chemical.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Lot: ${chemical.lotNumber}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                // Status badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = chemical.statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = chemical.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = chemical.statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Additional info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Quantity",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${chemical.quantity} ${chemical.unit}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text(
                        text = "Location",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = chemical.location,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text(
                        text = "Expires",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = chemical.expirationDate,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = chemical.statusColor
                    )
                }
            }
        }
    }
}

// Data classes and sample data
data class ChemicalItem(
    val id: Int,
    val partNumber: String,
    val lotNumber: String,
    val description: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val location: String,
    val expirationDate: String,
    val status: String,
    val statusColor: Color
)

fun getChemicalFilters() = listOf("All", "Good", "Expiring", "Low Stock", "Sealant", "Paint", "Adhesive")

fun getSampleChemicals() = listOf(
    ChemicalItem(1, "PR-1422", "LOT001", "Fuel Tank Sealant", "Sealant", 500.0, "ml", "Storage A", "2024-12-15", "Good", ChemicalGood),
    ChemicalItem(2, "EC-776", "LOT002", "Primer Paint", "Paint", 250.0, "ml", "Paint Shop", "2024-06-30", "Expiring", ChemicalExpiring),
    ChemicalItem(3, "AF-163", "LOT003", "Structural Adhesive", "Adhesive", 100.0, "ml", "Storage B", "2025-03-20", "Good", ChemicalGood),
    ChemicalItem(4, "SK-70", "LOT004", "Skydrol Hydraulic Fluid", "Hydraulic", 50.0, "L", "Hydraulic Shop", "2024-08-15", "Low Stock", ChemicalLowStock),
    ChemicalItem(5, "PR-1440", "LOT005", "Wing Tank Sealant", "Sealant", 750.0, "ml", "Storage A", "2025-01-10", "Good", ChemicalGood),
    ChemicalItem(6, "EC-838", "LOT006", "Topcoat Paint", "Paint", 300.0, "ml", "Paint Shop", "2024-05-25", "Expiring", ChemicalExpiring)
)
