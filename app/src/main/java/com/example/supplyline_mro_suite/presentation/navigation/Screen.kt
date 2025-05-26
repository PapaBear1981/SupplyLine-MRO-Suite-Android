package com.example.supplyline_mro_suite.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    object Login : Screen("login", "Login")
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Tools : Screen("tools", "Tools", Icons.Default.Build)
    object Chemicals : Screen("chemicals", "Chemicals", Icons.Default.Science)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Scanner : Screen("scanner", "Scanner", Icons.Default.QrCodeScanner)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    
    // Detail screens
    object ToolDetail : Screen("tool_detail/{toolId}", "Tool Details") {
        fun createRoute(toolId: Int) = "tool_detail/$toolId"
    }
    
    object ChemicalDetail : Screen("chemical_detail/{chemicalId}", "Chemical Details") {
        fun createRoute(chemicalId: Int) = "chemical_detail/$chemicalId"
    }
    
    object CheckoutTool : Screen("checkout_tool/{toolId}", "Checkout Tool") {
        fun createRoute(toolId: Int) = "checkout_tool/$toolId"
    }
    
    object IssueChemical : Screen("issue_chemical/{chemicalId}", "Issue Chemical") {
        fun createRoute(chemicalId: Int) = "issue_chemical/$chemicalId"
    }
}

// Bottom navigation items
val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Tools,
    Screen.Chemicals,
    Screen.Profile
)
