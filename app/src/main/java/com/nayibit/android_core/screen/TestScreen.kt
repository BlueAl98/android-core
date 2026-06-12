package com.nayibit.android_core.screen

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.nayibit.errorManager.AppError
import com.nayibit.errorManager.ErrorManager
import com.nayibit.errorManager.HttpErrorInfo
import com.nayibit.errorManager.safeCall
import java.io.IOException

private const val TAG = "ErrorManagerTest"

data class User(val id: Int, val name: String)

// Simulates the exception Retrofit/Ktor would throw for HTTP errors
data class FakeHttpException(val code: Int, val body: String) : Exception("HTTP $code")

@Composable
fun TestScreen() {
    LaunchedEffect(Unit) {

        ErrorManager.configure(
            httpAdapter = { e ->
                if (e is FakeHttpException) HttpErrorInfo(e.code, e.body) else null
            },
            errorParser = { code, rawBody ->
                // rawBody is the JSON string your backend returns on error
                // parse it however your project needs
                AppError.Http(code, rawBody)
            }
        )

        // Fake a backend call that returns 404
       safeCall<User> {
            throw FakeHttpException(404, """{"error":"user_not_found","message":"No user with that id"}""")
        }.onFailure { e->

        }






    }
}

@Preview(showBackground = true)
@Composable
fun TestScreenPreview() {
    TestScreen()
}
