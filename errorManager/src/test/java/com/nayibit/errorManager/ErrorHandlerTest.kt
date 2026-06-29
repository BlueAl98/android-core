package com.nayibit.errorManager

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLPeerUnverifiedException

class ErrorHandlerTest {

    @Before
    fun setUp() {
        ErrorManager.configure(httpAdapter = HttpExceptionAdapter { null })
    }

    // ── Success ──────────────────────────────────────────────────────────────

    @Test
    fun `success returns the value`() = runTest {
        val result = safeCall { 42 }
        assertEquals(42, result.getOrNull())
    }

    // ── SSL ──────────────────────────────────────────────────────────────────

    @Test
    fun `SSLPeerUnverifiedException maps to SslError`() = runTest {
        val result = safeCall<Unit> { throw SSLPeerUnverifiedException("cert not verified") }
        assertTrue(result.exceptionOrNull() is AppError.Network.SslError)
    }

    @Test
    fun `SSLException maps to SslError`() = runTest {
        val result = safeCall<Unit> { throw SSLException("handshake failed") }
        assertTrue(result.exceptionOrNull() is AppError.Network.SslError)
    }

    @Test
    fun `SslError preserves original cause`() = runTest {
        val original = SSLPeerUnverifiedException("bad cert")
        val result = safeCall<Unit> { throw original }
        val error = result.exceptionOrNull() as AppError.Network.SslError
        assertEquals(original, error.cause)
    }

    // ── Timeout ──────────────────────────────────────────────────────────────

    @Test
    fun `SocketTimeoutException maps to Timeout`() = runTest {
        val result = safeCall<Unit> { throw SocketTimeoutException("read timed out") }
        assertTrue(result.exceptionOrNull() is AppError.Network.Timeout)
    }

    @Test
    fun `Timeout preserves original cause`() = runTest {
        val original = SocketTimeoutException("connect timed out")
        val result = safeCall<Unit> { throw original }
        val error = result.exceptionOrNull() as AppError.Network.Timeout
        assertEquals(original, error.cause)
    }

    // ── No connection ─────────────────────────────────────────────────────────

    @Test
    fun `SocketException maps to NoConnection`() = runTest {
        val result = safeCall<Unit> { throw SocketException("connection refused") }
        assertTrue(result.exceptionOrNull() is AppError.Network.NoConnection)
    }

    @Test
    fun `UnknownHostException maps to NoConnection`() = runTest {
        val result = safeCall<Unit> { throw UnknownHostException("no such host") }
        assertTrue(result.exceptionOrNull() is AppError.Network.NoConnection)
    }

    @Test
    fun `NoConnection preserves original cause`() = runTest {
        val original = UnknownHostException("api.example.com")
        val result = safeCall<Unit> { throw original }
        val error = result.exceptionOrNull() as AppError.Network.NoConnection
        assertEquals(original, error.cause)
    }

    // ── Generic IO (covers malformed JSON path in library layer) ─────────────

    @Test
    fun `plain IOException maps to Generic`() = runTest {
        val result = safeCall<Unit> { throw IOException("unexpected end of stream") }
        assertTrue(result.exceptionOrNull() is AppError.Network.Generic)
    }

    @Test
    fun `IOException wrapping another exception maps to Generic`() = runTest {
        val result = safeCall<Unit> {
            throw IOException("wrapped", RuntimeException("inner"))
        }
        assertTrue(result.exceptionOrNull() is AppError.Network.Generic)
    }

    @Test
    fun `Generic preserves the IOException as cause`() = runTest {
        val original = IOException("bad stream")
        val result = safeCall<Unit> { throw original }
        val error = result.exceptionOrNull() as AppError.Network.Generic
        assertEquals(original, error.cause)
    }

    // ── Unknown (non-IO, non-HTTP) ────────────────────────────────────────────

    @Test
    fun `RuntimeException maps to Unknown`() = runTest {
        val result = safeCall<Unit> { throw RuntimeException("unexpected") }
        assertTrue(result.exceptionOrNull() is AppError.Unknown)
    }

    @Test
    fun `Unknown preserves original cause`() = runTest {
        val original = RuntimeException("unexpected")
        val result = safeCall<Unit> { throw original }
        val error = result.exceptionOrNull() as AppError.Unknown
        assertEquals(original, error.cause)
    }

    // ── HTTP via adapter ──────────────────────────────────────────────────────

    @Test
    fun `HTTP exception is parsed into AppError_Http when no errorParser`() = runTest {
        ErrorManager.configure(
            httpAdapter = HttpExceptionAdapter { HttpErrorInfo(code = 401, rawBody = null) },
            errorParser = null
        )
        val result = safeCall<Unit> { throw RuntimeException("http 401") }
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.Http)
        assertEquals(401, (error as AppError.Http).code)
    }

    @Test
    fun `HTTP exception is delegated to errorParser when configured`() = runTest {
        val parsed = Exception("Unauthorized")
        ErrorManager.configure(
            httpAdapter = HttpExceptionAdapter { HttpErrorInfo(code = 401, rawBody = """{"error":"Unauthorized"}""") },
            errorParser = BackendErrorParser { _, _ -> parsed }
        )
        val result = safeCall<Unit> { throw RuntimeException("http 401") }
        assertEquals(parsed, result.exceptionOrNull())
    }

    @Test
    fun `specific HTTP code handler returns custom success`() = runTest {
        ErrorManager.configure(
            httpAdapter = HttpExceptionAdapter { HttpErrorInfo(code = 304, rawBody = null) }
        )
        val result = safeCall<String>({
            on(304) { "not modified" }
        }) {
            throw RuntimeException("304")
        }
        assertEquals("not modified", result.getOrNull())
    }

    // ── CancellationException must propagate ──────────────────────────────────

    @Test
    fun `CancellationException is rethrown not wrapped`() = runTest {
        var caughtCancel = false
        try {
            safeCall<Unit> { throw kotlin.coroutines.cancellation.CancellationException("cancelled") }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            caughtCancel = true
        }
        assertTrue(caughtCancel)
    }
}
