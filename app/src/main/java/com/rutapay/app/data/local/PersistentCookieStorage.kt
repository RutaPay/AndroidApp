package com.rutapay.app.data.local

import android.content.Context
import android.util.Log
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.fillDefaults
import io.ktor.client.plugins.cookies.matches
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.parseServerSetCookieHeader
import io.ktor.http.renderSetCookieHeader
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Almacenamiento persistente de cookies para Ktor.
 *
 * La API autentica con una cookie JWT HttpOnly (`token`, 7 días). El
 * `AcceptAllCookiesStorage` de Ktor vive solo en memoria, así que al cerrar
 * la app se perdía la sesión y el usuario tenía que iniciar sesión otra vez.
 * Aquí las cookies se guardan en SharedPreferences (privadas de la app) y se
 * reconstruyen al arrancar.
 */
class PersistentCookieStorage(context: Context) : CookiesStorage {

    companion object {
        private const val TAG = "CookieStorage"
        private const val PREFS = "rutapay_auth_cookies"
        private const val KEY = "cookies"
        const val AUTH_COOKIE = "token"
    }

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val mutex = Mutex()

    /** JWT de la sesión, útil para conexiones que no usan cookies (SignalR). */
    val token: String?
        get() {
            val now = System.currentTimeMillis()
            return load()
                .firstOrNull { it.name == AUTH_COOKIE && !it.isExpired(now) }
                ?.value
        }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) = mutex.withLock {
        val normalized = cookie.fillDefaults(requestUrl)
        val now = System.currentTimeMillis()
        val maxAge = normalized.maxAge
        val expiresAt = when {
            maxAge != null -> if (maxAge <= 0) 0L else now + maxAge * 1000L
            normalized.expires != null -> normalized.expires!!.timestamp
            else -> null
        }

        val current = load().filterNot { it.sameKeyAs(normalized) }

        // maxAge <= 0 (o ya vencida) = el servidor pidió borrar la cookie.
        if (expiresAt != null && expiresAt <= now) {
            save(current)
            return@withLock
        }

        // Guardamos una fecha absoluta: `maxAge` es relativo al momento en que
        // el servidor emitió la cookie, no al arranque de la app.
        val absolute = if (normalized.expires == null && expiresAt != null) {
            normalized.copy(expires = GMTDate(expiresAt), maxAge = null)
        } else {
            normalized.copy(maxAge = null)
        }

        save(current + absolute)
    }

    override suspend fun get(requestUrl: Url): List<Cookie> = mutex.withLock {
        val all = load()
        val now = System.currentTimeMillis()
        val valid = all.filter { !it.isExpired(now) && it.matches(requestUrl) }
        if (valid.size != all.size) save(valid)
        valid
    }

    suspend fun clear() = mutex.withLock {
        prefs.edit().remove(KEY).apply()
    }

    override fun close() {
        // SharedPreferences se guarda de forma síncrona en cada cambio.
    }

    private fun load(): List<Cookie> {
        val raw = try {
            prefs.getStringSet(KEY, emptySet()).orEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron leer las cookies guardadas", e)
            emptySet()
        }

        return raw.mapNotNull { value ->
            try {
                parseServerSetCookieHeader(value)
            } catch (e: Exception) {
                Log.w(TAG, "Cookie guardada inválida, se descarta", e)
                null
            }
        }
    }

    private fun save(cookies: List<Cookie>) {
        val values = cookies.mapNotNull { cookie ->
            try {
                renderSetCookieHeader(cookie)
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo serializar la cookie ${cookie.name}", e)
                null
            }
        }.toSet()

        prefs.edit().putStringSet(KEY, values).apply()
    }

    private fun Cookie.isExpired(now: Long): Boolean =
        expires?.timestamp?.let { it <= now } == true

    private fun Cookie.sameKeyAs(other: Cookie): Boolean =
        name == other.name && domain == other.domain && path == other.path
}
