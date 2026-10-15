package com.rutapay.app.ui.dashboard.qr

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.microsoft.signalr.HubConnectionState
import com.rutapay.app.data.local.SessionManager
import com.rutapay.app.data.model.PaymentQrTokenResponseDto
import com.rutapay.app.data.model.ValidatePaymentResultDto
import com.rutapay.app.data.repository.PaymentRepository
import com.rutapay.app.network.PaymentStatusUpdate
import com.rutapay.app.network.SignalRManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class QrUiState(
    val isLoading: Boolean = false,
    val payment: PaymentQrTokenResponseDto? = null,
    val error: String? = null,
    val liveUpdate: PaymentStatusUpdate? = null,
    val liveConnected: Boolean = false,
    val isValidating: Boolean = false,
    val scanResult: ValidatePaymentResultDto? = null,
    val scanError: String? = null
)

class QrViewModel(
    private val paymentRepository: PaymentRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrUiState())
    val uiState: StateFlow<QrUiState> = _uiState.asStateFlow()

    private var signalRManager: SignalRManager? = null
    private var liveJob: Job? = null

    fun generate(route: String = "Ruta General Celaya") {
        stopLive()
        _uiState.value = QrUiState(isLoading = true)
        viewModelScope.launch {
            val result = paymentRepository.generateQrPayment(route)
            result.fold(
                onSuccess = { payment ->
                    _uiState.value = QrUiState(payment = payment)
                    startLive(payment.transactionId)
                },
                onFailure = { e ->
                    _uiState.value = QrUiState(error = e.message ?: "No se pudo generar el QR")
                }
            )
        }
    }

    /**
     * Confirmación en tiempo real: se une al grupo del transactionId en el
     * hub y escucha "Transaction Status" hasta que el validador procese el QR.
     */
    private fun startLive(transactionId: String) {
        if (transactionId.isBlank()) return
        viewModelScope.launch {
            try {
                val hubUrl = sessionManager.signalrUrl.first()
                    .ifBlank { SessionManager.DEFAULT_SIGNALR_URL }
                val manager = SignalRManager(hubUrl)
                signalRManager = manager
                liveJob = launch {
                    manager.statusUpdates.collect { update ->
                        _uiState.value = _uiState.value.copy(liveUpdate = update)
                    }
                }
                launch {
                    manager.connectionState.collect { state ->
                        _uiState.value = _uiState.value.copy(
                            liveConnected = state == HubConnectionState.CONNECTED
                        )
                    }
                }
                manager.connectAndJoin(transactionId)
            } catch (e: Exception) {
                Log.e("QrViewModel", "No se pudo iniciar el canal en vivo", e)
            }
        }
    }

    fun stopLive() {
        liveJob?.cancel()
        liveJob = null
        signalRManager?.stop()
        signalRManager = null
    }

    fun dismissLiveUpdate() {
        _uiState.value = _uiState.value.copy(liveUpdate = null)
    }

    fun validateScanned(token: String, route: String? = null) {
        _uiState.value = _uiState.value.copy(
            isValidating = true, scanResult = null, scanError = null
        )
        viewModelScope.launch {
            val result = paymentRepository.validatePayment(token.trim(), route)
            result.fold(
                onSuccess = { validation ->
                    _uiState.value = _uiState.value.copy(
                        isValidating = false, scanResult = validation
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isValidating = false,
                        scanError = e.message ?: "No se pudo validar el código"
                    )
                }
            )
        }
    }

    fun clearScan() {
        _uiState.value = _uiState.value.copy(scanResult = null, scanError = null)
    }

    override fun onCleared() {
        super.onCleared()
        stopLive()
        signalRManager?.release()
    }

    class Factory(
        private val paymentRepository: PaymentRepository,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QrViewModel(paymentRepository, sessionManager) as T
        }
    }
}
