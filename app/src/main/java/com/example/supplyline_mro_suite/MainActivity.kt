package com.example.supplyline_mro_suite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.supplyline_mro_suite.presentation.navigation.Screen
import com.example.supplyline_mro_suite.presentation.screens.auth.SimpleLoginScreen
import com.example.supplyline_mro_suite.presentation.screens.dashboard.DashboardScreen
import com.example.supplyline_mro_suite.presentation.screens.tools.ToolsScreen
import com.example.supplyline_mro_suite.presentation.screens.tools.ToolDetailScreen
import com.example.supplyline_mro_suite.presentation.screens.tools.ToolCheckoutScreen
import com.example.supplyline_mro_suite.presentation.screens.chemicals.ChemicalsScreen
import com.example.supplyline_mro_suite.presentation.screens.profile.ProfileScreen
import com.example.supplyline_mro_suite.presentation.screens.scanner.ScannerScreen
import com.example.supplyline_mro_suite.presentation.screens.splash.SplashScreen
import com.example.supplyline_mro_suite.data.repository.SampleDataInitializer
import javax.inject.Inject
import com.example.supplyline_mro_suite.ui.theme.SupplyLineMROSuiteTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sampleDataInitializer: SampleDataInitializer
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize sample data
        sampleDataInitializer.initializeSampleData()

        setContent {
            SupplyLineMROSuiteTheme {
                val systemUiController = rememberSystemUiController()
                val navController = rememberNavController()

                LaunchedEffect(systemUiController) {
                    systemUiController.setSystemBarsColor(
                        color = androidx.compose.ui.graphics.Color.Transparent,
                        darkIcons = false
                    )
                }

                SupplyLineNavigation(navController = navController)
            }
        }
    }
}

@Composable
fun SupplyLineNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { 1000 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -1000 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -1000 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { 1000 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.Login.route) {
            SimpleLoginScreen(navController = navController)
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        composable(Screen.Tools.route) {
            ToolsScreen(navController = navController)
        }

        composable(Screen.Chemicals.route) {
            ChemicalsScreen(navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(Screen.Scanner.route) {
            ScannerScreen(navController = navController)
        }

        composable(
            route = Screen.ToolDetail.route,
            arguments = listOf(navArgument("toolId") { type = NavType.IntType })
        ) { backStackEntry ->
            val toolId = backStackEntry.arguments?.getInt("toolId") ?: 0
            ToolDetailScreen(navController = navController, toolId = toolId.toString())
        }

        composable(
            route = Screen.CheckoutTool.route,
            arguments = listOf(navArgument("toolId") { type = NavType.IntType })
        ) { backStackEntry ->
            val toolId = backStackEntry.arguments?.getInt("toolId") ?: 0
            ToolCheckoutScreen(navController = navController, toolId = toolId)
        }
    }
}