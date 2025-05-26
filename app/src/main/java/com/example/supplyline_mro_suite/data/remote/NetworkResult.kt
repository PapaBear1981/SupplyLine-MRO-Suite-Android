package com.example.supplyline_mro_suite.data.remote

/**
 * A generic wrapper class for network responses that provides
 * comprehensive error handling and success states.
 */
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val exception: NetworkException) : NetworkResult<T>()
    data class Loading<T>(val isLoading: Boolean = true) : NetworkResult<T>()
}

/**
 * Custom exception class for network-related errors
 */
sealed class NetworkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    data class NetworkError(
        val errorMessage: String,
        val errorCause: Throwable? = null
    ) : NetworkException(errorMessage, errorCause)
    
    data class HttpError(
        val code: Int,
        val errorMessage: String,
        val errorBody: String? = null
    ) : NetworkException("HTTP $code: $errorMessage")
    
    data class AuthenticationError(
        val errorMessage: String = "Authentication failed"
    ) : NetworkException(errorMessage)
    
    data class AuthorizationError(
        val errorMessage: String = "Access denied"
    ) : NetworkException(errorMessage)
    
    data class ServerError(
        val errorMessage: String = "Server error occurred"
    ) : NetworkException(errorMessage)
    
    data class TimeoutError(
        val errorMessage: String = "Request timed out"
    ) : NetworkException(errorMessage)
    
    data class NoInternetError(
        val errorMessage: String = "No internet connection"
    ) : NetworkException(errorMessage)
    
    data class UnknownError(
        val errorMessage: String = "Unknown error occurred",
        val errorCause: Throwable? = null
    ) : NetworkException(errorMessage, errorCause)
}

/**
 * Extension function to get user-friendly error messages
 */
fun NetworkException.getUserFriendlyMessage(): String {
    return when (this) {
        is NetworkException.NetworkError -> "Network connection problem. Please check your internet connection."
        is NetworkException.HttpError -> when (code) {
            400 -> "Invalid request. Please check your input."
            401 -> "Please log in again to continue."
            403 -> "You don't have permission to perform this action."
            404 -> "The requested resource was not found."
            408 -> "Request timed out. Please try again."
            429 -> "Too many requests. Please wait a moment and try again."
            in 500..599 -> "Server error. Please try again later."
            else -> "Request failed with error code $code."
        }
        is NetworkException.AuthenticationError -> "Authentication failed. Please log in again."
        is NetworkException.AuthorizationError -> "You don't have permission to access this resource."
        is NetworkException.ServerError -> "Server error. Please try again later."
        is NetworkException.TimeoutError -> "Request timed out. Please check your connection and try again."
        is NetworkException.NoInternetError -> "No internet connection. Please check your network settings."
        is NetworkException.UnknownError -> "An unexpected error occurred. Please try again."
    }
}

/**
 * Extension function to convert NetworkResult to Result
 */
fun <T> NetworkResult<T>.toResult(): Result<T> {
    return when (this) {
        is NetworkResult.Success -> Result.success(data)
        is NetworkResult.Error -> Result.failure(exception)
        is NetworkResult.Loading -> Result.failure(IllegalStateException("Result is still loading"))
    }
}

/**
 * Extension function to convert Result to NetworkResult
 */
fun <T> Result<T>.toNetworkResult(): NetworkResult<T> {
    return fold(
        onSuccess = { NetworkResult.Success(it) },
        onFailure = { 
            val networkException = when (it) {
                is NetworkException -> it
                else -> NetworkException.UnknownError(
                    it.message ?: "Unknown error occurred",
                    it
                )
            }
            NetworkResult.Error(networkException)
        }
    )
}
