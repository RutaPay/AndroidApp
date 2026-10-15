package com.rutapay.app.ui.dashboard.qr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rutapay.app.data.model.PaymentQrTokenResponseDto
import com.rutapay.app.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QrUiState(
    val isLoading: Boolean = false,
    val payment: PaymentQrTokenResponseDto? = null,
    val error: String? = null
)

class QrViewModel(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrUiState())
    val uiState: StateFlow<QrUiState> = _uiState.asStateFlow()

    fun generate(route: String = "Ruta General Celaya") {
        _uiState.value = QrUiState(isLoading = true)
        viewModelScope.launch {
            val result = paymentRepository.generateQrPayment(route)
            result.fold(
                onSuccess = { payment ->
                    _uiState.value = QrUiState(payment = payment)
                },
                onFailure = { e ->
                    _uiState.value = QrUiState(error = e.message ?: "No se pudo generar el QR")
                }
            )
        }
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
