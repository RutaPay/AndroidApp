package com.rutapay.app.ui.dashboard.qr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rutapay.app.data.model.CardResponseDto
import com.rutapay.app.data.model.UserDto
import com.rutapay.app.data.model.ValidatePaymentResultDto
import com.rutapay.app.data.repository.AuthRepository
import com.rutapay.app.data.repository.CardRepository
import com.rutapay.app.data.repository.PaymentRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QrUiState(
    val isLoading: Boolean = false,
    val user: UserDto? = null,
    val card: CardResponseDto? = null,
    val isValidating: Boolean = false,
    val scanResult: ValidatePaymentResultDto? = null,
    val scanError: String? = null
)

class QrViewModel(
    private val paymentRepository: PaymentRepository,
    private val authRepository: AuthRepository,
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrUiState())
    val uiState: StateFlow<QrUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val userDef = async { authRepository.getCurrentUser() }
            val cardDef = async { cardRepository.getMyCard() }
            val userResult = userDef.await()
            val cardResult = cardDef.await()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                user = userResult.getOrNull() ?: _uiState.value.user,
                card = cardResult.getOrNull() ?: _uiState.value.card
            )
        }
    }

    fun validateScanned(token: String, route: String? = null) {
        _uiState.value = _uiState.value.copy(
            isValidating = true,
            scanResult = null,
            scanError = null
        )
        viewModelScope.launch {
            val result = paymentRepository.validatePayment(token.trim(), route)
            result.fold(
                onSuccess = { validation ->
                    _uiState.value = _uiState.value.copy(
                        isValidating = false,
                        scanResult = validation
                    )
                    loadData()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isValidating = false,
                        scanError = e.message ?: "No se pudo procesar el código de pago"
                    )
                }
            )
        }
    }

    fun clearScan() {
        _uiState.value = _uiState.value.copy(scanResult = null, scanError = null)
    }

    class Factory(
        private val paymentRepository: PaymentRepository,
        private val authRepository: AuthRepository,
        private val cardRepository: CardRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QrViewModel(paymentRepository, authRepository, cardRepository) as T
        }
    }
}
