package com.example.supplyline_mro_suite.presentation.screens.tools

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.supplyline_mro_suite.presentation.components.BottomNavigationBar
import com.example.supplyline_mro_suite.presentation.navigation.Screen
import com.example.supplyline_mro_suite.presentation.viewmodel.ToolListViewModel
import com.example.supplyline_mro_suite.presentation.viewmodel.ToolFilter
import com.example.supplyline_mro_suite.presentation.viewmodel.ToolSort
import com.example.supplyline_mro_suite.data.model.ToolWithCheckout
import com.example.supplyline_mro_suite.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    navController: NavController,
    viewModel: ToolListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showSearchBar by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showSearchBar) {
                SearchTopBar(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onCloseSearch = { showSearchBar = false }
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Tools",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.tools.size} tools",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (uiState.selectedFilter != ToolFilter.ALL)
                                    AerospacePrimary else LocalContentColor.current
                            )
                        }
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.Scanner.route) },
                containerColor = AerospacePrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Tool")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = AerospacePrimary,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading tools...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.refreshTools() },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                uiState.tools.isEmpty() -> {
                    EmptyContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Filter chips
                        item {
                            FilterChipsRow(
                                selectedFilter = uiState.selectedFilter,
                                onFilterSelected = viewModel::updateFilter
                            )
                        }

                        // Tools list
                        items(
                            items = uiState.tools,
                            key = { it.tool.id }
                        ) { toolWithCheckout ->
                            EnhancedToolCard(
                                toolWithCheckout = toolWithCheckout,
                                onClick = {
                                    navController.navigate(
                                        Screen.ToolDetail.createRoute(toolWithCheckout.tool.id)
                                    )
                                },
                                onCheckoutClick = {
                                    navController.navigate(
                                        Screen.CheckoutTool.createRoute(toolWithCheckout.tool.id)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showFilterDialog) {
            FilterDialog(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { filter ->
                    viewModel.updateFilter(filter)
                    showFilterDialog = false
                },
                onDismiss = { showFilterDialog = false }
            )
        }

        if (showSortDialog) {
            SortDialog(
                selectedSort = uiState.selectedSort,
                onSortSelected = { sort ->
                    viewModel.updateSort(sort)
                    showSortDialog = false
                },
                onDismiss = { showSortDialog = false }
            )
        }

        // Error snackbar
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show snackbar or handle error
                viewModel.clearError()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search tools...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onCloseSearch) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Close search")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun FilterChipsRow(
    selectedFilter: ToolFilter,
    onFilterSelected: (ToolFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(ToolFilter.values()) { filter ->
            FilterChip(
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.displayName) },
                selected = selectedFilter == filter,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AerospacePrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun EnhancedToolCard(
    toolWithCheckout: ToolWithCheckout,
    onClick: () -> Unit,
    onCheckoutClick: () -> Unit
) {
    val tool = toolWithCheckout.tool
    val statusColor = when (tool.status) {
        "Available" -> ToolAvailable
        "Checked Out" -> ToolCheckedOut
        "Maintenance" -> ToolMaintenance
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tool icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = statusColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = statusColor,
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
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = tool.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Additional info row
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${tool.category} • ${tool.location}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                if (tool.status == "Available") {
                    Button(
                        onClick = onCheckoutClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AerospacePrimary
                        ),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "Check Out",
                            fontSize = 12.sp
                        )
                    }
                } else if (toolWithCheckout.checkedOutUser != null) {
                    Text(
                        text = "Checked out to: ${toolWithCheckout.checkedOutUser.name}",
                        fontSize = 12.sp,
                        color = ToolCheckedOut
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            tint = AerospaceError,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error loading tools",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = AerospacePrimary
            )
        ) {
            Text("Retry")
        }
    }
}

@Composable
fun EmptyContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Build,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No tools found",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try adjusting your search or filters",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun FilterDialog(
    selectedFilter: ToolFilter,
    onFilterSelected: (ToolFilter) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Tools") },
        text = {
            LazyColumn {
                items(ToolFilter.values()) { filter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFilterSelected(filter) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFilter == filter,
                            onClick = { onFilterSelected(filter) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(filter.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SortDialog(
    selectedSort: ToolSort,
    onSortSelected: (ToolSort) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort Tools") },
        text = {
            LazyColumn {
                items(ToolSort.values()) { sort ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortSelected(sort) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSort == sort,
                            onClick = { onSortSelected(sort) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(sort.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}


