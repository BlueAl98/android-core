package com.nayibit.errorManager

class CallConfig<T> internal constructor() {
    internal val handlers = mutableMapOf<Int, () -> Result<T>>()

    fun on(code: Int, result: () -> T) {
        handlers[code] = { Result.success(result()) }
    }

    fun onError(code: Int, error: () -> Throwable) {
        handlers[code] = { Result.failure(error()) }
    }
}
