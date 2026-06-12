package com.nayibit.errorManager

object ErrorManager {
    var httpAdapter: HttpExceptionAdapter? = null
    var errorParser: BackendErrorParser? = null

    fun configure(
        httpAdapter: HttpExceptionAdapter,
        errorParser: BackendErrorParser? = null
    ) {
        this.httpAdapter = httpAdapter
        this.errorParser = errorParser
    }
}

fun interface HttpExceptionAdapter {
    fun extract(throwable: Throwable): HttpErrorInfo?
}

fun interface BackendErrorParser {
    fun parse(httpCode: Int, rawBody: String?): Throwable
}
