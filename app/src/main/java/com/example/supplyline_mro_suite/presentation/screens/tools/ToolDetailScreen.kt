package com.example.supplyline_mro_suite.presentation.screens.tools

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.supplyline_mro_suite.presentation.components.AnimatedCounter
import com.example.supplyline_mro_suite.presentation.components.BouncyCard
import com.example.supplyline_mro_suite.presentation.components.GradientButton
import com.example.supplyline_mro_suite.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolDetailScreen(
    navController: NavController,
    toolId: String
) {
    var isLoading by remember { mutableStateOf(true) }
    var currentImageIndex by remember { mutableStateOf(0) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    
    // Simulate loading
    LaunchedEffect(Unit) {
        delay(800)
        isLoading = false
    }
    
    val tool = getSampleToolDetail(toolId)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tool.toolNumber,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share tool */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { /* Favorite tool */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
            ToolDetailContent(
                tool = tool,
                modifier = Modifier.padding(paddingValues),
                onCheckoutClick = { showCheckoutDialog = true }
            )
        }
        
        if (showCheckoutDialog) {
            CheckoutDialog(
                tool = tool,
                onDismiss = { showCheckoutDialog = false },
                onConfirm = { 
                    showCheckoutDialog = false
                    // Handle checkout
                }
            )
        }
    }
}

@Composable
fun ToolDetailContent(
    tool: ToolDetail,
    modifier: Modifier = Modifier,
    onCheckoutClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tool image carousel
        item {
            SwipeableImageCarousel(
                images = tool.images,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
        
        // Tool info card
        item {
            AnimatedToolInfoCard(tool = tool)
        }
        
        // Status and actions
        item {
            StatusAndActionsCard(
                tool = tool,
                onCheckoutClick = onCheckoutClick
            )
        }
        
        // Specifications
        item {
            SpecificationsCard(specifications = tool.specifications)
        }
        
        // History
        item {
            HistoryCard(history = tool.history)
        }
    }
}

@Composable
fun SwipeableImageCarousel(
    images: List<String>,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GradientStart.copy(alpha = 0.8f),
                        GradientEnd.copy(alpha = 0.8f)
                    )
                )
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // Handle swipe to next/previous image
                    }
                ) { _, _ ->
                    // Handle drag
                }
            }
    ) {
        // Placeholder for tool image
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.White.copy(alpha = 0.8f)
            )
        }
        
        // Image indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(images.size) { index ->
                val isSelected = index == currentIndex
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 12.dp else 8.dp)
                        .background(
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun AnimatedToolInfoCard(tool: ToolDetail) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tool_info_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "tool_info_alpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = tool.description,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AerospacePrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    label = "Serial Number",
                    value = tool.serialNumber
                )
                InfoItem(
                    label = "Category",
                    value = tool.category
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    label = "Location",
                    value = tool.location
                )
                InfoItem(
                    label = "Condition",
                    value = tool.condition
                )
            }
        }
    }
}

@Composable
fun StatusAndActionsCard(
    tool: ToolDetail,
    onCheckoutClick: () -> Unit
) {
    BouncyCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Status",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = tool.statusColor,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tool.status,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = tool.statusColor
                        )
                    }
                }
                
                if (tool.status == "Available") {
                    GradientButton(
                        onClick = onCheckoutClick,
                        text = "Checkout",
                        modifier = Modifier.height(48.dp)
                    )
                }
            }
            
            if (tool.status == "Checked Out") {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AerospaceWarning.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Checked out to: ${tool.checkedOutTo}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Due: ${tool.dueDate}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpecificationsCard(specifications: Map<String, String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Specifications",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            specifications.forEach { (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = key,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (specifications.keys.last() != key) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun HistoryCard(history: List<HistoryItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Recent History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            history.forEach { item ->
                HistoryItemRow(item = item)
                if (history.last() != item) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun HistoryItemRow(item: HistoryItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = item.color,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.action,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.user,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Text(
            text = item.date,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CheckoutDialog(
    tool: ToolDetail,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Checkout Tool")
        },
        text = {
            Column {
                Text("Are you sure you want to checkout:")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${tool.toolNumber} - ${tool.description}",
                    fontWeight = FontWeight.Bold,
                    color = AerospacePrimary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AerospacePrimary
                )
            ) {
                Text("Checkout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Data classes
data class ToolDetail(
    val id: String,
    val toolNumber: String,
    val serialNumber: String,
    val description: String,
    val category: String,
    val location: String,
    val condition: String,
    val status: String,
    val statusColor: Color,
    val checkedOutTo: String? = null,
    val dueDate: String? = null,
    val images: List<String> = listOf("image1", "image2", "image3"),
    val specifications: Map<String, String> = emptyMap(),
    val history: List<HistoryItem> = emptyList()
)

data class HistoryItem(
    val action: String,
    val user: String,
    val date: String,
    val icon: ImageVector,
    val color: Color
)

fun getSampleToolDetail(toolId: String) = ToolDetail(
    id = toolId,
    toolNumber = "HT001",
    serialNumber = "SN123456",
    description = "Torque Wrench 1/2\"",
    category = "General",
    location = "Tool Crib A",
    condition = "Excellent",
    status = "Available",
    statusColor = ToolAvailable,
    specifications = mapOf(
        "Drive Size" to "1/2 inch",
        "Torque Range" to "10-150 ft-lbs",
        "Accuracy" to "±4%",
        "Length" to "18 inches",
        "Weight" to "3.2 lbs"
    ),
    history = listOf(
        HistoryItem(
            "Returned",
            "John Doe",
            "2 days ago",
            Icons.Default.CheckCircle,
            AerospaceSuccess
        ),
        HistoryItem(
            "Checked Out",
            "John Doe",
            "1 week ago",
            Icons.Default.Assignment,
            AerospaceWarning
        ),
        HistoryItem(
            "Calibrated",
            "Maintenance Team",
            "2 weeks ago",
            Icons.Default.Build,
            AerospaceInfo
        )
    )
)
