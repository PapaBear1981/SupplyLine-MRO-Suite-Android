package com.example.supplyline_mro_suite.data.auth

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles authentication validation logic
 */
@Singleton
class AuthenticationValidator @Inject constructor() {

    /**
     * Validates employee number format and content
     */
    fun validateEmployeeNumber(employeeNumber: String): ValidationResult {
        return when {
            employeeNumber.isBlank() -> ValidationResult.Error("Employee number is required")
            employeeNumber.length < 3 -> ValidationResult.Error("Employee number must be at least 3 characters")
            containsSqlInjectionPatterns(employeeNumber) -> ValidationResult.Error("Invalid characters in employee number")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates password strength and security requirements
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Password is required")
            password.length < 8 -> ValidationResult.Error("Password must be at least 8 characters")
            !hasUppercase(password) -> ValidationResult.Error("Password must contain uppercase letters")
            !hasLowercase(password) -> ValidationResult.Error("Password must contain lowercase letters")
            !hasDigit(password) -> ValidationResult.Error("Password must contain numbers")
            password.length > 128 -> ValidationResult.Error("Password is too long")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates both credentials together
     */
    fun validateCredentials(employeeNumber: String, password: String): ValidationResult {
        val employeeValidation = validateEmployeeNumber(employeeNumber)
        if (employeeValidation is ValidationResult.Error) {
            return employeeValidation
        }

        val passwordValidation = validatePassword(password)
        if (passwordValidation is ValidationResult.Error) {
            return passwordValidation
        }

        return ValidationResult.Success
    }

    private fun hasUppercase(password: String): Boolean = password.any { it.isUpperCase() }
    private fun hasLowercase(password: String): Boolean = password.any { it.isLowerCase() }
    private fun hasDigit(password: String): Boolean = password.any { it.isDigit() }

    private fun containsSqlInjectionPatterns(input: String): Boolean {
        val sqlPatterns = listOf(
            "drop table", "delete from", "insert into", "update set",
            "union select", "or 1=1", "' or '", "-- ", "/*", "*/"
        )
        val lowerInput = input.lowercase()
        return sqlPatterns.any { pattern -> lowerInput.contains(pattern) }
    }
}

/**
 * Represents the result of a validation operation
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
