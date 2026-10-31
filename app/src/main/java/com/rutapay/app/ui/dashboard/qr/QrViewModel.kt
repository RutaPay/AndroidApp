package com.rutapay.app.ui.dashboard.qr

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rutapay.app.data.model.AuthorizePaymentResultDto
import com.rutapay.app.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QrUiState(
    val isAuthorizing: Boolean = false,
    val result: AuthorizePaymentResultDto? = null,
    val error: String? = null,
    val lastToken: String? = null,
    val lastMethod: String? = null
)

/**
 * Flujo actual de pago: la terminal genera un token de sesión y lo muestra en
 * QR (o lo emite por NFC). La app del pasajero lo lee y lo autoriza con su
 * propia sesión; la API cobra y responde con el resultado.
 */
class QrViewModel(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrUiState())
    val uiState: StateFlow<QrUiState> = _uiState.asStateFlow()

    fun authorize(rawCode: String, paymentMethod: String) {
        if (_uiState.value.isAuthorizing) return

        val token = extractTerminalToken(rawCode)
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "No se leyó ningún código de pago.",
                lastToken = null,
                lastMethod = null
            )
            return
        }

        _uiState.value = QrUiState(
            isAuthorizing = true,
            lastToken = token,
            lastMethod = paymentMethod
        )

        viewModelScope.launch {
            Log.d("QrViewModel", "Autorizando token por $paymentMethod")
            paymentRepository.authorizePayment(token, paymentMethod).fold(
                onSuccess = { result ->
                    _uiState.value = QrUiState(
                        isAuthorizing = false,
                        result = result,
                        lastToken = token,
                        lastMethod = paymentMethod
                    )
                },
                onFailure = { e ->
                    _uiState.value = QrUiState(
                        isAuthorizing = false,
                        error = e.message ?: "No se pudo autorizar el pago.",
                        lastToken = token,
                        lastMethod = paymentMethod
                    )
                }
            )
        }
    }

    fun retry() {
        val state = _uiState.value
        val token = state.lastToken ?: return
        val method = state.lastMethod ?: "QR_Terminal"
        authorize(token, method)
    }

    fun clear() {
        _uiState.value = QrUiState()
    }

    /**
     * El QR puede traer el JWT de la terminal tal cual o dentro de una URL
     * (`rutapay://pay?token=...`, `https://.../pago/<token>`). Normalizamos
     * antes de enviarlo a la API.
     */
    private fun extractTerminalToken(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""

        if (looksLikeJwt(trimmed)) return trimmed

        val queryToken = runCatching {
            android.net.Uri.parse(trimmed).getQueryParameter("token")
        }.getOrNull()
        if (!queryToken.isNullOrBlank()) return queryToken.trim()

        val lastSegment = runCatching {
            android.net.Uri.parse(trimmed).lastPathSegment
        }.getOrNull()
        if (!lastSegment.isNullOrBlank() && looksLikeJwt(lastSegment)) return lastSegment

        // Algunos QRs envuelven el token en JSON o texto. Si hay un JWT dentro,
        // lo extraemos; si no, se manda tal cual y la API responde "Invalid".
        jwtRegex.find(trimmed)?.value?.let { return it }

        return trimmed
    }

    private fun looksLikeJwt(value: String): Boolean {
        val parts = value.split('.')
        return parts.size == 3 && parts.all { it.isNotBlank() }
    }

    private companion object {
        val jwtRegex = Regex("[A-Za-z0-9_-]{8,}\\.[A-Za-z0-9_-]{8,}\\.[A-Za-z0-9_-]{8,}")
    }

    class Factory(
        private val paymentRepository: PaymentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QrViewModel(paymentRepository) as T
        }
    }
}
