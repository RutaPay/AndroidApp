package com.rutapay.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rutapay.app.data.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionManager(private val context: Context) {

    companion object {
        val API_URL = stringPreferencesKey("api_url")
        val SIGNALR_URL = stringPreferencesKey("signalr_url")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_FULL_NAME = stringPreferencesKey("user_full_name")
        val CARD_UID = stringPreferencesKey("card_uid")

        const val DEFAULT_API_URL = "https://rutapayapi.runasp.net"
        const val DEFAULT_SIGNALR_URL = "https://rutapayapi.runasp.net/Payments/paymentHub"
    }

    val apiUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[API_URL] ?: DEFAULT_API_URL
    }

    val signalrUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SIGNALR_URL] ?: DEFAULT_SIGNALR_URL
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN] ?: false
    }

    val userEmail: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_EMAIL] ?: ""
    }

    val userFullName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_FULL_NAME] ?: ""
    }

    val cardUid: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CARD_UID] ?: ""
    }

    suspend fun saveConfig(api: String, signalr: String) {
        context.dataStore.edit { prefs ->
            prefs[API_URL] = api
            prefs[SIGNALR_URL] = signalr
        }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = loggedIn
        }
    }

    suspend fun saveUserSession(email: String, fullName: String, cardUid: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_EMAIL] = email
            prefs[USER_FULL_NAME] = fullName
            prefs[CARD_UID] = cardUid
            prefs[IS_LOGGED_IN] = true
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(IS_LOGGED_IN)
            prefs.remove(USER_EMAIL)
            prefs.remove(USER_FULL_NAME)
            prefs.remove(CARD_UID)
            prefs[IS_LOGGED_IN] = false
        }
    }
}
