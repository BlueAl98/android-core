# errorManager

A lightweight Android library for handling HTTP and network errors in a unified, type-safe way. It decouples error detection from error handling and works with any HTTP client (Retrofit, Ktor, etc.).

---

## Table of Contents

- [How it works](#how-it-works)
- [Setup](#setup)
- [AppError types](#apperror-types)
- [safeCall](#safecall)
- [Handling errors](#handling-errors)
- [Scenarios](#scenarios)
  - [1. Minimal setup — no JSON parsing](#1-minimal-setup--no-json-parsing)
  - [2. Parsing the backend error body](#2-parsing-the-backend-error-body)
  - [3. Per-call code handlers](#3-per-call-code-handlers)
  - [4. Retrofit adapter](#4-retrofit-adapter)
  - [5. Ktor adapter](#5-ktor-adapter)
  - [6. Showing errors in the UI (ViewModel)](#6-showing-errors-in-the-ui-viewmodel)
  - [7. Exhaustive when — never miss an error type](#7-exhaustive-when--never-miss-an-error-type)
- [API reference](#api-reference)

---

## How it works

```
HTTP client throws exception
        │
        ▼
   safeCall catches it
        │
        ├─ IOException         → Result.failure(AppError.Network)
        │
        ├─ httpAdapter returns HttpErrorInfo?
        │       │
        │       ├─ null        → Result.failure(AppError.Unknown)
        │       │
        │       └─ has info ──► CallConfig handler for that code?
        │                              │
        │                              ├─ yes → runs handler, returns Result
        │                              │
        │                              └─ no ──► errorParser?
        │                                              │
        │                                              ├─ yes → Result.failure(parser result)
        │                                              │
        │                                              └─ no  → Result.failure(AppError.Http)
        ▼
  Result<T> returned to caller
```

The library never imports Retrofit or Ktor. **Your app tells it how to read an exception**; the library decides what to do next.

---

## Setup

Configure once, at app start — `Application.onCreate()` or your DI graph.

```kotlin
ErrorManager.configure(
    httpAdapter = { e ->
        // Return HttpErrorInfo if you recognize the exception, null otherwise
        if (e is retrofit2.HttpException)
            HttpErrorInfo(e.code(), e.response()?.errorBody()?.string())
        else null
    },
    errorParser = { code, rawBody ->
        // Optional: parse the raw JSON body into a typed error
        val parsed = runCatching {
            Json.decodeFromString<BackendError>(rawBody ?: "")
        }.getOrNull()
        AppError.Http(code, parsed?.message ?: rawBody)
    }
)
```

`errorParser` is optional. If omitted, `AppError.Http` will carry the raw body string as-is.

---

## AppError types

```kotlin
sealed class AppError : Exception {
    class Network(cause: Throwable) : AppError   // no internet, timeout, DNS failure
    class Http(val code: Int, val rawBody: String?) : AppError  // server replied 4xx/5xx
    class Unknown(cause: Throwable) : AppError   // unexpected exception
}
```

| Type | When it happens | Properties |
|------|----------------|------------|
| `Network` | `IOException` — no connection, timeout | `cause`, `message` |
| `Http` | Server returned a non-2xx code | `code` (e.g. 404), `rawBody` (JSON string) |
| `Unknown` | Any other unexpected exception | `cause`, `message` |

---

## safeCall

Wraps a suspending block and always returns `Result<T>` — never throws (except `CancellationException`, which is re-thrown so coroutine cancellation works correctly).

```kotlin
val result: Result<User> = safeCall {
    api.getUser(id)
}
```

With per-call configuration (see [Scenario 3](#3-per-call-code-handlers)):

```kotlin
val result: Result<User> = safeCall(
    config = {
        on(401) { sessionManager.logout(); null }
    }
) {
    api.getUser(id)
}
```

---

## Handling errors

After `safeCall`, use Kotlin's `Result` API:

```kotlin
safeCall { api.getUser(id) }
    .onSuccess { user ->
        // use user
    }
    .onFailure { error ->
        when (error) {
            is AppError.Network -> showMessage("Check your connection")
            is AppError.Http    -> showMessage("Server error ${error.code}: ${error.rawBody}")
            is AppError.Unknown -> showMessage("Something went wrong")
        }
    }
```

Or destructure with `fold`:

```kotlin
safeCall { api.getUser(id) }.fold(
    onSuccess = { user -> /* ... */ },
    onFailure = { error -> /* ... */ }
)
```

---

## Scenarios

### 1. Minimal setup — no JSON parsing

When you only care about the HTTP code, not the body.

```kotlin
// Application.onCreate()
ErrorManager.configure(
    httpAdapter = { e ->
        if (e is retrofit2.HttpException)
            HttpErrorInfo(e.code(), null)   // body ignored
        else null
    }
)
```

```kotlin
safeCall { api.deleteAccount() }.onFailure { error ->
    when (error) {
        is AppError.Http -> when (error.code) {
            403 -> showMessage("You don't have permission")
            404 -> showMessage("Account not found")
            else -> showMessage("Server error (${error.code})")
        }
        is AppError.Network -> showMessage("No connection")
        else -> showMessage("Unexpected error")
    }
}
```

---

### 2. Parsing the backend error body

Your backend returns structured JSON on errors:

```json
{ "error": "user_not_found", "message": "No user with that id" }
```

**Step 1 — define the shape:**

```kotlin
@Serializable
data class BackendError(
    val error: String,
    val message: String
)
```

**Step 2 — parse in `errorParser`:**

```kotlin
ErrorManager.configure(
    httpAdapter = { e ->
        if (e is retrofit2.HttpException)
            HttpErrorInfo(e.code(), e.response()?.errorBody()?.string())
        else null
    },
    errorParser = { code, rawBody ->
        val backendError = runCatching {
            Json.decodeFromString<BackendError>(rawBody ?: "")
        }.getOrNull()

        AppError.Http(code, backendError?.message ?: rawBody)
    }
)
```

**Step 3 — use the parsed message:**

```kotlin
safeCall { api.getUser(id) }.onFailure { error ->
    if (error is AppError.Http) {
        showMessage(error.rawBody ?: "Unknown server error")
        // prints: "No user with that id"
    }
}
```

---

### 3. Per-call code handlers

Use `CallConfig` inside `safeCall` when a specific call needs to react to a specific HTTP code differently from the global behaviour.

```kotlin
val result = safeCall<User>(
    config = {
        // on 401: trigger logout and return null (ends the call silently)
        on(401) { sessionManager.logout(); throw AppError.Network(IOException("Session expired")) }

        // on 404: return a default guest user instead of failing
        on(404) { User(id = -1, name = "Guest") }

        // on 403: convert to a specific domain error
        onError(403) { AppError.Http(403, "You are not allowed to do this") }
    }
) {
    api.getUser(id)
}
```

**Rules:**
- `on(code) { value }` — returns `Result.success(value)`. Use when you have a safe fallback.
- `onError(code) { throwable }` — returns `Result.failure(throwable)`. Use when you want a specific error.
- Per-call handlers take priority over `errorParser`.
- Codes without a handler fall through to `errorParser` (or `AppError.Http` if no parser).

---

### 4. Retrofit adapter

```kotlin
ErrorManager.configure(
    httpAdapter = { e ->
        if (e is retrofit2.HttpException) {
            val code = e.code()
            val body = e.response()?.errorBody()?.string()
            HttpErrorInfo(code, body)
        } else null
    }
)
```

> `errorBody()?.string()` reads the stream once — do not call it more than once per exception.

---

### 5. Ktor adapter

```kotlin
ErrorManager.configure(
    httpAdapter = { e ->
        if (e is io.ktor.client.plugins.ResponseException) {
            val code = e.response.status.value
            val body = runCatching { e.response.bodyAsText() }.getOrNull()
            HttpErrorInfo(code, body)
        } else null
    }
)
```

---

### 6. Showing errors in the UI (ViewModel)

A complete real-world pattern with a ViewModel and Compose UI.

```kotlin
data class UiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class UserViewModel(private val api: UserApi) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun loadUser(id: Int) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            safeCall { api.getUser(id) }
                .onSuccess { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                }
                .onFailure { error ->
                    val message = when (error) {
                        is AppError.Network -> "No internet connection"
                        is AppError.Http    -> when (error.code) {
                            401 -> "Session expired, please log in again"
                            404 -> "User not found"
                            500 -> "Server is down, try later"
                            else -> error.rawBody ?: "Server error (${error.code})"
                        }
                        else -> "Something went wrong"
                    }
                    _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                }
        }
    }
}
```

```kotlin
@Composable
fun UserScreen(viewModel: UserViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        state.isLoading       -> CircularProgressIndicator()
        state.errorMessage != null -> Text(state.errorMessage!!, color = Color.Red)
        state.user != null    -> Text("Hello, ${state.user!!.name}")
    }
}
```

---

### 7. Exhaustive when — never miss an error type

Because `AppError` is a `sealed class`, Kotlin enforces exhaustiveness when you use `when` as an expression. This guarantees you handle every type at compile time.

```kotlin
// This does NOT compile if you add a new AppError subtype and forget to handle it
val message: String = when (val error = result.exceptionOrNull()) {
    is AppError.Network -> "No connection"
    is AppError.Http    -> "HTTP ${error.code}"
    is AppError.Unknown -> "Unknown: ${error.message}"
    else                -> "Not an AppError"
}
```

Use this pattern in mappers or UI state builders where missing a case would be a bug.

---

## API reference

### `ErrorManager.configure`

```kotlin
fun configure(
    httpAdapter: HttpExceptionAdapter,
    errorParser: BackendErrorParser? = null
)
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `httpAdapter` | `(Throwable) -> HttpErrorInfo?` | Yes | Converts your HTTP client's exception into `HttpErrorInfo`. Return `null` for non-HTTP exceptions. |
| `errorParser` | `(Int, String?) -> Throwable` | No | Converts the HTTP code + raw body into a typed error. Called when no per-call handler matches. |

---

### `safeCall`

```kotlin
suspend fun <T> safeCall(
    config: (CallConfig<T>.() -> Unit)? = null,
    block: suspend () -> T
): Result<T>
```

| Parameter | Description |
|-----------|-------------|
| `config` | Optional per-call handler configuration. |
| `block` | The suspending network call to wrap. |

---

### `CallConfig<T>`

| Function | Signature | Description |
|----------|-----------|-------------|
| `on` | `on(code: Int, result: () -> T)` | Returns `Result.success(result())` for the given code. |
| `onError` | `onError(code: Int, error: () -> Throwable)` | Returns `Result.failure(error())` for the given code. |

---

### `HttpErrorInfo`

```kotlin
data class HttpErrorInfo(val code: Int, val rawBody: String?)
```

Internal bridge between your HTTP client and the library. You produce it in `httpAdapter`; the library consumes it.

---

### `AppError`

```kotlin
sealed class AppError(message: String?, cause: Throwable?) : Exception(message, cause) {
    class Network(cause: Throwable) : AppError(cause.message, cause)
    class Http(val code: Int, val rawBody: String?) : AppError("HTTP $code")
    class Unknown(cause: Throwable) : AppError(cause.message, cause)
}
```
