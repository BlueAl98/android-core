package com.nayibit.errorManager

import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> safeCall(
    config: (CallConfig<T>.() -> Unit)? = null,
    block: suspend () -> T
): Result<T> {
    val callConfig = config?.let { CallConfig<T>().apply(it) }
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Result.failure(e.toNetworkError())
    } catch (e: Throwable) {
        val httpInfo = ErrorManager.httpAdapter?.extract(e)
        if (httpInfo != null) {
            callConfig?.handlers?.get(httpInfo.code)?.invoke()
                ?: ErrorManager.errorParser
                    ?.let { Result.failure(it.parse(httpInfo.code, httpInfo.rawBody)) }
                ?: Result.failure(AppError.Http(httpInfo.code, httpInfo.rawBody))
        } else {
            Result.failure(AppError.Unknown(e))
        }
    }
}

private fun IOException.toNetworkError(): AppError.Network = when {
    this is SSLException -> AppError.Network.SslError(this)
    this is SocketTimeoutException -> AppError.Network.Timeout(this)
    this is SocketException || this is UnknownHostException -> AppError.Network.NoConnection(this)
    else -> AppError.Network.Generic(this)
}
