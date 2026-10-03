package com.spirelab.productcatalog.data.remote

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMessagesTest {

    private val fallback = "Unable to load products"

    private fun httpError(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody()))

    @Test
    fun `maps network failures to user-facing messages`() {
        assertEquals(ErrorMessages.NO_INTERNET, ErrorMessages.from(UnknownHostException("dummyjson.com"), fallback))
        assertEquals(ErrorMessages.NO_INTERNET, ErrorMessages.from(IOException("reset"), fallback))
        assertEquals(ErrorMessages.TIMEOUT, ErrorMessages.from(SocketTimeoutException("timeout"), fallback))
    }

    @Test
    fun `maps http failures`() {
        assertEquals(ErrorMessages.NOT_FOUND, ErrorMessages.from(httpError(404), fallback))
        assertEquals(ErrorMessages.SERVER_ERROR, ErrorMessages.from(httpError(503), fallback))
        assertEquals(fallback, ErrorMessages.from(httpError(400), fallback))
    }

    @Test
    fun `unknown errors use the fallback instead of the raw exception message`() {
        assertEquals(fallback, ErrorMessages.from(IllegalStateException("Expected BEGIN_OBJECT"), fallback))
    }
}
