package com.example.supplyline_mro_suite.data.remote

import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NetworkErrorHandlerTest {

    private lateinit var networkErrorHandler: NetworkErrorHandler

    @Before
    fun setup() {
        networkErrorHandler = NetworkErrorHandler()
    }

    @Test
    fun `handleException with HttpException 401 returns AuthenticationError`() {
        // Given
        val httpException = HttpException(
            Response.error<Any>(401, "Unauthorized".toResponseBody())
        )

        // When
        val result = networkErrorHandler.handleException(httpException)

        // Then
        assertTrue(result is NetworkException.AuthenticationError)
        assertEquals("Authentication failed", result.message)
    }

    @Test
    fun `handleException with HttpException 403 returns AuthorizationError`() {
        // Given
        val httpException = HttpException(
            Response.error<Any>(403, "Forbidden".toResponseBody())
        )

        // When
        val result = networkErrorHandler.handleException(httpException)

        // Then
        assertTrue(result is NetworkException.AuthorizationError)
        assertEquals("Access denied", result.message)
    }

    @Test
    fun `handleException with HttpException 500 returns ServerError`() {
        // Given
        val httpException = HttpException(
            Response.error<Any>(500, "Internal Server Error".toResponseBody())
        )

        // When
        val result = networkErrorHandler.handleException(httpException)

        // Then
        assertTrue(result is NetworkException.ServerError)
        assertEquals("Server error occurred", result.message)
    }

    @Test
    fun `handleException with SocketTimeoutException returns TimeoutError`() {
        // Given
        val timeoutException = SocketTimeoutException("Connection timed out")

        // When
        val result = networkErrorHandler.handleException(timeoutException)

        // Then
        assertTrue(result is NetworkException.TimeoutError)
    }

    @Test
    fun `handleException with UnknownHostException returns NoInternetError`() {
        // Given
        val unknownHostException = UnknownHostException("Unable to resolve host")

        // When
        val result = networkErrorHandler.handleException(unknownHostException)

        // Then
        assertTrue(result is NetworkException.NoInternetError)
    }

    @Test
    fun `handleException with IOException returns NetworkError`() {
        // Given
        val ioException = IOException("Network connection failed")

        // When
        val result = networkErrorHandler.handleException(ioException)

        // Then
        assertTrue(result is NetworkException.NetworkError)
        assertEquals("Network connection error", result.message)
    }

    @Test
    fun `handleException with unknown exception returns UnknownError`() {
        // Given
        val unknownException = RuntimeException("Something went wrong")

        // When
        val result = networkErrorHandler.handleException(unknownException)

        // Then
        assertTrue(result is NetworkException.UnknownError)
        assertEquals("Something went wrong", result.message)
    }

    @Test
    fun `handleResponse with successful response returns Success`() {
        // Given
        val successfulResponse = Response.success("test data")

        // When
        val result = networkErrorHandler.handleResponse(successfulResponse)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals("test data", result.data)
    }

    @Test
    fun `handleResponse with error response returns Error`() {
        // Given
        val errorResponse = Response.error<String>(404, "Not Found".toResponseBody())

        // When
        val result = networkErrorHandler.handleResponse(errorResponse)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertTrue(result.exception is NetworkException.HttpError)
        val httpError = result.exception as NetworkException.HttpError
        assertEquals(404, httpError.code)
    }

    @Test
    fun `safeApiCall with successful API call returns Success`() = runTest {
        // Given
        val apiCall: suspend () -> Response<String> = {
            Response.success("success data")
        }

        // When
        val result = networkErrorHandler.safeApiCall(apiCall)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals("success data", result.data)
    }

    @Test
    fun `safeApiCall with exception returns Error`() = runTest {
        // Given
        val apiCall: suspend () -> Response<String> = {
            throw SocketTimeoutException("Timeout")
        }

        // When
        val result = networkErrorHandler.safeApiCall(apiCall)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertTrue(result.exception is NetworkException.TimeoutError)
    }

    @Test
    fun `safeApiCallDirect with successful call returns Success`() = runTest {
        // Given
        val apiCall: suspend () -> String = {
            "direct success"
        }

        // When
        val result = networkErrorHandler.safeApiCallDirect(apiCall)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals("direct success", result.data)
    }

    @Test
    fun `safeApiCallDirect with exception returns Error`() = runTest {
        // Given
        val apiCall: suspend () -> String = {
            throw IOException("Network error")
        }

        // When
        val result = networkErrorHandler.safeApiCallDirect(apiCall)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertTrue(result.exception is NetworkException.NetworkError)
    }
}
