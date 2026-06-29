package com.nayibit.errorManager

sealed class AppError(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {

    sealed class Network(message: String? = null, cause: Throwable? = null) : AppError(message, cause) {
        class NoConnection(cause: Throwable) : Network(cause.message, cause)
        class Timeout(cause: Throwable) : Network(cause.message, cause)
        class SslError(cause: Throwable) : Network(cause.message, cause)
        class Generic(cause: Throwable) : Network(cause.message, cause)
    }

    class Http(val code: Int, val rawBody: String?) : AppError("HTTP $code")
    class Unknown(cause: Throwable) : AppError(cause.message, cause)
}
