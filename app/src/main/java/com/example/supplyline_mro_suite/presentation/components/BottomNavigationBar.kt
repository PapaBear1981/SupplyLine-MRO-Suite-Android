package com.example.supplyline_mro_suite.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.supplyline_mro_suite.presentation.navigation.Screen
import com.example.supplyline_mro_suite.presentation.navigation.bottomNavItems
import com.example.supplyline_mro_suite.ui.theme.*

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        bottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route

            NavigationBarItem(
                icon = {
                    AnimatedNavigationIcon(
                        screen = screen,
                        isSelected = isSelected
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AerospacePrimary,
                    selectedTextColor = AerospacePrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = AerospacePrimary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Composable
fun AnimatedNavigationIcon(
    screen: Screen,
    isSelected: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) AerospacePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        animationSpec = tween(300),
        label = "icon_color"
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = AerospacePrimary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )
        }

        screen.icon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = screen.title,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
