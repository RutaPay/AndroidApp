package com.rutapay.app.data.remote

import android.util.Log
import com.rutapay.app.data.local.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ApiClient(
    private val sessionManager: SessionManager
) {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        explicitNulls = false
        // La API puede devolver nulls explícitos (p. ej. AccountType cuando el
        // usuario no tiene roles). Sin esto, un solo null revienta todo el
        // parseo y la vista queda en blanco; con esto se usa el valor default.
        coerceInputValues = true
    }

    val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("Ktor", message)
                }
            }
            level = LogLevel.INFO
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
        HttpResponseValidator {
            validateResponse { response ->
                if (response.status.value == 401) {
                    Log.w("ApiClient", "401 Unauthorized: ${response.call.request.url} — limpiando sesión")
                    // Guardián global: cualquier 401 invalida la sesión local y
                    // MainActivity redirige a login si está en el dashboard.
                    try {
                        sessionManager.clearSession()
                    } catch (e: Exception) {
                        Log.e("ApiClient", "No se pudo limpiar la sesión", e)
                    }
                }
            }
        }
    }

    suspend fun baseUrl(): String {
        val url = sessionManager.apiUrl.first().trim().trimEnd('/')
        return url.ifBlank { SessionManager.DEFAULT_API_URL }
    }

    /**
     * Lee el body como texto, lo registra en LogCat (para diagnosticar
     * desajustes con la API) y lo parsea. Centraliza el parseo de todas
     * las respuestas para que un campo inesperado no falle en silencio.
     */
    suspend inline fun <reified T> parseAndLog(
        response: HttpResponse,
        tag: String,
        label: String
    ): T {
        val text = response.bodyAsText()
        Log.d(tag, "$label (${response.status.value}) body: ${text.take(2000)}")
        return json.decodeFromString(text)
    }

    fun close() {
        client.close()
    }
}

suspend inline fun <reified T> parseApiErrorBody(body: String): T? {
    return null
}
