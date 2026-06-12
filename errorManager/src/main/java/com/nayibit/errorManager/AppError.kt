package com.nayibit.errorManager

sealed class AppError(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    class Network(cause: Throwable) : AppError(cause.message, cause)
    class Http(val code: Int, val rawBody: String?) : AppError("HTTP $code")
    class Unknown(cause: Throwable) : AppError(cause.message, cause)
}
