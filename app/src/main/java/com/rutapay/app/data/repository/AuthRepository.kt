package com.rutapay.app.data.repository

import android.util.Log
import com.rutapay.app.data.local.SessionManager
import com.rutapay.app.data.model.LoginRequestDto
import com.rutapay.app.data.model.NewUserDto
import com.rutapay.app.data.model.RegisterRequestDto
import com.rutapay.app.data.model.UserDto
import com.rutapay.app.data.remote.ApiClient
import com.rutapay.app.ui.theme.WebErrors
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first

class AuthRepository(
    private val apiClient: ApiClient,
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, password: String): Result<NewUserDto> {
        return try {
            val base = apiClient.baseUrl()
            val response = apiClient.client.post("$base/api/account/login") {
                setBody(LoginRequestDto(email.trim(), password))
            }
            if (response.status.isSuccess()) {
                val user = response.body<NewUserDto>()
                sessionManager.saveUserSession(
                    email = user.email.ifBlank { email },
                    fullName = user.userName,
                    cardUid = ""
                )
                refreshCurrentUser()
                Result.success(user)
            } else {
                val msg = response.bodyAsText()
                Log.w("AuthRepo", "login failed ${response.status}: $msg")
                if (response.status.value == 401) {
                    Result.failure(Exception(WebErrors.LOGIN_BAD_CREDENTIALS))
                } else {
                    Result.failure(Exception(errorMessage(msg, WebErrors.NETWORK_ERROR)))
                }
            }
        } catch (e: Exception) {
            Log.e("AuthRepo", "login error", e)
            Result.failure(Exception(WebErrors.LOGIN_NO_CONNECTION))
        }
    }

    suspend fun register(
        fullName: String,
        lastNames: String,
        email: String,
        password: String,
        phoneNumber: String
    ): Result<NewUserDto> {
        return try {
            val base = apiClient.baseUrl()
            val response = apiClient.client.post("$base/api/account/register") {
                setBody(
                    RegisterRequestDto(
                        fullName = fullName.trim(),
                        lastNames = lastNames.trim(),
                        email = email.trim(),
                        password = password,
                        phoneNumber = phoneNumber.trim()
                    )
                )
            }
            if (response.status.isSuccess()) {
                val user = response.body<NewUserDto>()
                Result.success(user)
            } else {
                val msg = response.bodyAsText()
                Log.w("AuthRepo", "register failed ${response.status}: $msg")
                if (msg.contains("Email already exists.")) {
                    Result.failure(Exception(WebErrors.EMAIL_IN_USE))
                } else {
                    Result.failure(Exception(errorMessage(msg, WebErrors.REGISTER_ERROR)))
                }
            }
        } catch (e: Exception) {
            Log.e("AuthRepo", "register error", e)
            Result.failure(Exception(WebErrors.NETWORK_ERROR))
        }
    }

    suspend fun getCurrentUser(): Result<UserDto> {
        return try {
            val base = apiClient.baseUrl()
            val response = apiClient.client.get("$base/api/account")
            if (response.status.isSuccess()) {
                try {
                    val user = apiClient.parseAndLog<UserDto>(response, "AuthRepo", "GET /api/account")
                    sessionManager.saveUserSession(
                        user.email.ifBlank { "" },
                        user.fullName.ifBlank { user.userName },
                        user.cardUID
                    )
                    Result.success(user)
                } catch (e: Exception) {
                    Log.e("AuthRepo", "getCurrentUser: parseo fallido", e)
                    Result.failure(Exception("No se pudieron leer los datos del usuario"))
                }
            } else {
                val body = response.bodyAsText()
                Log.w("AuthRepo", "getCurrentUser failed ${response.status}: ${body.take(300)}")
                Result.failure(Exception("Sesión inválida"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepo", "getCurrentUser error", e)
            Result.failure(e)
        }
    }

    /**
     * Revalida la sesión contra la API. La cookie JWT HttpOnly vive solo en
     * memoria (Ktor), así que al reiniciar la app el flag local puede decir
     * "logueado" cuando la sesión real ya murió.
     */
    suspend fun validateSession(): Boolean {
        return try {
            getCurrentUser().isSuccess
        } catch (_: Exception) {
            false
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val base = apiClient.baseUrl()
            try {
                apiClient.client.post("$base/api/account/logout")
            } catch (_: Exception) {
            }
            sessionManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            sessionManager.clearSession()
            Result.success(Unit)
        }
    }

    suspend fun refreshCurrentUser() {
        try {
            getCurrentUser()
        } catch (_: Exception) {
        }
    }

    private fun errorMessage(raw: String, fallback: String): String {
        if (raw.isBlank()) return fallback
        return if (raw.length > 300) fallback else raw
    }

    private suspend fun sessionManagerDefaultHint(): String {
        return try {
            sessionManager.apiUrl.first()
        } catch (_: Exception) {
            SessionManager.DEFAULT_API_URL
        }
    }
}
