package com.example.supplyline_mro_suite.presentation.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.supplyline_mro_suite.presentation.components.BottomNavigationBar
import com.example.supplyline_mro_suite.presentation.navigation.Screen
import com.example.supplyline_mro_suite.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(true) }
    
    // Simulate loading
    LaunchedEffect(Unit) {
        delay(600)
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* Open settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
            ProfileContent(
                modifier = Modifier.padding(paddingValues),
                navController = navController
            )
        }
    }
}

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile header
        item {
            AnimatedProfileHeader()
        }
        
        // Quick stats
        item {
            Text(
                text = "My Activity",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Tools Checked Out",
                    value = "3",
                    icon = Icons.Default.Build,
                    color = AerospacePrimary,
                    modifier = Modifier.weight(1f)
                )
                
                StatCard(
                    title = "Chemicals Used",
                    value = "12",
                    icon = Icons.Default.Science,
                    color = AerospaceAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Menu items
        item {
            Text(
                text = "Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                getMenuItems().forEach { menuItem ->
                    AnimatedMenuItem(
                        menuItem = menuItem,
                        onClick = {
                            when (menuItem.title) {
                                "Logout" -> {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                                else -> {
                                    // Handle other menu items
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedProfileHeader() {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "profile_header_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "profile_header_alpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = AerospacePrimary.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AerospacePrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CB",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Chris Bear",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AerospacePrimary
            )
            
            Text(
                text = "Employee #: ADMIN001",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Text(
                text = "Materials Department",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AnimatedMenuItem(
    menuItem: MenuItem,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(menuItem) {
        delay(100)
        visible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "menu_item_alpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = menuItem.icon,
                contentDescription = null,
                tint = menuItem.color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = menuItem.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

// Data classes and sample data
data class MenuItem(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

fun getMenuItems() = listOf(
    MenuItem("Edit Profile", Icons.Default.Edit, AerospacePrimary),
    MenuItem("Notifications", Icons.Default.Notifications, AerospaceAccent),
    MenuItem("Dark Mode", Icons.Default.DarkMode, AerospaceSecondary),
    MenuItem("Help & Support", Icons.Default.Help, AerospaceInfo),
    MenuItem("About", Icons.Default.Info, AerospaceSecondary),
    MenuItem("Logout", Icons.Default.Logout, AerospaceError)
)
