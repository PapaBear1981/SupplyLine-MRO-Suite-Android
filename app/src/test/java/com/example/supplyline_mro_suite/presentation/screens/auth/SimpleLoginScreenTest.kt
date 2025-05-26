package com.example.supplyline_mro_suite.presentation.screens.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.supplyline_mro_suite.ui.theme.SupplyLineMROSuiteTheme
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleLoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockNavController = mockk<NavController>(relaxed = true)

    @Test
    fun loginScreen_displaysCorrectElements() {
        composeTestRule.setContent {
            SupplyLineMROSuiteTheme {
                SimpleLoginScreen(navController = mockNavController)
            }
        }

        // Verify UI elements are displayed
        composeTestRule.onNodeWithText("SupplyLine").assertIsDisplayed()
        composeTestRule.onNodeWithText("MRO Suite").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign in to access your aerospace tools").assertIsDisplayed()
        composeTestRule.onNodeWithText("Employee Number").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsErrorForEmptyEmployeeNumber() {
        composeTestRule.setContent {
            SupplyLineMROSuiteTheme {
                SimpleLoginScreen(navController = mockNavController)
            }
        }

        // Click login button without entering employee number
        composeTestRule.onNodeWithText("Sign In").performClick()

        // Verify error message is shown
        composeTestRule.onNodeWithText("Employee number is required").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsErrorForEmptyPassword() {
        composeTestRule.setContent {
            SupplyLineMROSuiteTheme {
                SimpleLoginScreen(navController = mockNavController)
            }
        }

        // Enter employee number but not password
        composeTestRule.onNodeWithText("Employee Number").performTextInput("ADMIN001")
        composeTestRule.onNodeWithText("Sign In").performClick()

        // Verify error message is shown
        composeTestRule.onNodeWithText("Password is required").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsErrorForShortPassword() {
        composeTestRule.setContent {
            SupplyLineMROSuiteTheme {
                SimpleLoginScreen(navController = mockNavController)
            }
        }

        // Enter employee number and short password
        composeTestRule.onNodeWithText("Employee Number").performTextInput("ADMIN001")
        composeTestRule.onNodeWithText("Password").performTextInput("123")
        composeTestRule.onNodeWithText("Sign In").performClick()

        // Verify error message is shown
        composeTestRule.onNodeWithText("Password must be at least 6 characters").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsLoadingStateWhenLoggingIn() {
        composeTestRule.setContent {
            SupplyLineMROSuiteTheme {
                SimpleLoginScreen(navController = mockNavController)
            }
        }

        // Enter valid credentials
        composeTestRule.onNodeWithText("Employee Number").performTextInput("ADMIN001")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Sign In").performClick()

        // Verify loading state is shown
        composeTestRule.onNode(hasContentDescription("Loading")).assertIsDisplayed()
    }

    @Test
    fun loginScreen_passwordVisibilityToggleWorks() {
        composeTestRule.setContent {
            SupplyLineMROSuiteTheme {
                SimpleLoginScreen(navController = mockNavController)
            }
        }

        // Enter password
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // Click visibility toggle
        composeTestRule.onNodeWithContentDescription("Show password").performClick()

        // Verify password is now visible (toggle should now show "Hide password")
        composeTestRule.onNodeWithContentDescription("Hide password").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsErrorForInvalidCredentials() {
        composeTestRule.setContent {
            SupplyLineMROSuiteTheme {
                SimpleLoginScreen(navController = mockNavController)
            }
        }

        // Enter invalid credentials
        composeTestRule.onNodeWithText("Employee Number").performTextInput("INVALID")
        composeTestRule.onNodeWithText("Password").performTextInput("wrongpassword")
        composeTestRule.onNodeWithText("Sign In").performClick()

        // Wait for authentication to complete and verify error message
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Invalid credentials. Try ADMIN001 / password123")
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithText("Invalid credentials. Try ADMIN001 / password123")
            .assertIsDisplayed()
    }
}
