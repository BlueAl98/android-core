package com.nayibit.errorManager

import java.io.IOException
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
        Result.failure(AppError.Network(e))
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
