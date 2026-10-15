package com.rutapay.app.ui.dashboard.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rutapay.app.data.model.CardResponseDto
import com.rutapay.app.data.model.TransactionDto
import com.rutapay.app.data.model.UserDto
import com.rutapay.app.data.repository.AuthRepository
import com.rutapay.app.data.repository.CardRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CardUiState(
    val isLoading: Boolean = false,
    val card: CardResponseDto? = null,
    val user: UserDto? = null,
    val transactions: List<TransactionDto> = emptyList(),
    val error: String? = null,
    val lastMessage: String? = null,
    val lastReference: String? = null
)

class CardViewModel(
    private val cardRepository: CardRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardUiState())
    val uiState: StateFlow<CardUiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val cardDeferred = async { cardRepository.getMyCard() }
            val txDeferred = async { cardRepository.getTransactions() }
            val userDeferred = async { authRepository.getCurrentUser() }
            val cardResult = cardDeferred.await()
            val txResult = txDeferred.await()
            val userResult = userDeferred.await()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                card = cardResult.getOrNull(),
                transactions = txResult.getOrNull().orEmpty(),
                user = userResult.getOrNull() ?: _uiState.value.user,
                error = cardResult.exceptionOrNull()?.message
                    ?: txResult.exceptionOrNull()?.message
            )
        }
    }

    fun toggleStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = cardRepository.toggleCardStatus()
            result.fold(
                onSuccess = { toggle ->
                    _uiState.value = _uiState.value.copy(
                        lastMessage = toggle.message,
                        lastReference = null,
                        card = _uiState.value.card?.copy(state = toggle.currentState)
                    )
                    load()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun recharge(amount: Double, paymentMethod: String = "Card", onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, lastMessage = null)
            val result = cardRepository.rechargeBalance(amount, paymentMethod)
            result.fold(
                onSuccess = { res ->
                    _uiState.value = _uiState.value.copy(
                        lastMessage = "¡Recarga realizada con éxito! Se han acreditado \$$amount MXN. Ref: ${res.reference.ifBlank { "OK" }}",
                        lastReference = res.reference
                    )
                    load()
                    onDone()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(error = null, lastMessage = null, lastReference = null)
    }

    class Factory(
        private val cardRepository: CardRepository,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CardViewModel(cardRepository, authRepository) as T
        }
    }
}
