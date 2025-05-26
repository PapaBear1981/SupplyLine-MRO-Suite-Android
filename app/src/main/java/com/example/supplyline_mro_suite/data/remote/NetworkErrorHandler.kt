package com.example.supplyline_mro_suite.data.remote

import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for handling network errors and converting them to NetworkException
 */
@Singleton
class NetworkErrorHandler @Inject constructor() {

    /**
     * Handles exceptions and converts them to appropriate NetworkException
     */
    fun handleException(throwable: Throwable): NetworkException {
        return when (throwable) {
            is HttpException -> handleHttpException(throwable)
            is SocketTimeoutException -> NetworkException.TimeoutError()
            is UnknownHostException -> NetworkException.NoInternetError()
            is IOException -> NetworkException.NetworkError(
                "Network connection error",
                throwable
            )
            is NetworkException -> throwable
            else -> NetworkException.UnknownError(
                throwable.message ?: "Unknown error occurred",
                throwable
            )
        }
    }

    /**
     * Handles HTTP exceptions based on status codes
     */
    private fun handleHttpException(httpException: HttpException): NetworkException {
        val code = httpException.code()
        val message = httpException.message()
        val errorBody = try {
            httpException.response()?.errorBody()?.string()
        } catch (e: Exception) {
            null
        }

        return when (code) {
            401 -> NetworkException.AuthenticationError("Authentication failed")
            403 -> NetworkException.AuthorizationError("Access denied")
            in 400..499 -> NetworkException.HttpError(code, message, errorBody)
            in 500..599 -> NetworkException.ServerError("Server error occurred")
            else -> NetworkException.HttpError(code, message, errorBody)
        }
    }

    /**
     * Handles Retrofit Response and converts errors to NetworkException
     */
    fun <T> handleResponse(response: Response<T>): NetworkResult<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error(
                    NetworkException.UnknownError("Response body is null")
                )
            }
        } else {
            val errorBody = try {
                response.errorBody()?.string()
            } catch (e: Exception) {
                null
            }
            
            val networkException = when (response.code()) {
                401 -> NetworkException.AuthenticationError("Authentication failed")
                403 -> NetworkException.AuthorizationError("Access denied")
                in 400..499 -> NetworkException.HttpError(
                    response.code(),
                    response.message(),
                    errorBody
                )
                in 500..599 -> NetworkException.ServerError("Server error occurred")
                else -> NetworkException.HttpError(
                    response.code(),
                    response.message(),
                    errorBody
                )
            }
            
            NetworkResult.Error(networkException)
        }
    }

    /**
     * Safe API call wrapper that handles exceptions
     */
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        return try {
            val response = apiCall()
            handleResponse(response)
        } catch (throwable: Throwable) {
            NetworkResult.Error(handleException(throwable))
        }
    }

    /**
     * Safe API call wrapper for direct data (non-Response) calls
     */
    suspend fun <T> safeApiCallDirect(apiCall: suspend () -> T): NetworkResult<T> {
        return try {
            val result = apiCall()
            NetworkResult.Success(result)
        } catch (throwable: Throwable) {
            NetworkResult.Error(handleException(throwable))
        }
    }
}
