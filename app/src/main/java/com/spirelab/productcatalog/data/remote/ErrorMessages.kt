package com.spirelab.productcatalog.data.remote

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Turns a network failure into a message that is safe to show to users. */
object ErrorMessages {
    const val NO_INTERNET = "No internet connection"
    const val TIMEOUT = "Request timed out"
    const val NOT_FOUND = "Product not found"
    const val SERVER_ERROR = "The server is unavailable. Please try again later."

    fun from(error: Throwable, fallback: String): String = when (error) {
        is SocketTimeoutException -> TIMEOUT
        is UnknownHostException, is ConnectException -> NO_INTERNET
        is HttpException -> when (error.code()) {
            404 -> NOT_FOUND
            in 500..599 -> SERVER_ERROR
            else -> fallback
        }
        is IOException -> NO_INTERNET
        else -> fallback
    }
}
