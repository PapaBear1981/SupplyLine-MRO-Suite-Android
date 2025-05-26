package com.example.supplyline_mro_suite.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor that implements retry logic with exponential backoff
 * for failed network requests
 */
@Singleton
class RetryInterceptor @Inject constructor() : Interceptor {

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val INITIAL_BACKOFF_MS = 1000L
        private const val BACKOFF_MULTIPLIER = 2.0
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null

        for (attempt in 0 until MAX_RETRY_ATTEMPTS) {
            try {
                response?.close() // Close previous response if exists
                response = chain.proceed(request)

                // If successful or client error (4xx), don't retry
                if (response.isSuccessful || response.code in 400..499) {
                    return response
                }

                // For server errors (5xx), retry
                if (response.code in 500..599 && attempt < MAX_RETRY_ATTEMPTS - 1) {
                    response.close()
                    waitBeforeRetry(attempt)
                    continue
                }

                return response

            } catch (e: IOException) {
                exception = e
                
                // Only retry for specific network exceptions
                if (shouldRetry(e) && attempt < MAX_RETRY_ATTEMPTS - 1) {
                    waitBeforeRetry(attempt)
                    continue
                }
                
                // If we've exhausted retries or shouldn't retry, throw the exception
                throw e
            }
        }

        // This should never be reached, but just in case
        return response ?: throw (exception ?: IOException("Unknown error"))
    }

    /**
     * Determines if the exception is retryable
     */
    private fun shouldRetry(exception: IOException): Boolean {
        return when (exception) {
            is SocketTimeoutException -> true
            is UnknownHostException -> true
            else -> {
                // Check if it's a connection-related IOException
                val message = exception.message?.lowercase() ?: ""
                message.contains("timeout") ||
                message.contains("connection") ||
                message.contains("network") ||
                message.contains("socket")
            }
        }
    }

    /**
     * Implements exponential backoff delay
     */
    private fun waitBeforeRetry(attempt: Int) {
        try {
            val delay = (INITIAL_BACKOFF_MS * Math.pow(BACKOFF_MULTIPLIER, attempt.toDouble())).toLong()
            Thread.sleep(delay)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
