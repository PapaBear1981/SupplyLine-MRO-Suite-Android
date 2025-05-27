package com.example.supplyline_mro_suite

import com.example.supplyline_mro_suite.data.auth.AuthenticationValidator
import com.example.supplyline_mro_suite.data.auth.ValidationResult
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for authentication validation logic
 */
class AuthenticationValidationTest {

    private lateinit var validator: AuthenticationValidator

    @Before
    fun setup() {
        validator = AuthenticationValidator()
    }

    @Test
    fun validateEmployeeNumber_emptyString_returnsError() {
        val result = validator.validateEmployeeNumber("")
        assertTrue("Empty employee number should be invalid", result is ValidationResult.Error)
    }

    @Test
    fun validateEmployeeNumber_blankString_returnsError() {
        val result = validator.validateEmployeeNumber("   ")
        assertTrue("Blank employee number should be invalid", result is ValidationResult.Error)
    }

    @Test
    fun validateEmployeeNumber_validString_returnsSuccess() {
        val result = validator.validateEmployeeNumber("ADMIN001")
        assertTrue("Valid employee number should be valid", result is ValidationResult.Success)
    }

    @Test
    fun validateEmployeeNumber_sqlInjectionAttempt_returnsError() {
        val result = validator.validateEmployeeNumber("'; DROP TABLE users; --")
        assertTrue("SQL injection attempt should be invalid", result is ValidationResult.Error)
    }

    @Test
    fun validatePassword_emptyString_returnsError() {
        val result = validator.validatePassword("")
        assertTrue("Empty password should be invalid", result is ValidationResult.Error)
    }

    @Test
    fun validatePassword_shortPassword_returnsError() {
        val result = validator.validatePassword("123")
        assertTrue("Password shorter than 8 characters should be invalid", result is ValidationResult.Error)
    }

    @Test
    fun validatePassword_noUppercase_returnsError() {
        val result = validator.validatePassword("password123")
        assertTrue("Password without uppercase should be invalid", result is ValidationResult.Error)
    }

    @Test
    fun validatePassword_noLowercase_returnsError() {
        val result = validator.validatePassword("PASSWORD123")
        assertTrue("Password without lowercase should be invalid", result is ValidationResult.Error)
    }

    @Test
    fun validatePassword_noDigits_returnsError() {
        val result = validator.validatePassword("Password")
        assertTrue("Password without digits should be invalid", result is ValidationResult.Error)
    }

    @Test
    fun validatePassword_validPassword_returnsSuccess() {
        val result = validator.validatePassword("Password123!")
        assertTrue("Valid password should be valid", result is ValidationResult.Success)
    }

    @Test
    fun validatePassword_extremelyLongPassword_handledGracefully() {
        val longPassword = "A".repeat(200) + "a1"
        val result = validator.validatePassword(longPassword)
        assertTrue("Very long password should be handled gracefully", result is ValidationResult.Error)
    }

    @Test
    fun validateCredentials_validCredentials_returnsSuccess() {
        val result = validator.validateCredentials("ADMIN001", "Password123!")
        assertTrue("Valid credentials should be valid", result is ValidationResult.Success)
    }

    @Test
    fun validateCredentials_invalidEmployeeNumber_returnsError() {
        val result = validator.validateCredentials("", "Password123!")
        assertTrue("Invalid employee number should fail validation", result is ValidationResult.Error)
    }

    @Test
    fun validateCredentials_invalidPassword_returnsError() {
        val result = validator.validateCredentials("ADMIN001", "weak")
        assertTrue("Invalid password should fail validation", result is ValidationResult.Error)
    }

    @Test
    fun validateCredentials_emptyCredentials_returnsError() {
        val result = validator.validateCredentials("", "")
        assertTrue("Empty credentials should fail validation", result is ValidationResult.Error)
    }
}
