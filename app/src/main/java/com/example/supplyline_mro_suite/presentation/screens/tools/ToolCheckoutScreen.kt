package com.example.supplyline_mro_suite.presentation.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.supplyline_mro_suite.presentation.viewmodel.ToolCheckoutViewModel
import com.example.supplyline_mro_suite.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolCheckoutScreen(
    navController: NavController,
    toolId: Int,
    viewModel: ToolCheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(toolId) {
        viewModel.loadToolForCheckout(toolId)
    }

    LaunchedEffect(uiState.checkoutSuccess, uiState.returnSuccess) {
        if (uiState.checkoutSuccess || uiState.returnSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.activeCheckout != null) "Return Tool" else "Check Out Tool",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AerospacePrimary)
                }
            }
            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error ?: "Unknown error",
                    onRetry = { viewModel.loadToolForCheckout(toolId) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            uiState.tool != null -> {
                if (uiState.activeCheckout != null) {
                    ReturnToolContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    CheckoutToolContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }

        // Error snackbar
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                viewModel.clearError()
            }
        }
    }
}

@Composable
fun CheckoutToolContent(
    uiState: com.example.supplyline_mro_suite.presentation.viewmodel.ToolCheckoutUiState,
    viewModel: ToolCheckoutViewModel,
    modifier: Modifier = Modifier
) {
    val tool = uiState.tool!!

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tool info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = ToolAvailable.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = ToolAvailable,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tool.toolNumber,
                            fontSize = 18.sp,
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
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Category: ${tool.category}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Location: ${tool.location}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Expected return date
        OutlinedTextField(
            value = uiState.expectedReturnDate,
            onValueChange = viewModel::updateExpectedReturnDate,
            label = { Text("Expected Return Date") },
            placeholder = { Text("YYYY-MM-DD") },
            leadingIcon = {
                Icon(Icons.Default.DateRange, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = !viewModel.isValidReturnDate(uiState.expectedReturnDate)
        )

        // Notes
        OutlinedTextField(
            value = uiState.checkoutNotes,
            onValueChange = viewModel::updateCheckoutNotes,
            label = { Text("Notes (Optional)") },
            placeholder = { Text("Add any notes about this checkout...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.weight(1f))

        // Checkout button
        Button(
            onClick = { viewModel.checkoutTool() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.isProcessing &&
                     viewModel.isValidReturnDate(uiState.expectedReturnDate),
            colors = ButtonDefaults.buttonColors(
                containerColor = AerospacePrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Check Out Tool",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ReturnToolContent(
    uiState: com.example.supplyline_mro_suite.presentation.viewmodel.ToolCheckoutUiState,
    viewModel: ToolCheckoutViewModel,
    modifier: Modifier = Modifier
) {
    val tool = uiState.tool!!
    val checkout = uiState.activeCheckout!!

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tool info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = ToolCheckedOut.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = ToolCheckedOut,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tool.toolNumber,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tool.description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Checked out: ${checkout.checkoutDate}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Due: ${checkout.expectedReturnDate}",
                            fontSize = 12.sp,
                            color = if (LocalDate.parse(checkout.expectedReturnDate).isBefore(LocalDate.now()))
                                AerospaceError else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Return condition
        var expanded by remember { mutableStateOf(false) }

        @OptIn(ExperimentalMaterial3Api::class)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = uiState.returnCondition.ifEmpty { "Good" },
                onValueChange = { },
                readOnly = true,
                label = { Text("Return Condition") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                viewModel.getAvailableConditions().forEach { condition ->
                    DropdownMenuItem(
                        text = { Text(condition) },
                        onClick = {
                            viewModel.updateReturnCondition(condition)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Return notes
        OutlinedTextField(
            value = uiState.returnNotes,
            onValueChange = viewModel::updateReturnNotes,
            label = { Text("Return Notes (Optional)") },
            placeholder = { Text("Add any notes about the tool condition...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.weight(1f))

        // Return button
        Button(
            onClick = { viewModel.returnTool() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.isProcessing,
            colors = ButtonDefaults.buttonColors(
                containerColor = AerospacePrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Return Tool",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
