package com.rutapay.app.ui.dashboard.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rutapay.app.data.model.RewardDto
import com.rutapay.app.data.model.UserDto
import com.rutapay.app.data.model.UserRewardDto
import com.rutapay.app.data.repository.AuthRepository
import com.rutapay.app.data.repository.RewardsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RewardsUiState(
    val isLoading: Boolean = false,
    val isRedeeming: Boolean = false,
    val rewards: List<RewardDto> = emptyList(),
    val myRewards: List<UserRewardDto> = emptyList(),
    val user: UserDto? = null,
    val error: String? = null,
    val lastCouponCode: String? = null,
    val lastCouponTitle: String? = null
)

class RewardsViewModel(
    private val rewardsRepository: RewardsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RewardsUiState())
    val uiState: StateFlow<RewardsUiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val rewardsDeferred = async { rewardsRepository.getRewards() }
            val mineDeferred = async { rewardsRepository.getMyRewards() }
            val userDeferred = async { authRepository.getCurrentUser() }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                rewards = rewardsDeferred.await().getOrNull().orEmpty(),
                myRewards = mineDeferred.await().getOrNull().orEmpty(),
                user = userDeferred.await().getOrNull() ?: _uiState.value.user,
                error = rewardsDeferred.await().exceptionOrNull()?.message
            )
        }
    }

    fun redeem(reward: RewardDto) {
        val currentPoints = _uiState.value.user?.points ?: 0
        if (currentPoints < reward.pointsCost) {
            _uiState.value = _uiState.value.copy(
                error = "Puntos Insuficientes. Tienes $currentPoints puntos y requieres ${reward.pointsCost} puntos para este beneficio."
            )
            return
        }
        _uiState.value = _uiState.value.copy(isRedeeming = true, error = null)
        viewModelScope.launch {
            val result = rewardsRepository.redeem(reward.id)
            result.fold(
                onSuccess = { res ->
                    _uiState.value = _uiState.value.copy(
                        isRedeeming = false,
                        lastCouponCode = res.couponCode,
                        lastCouponTitle = reward.title
                    )
                    load()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isRedeeming = false, error = e.message)
                }
            )
        }
    }

    fun dismissCoupon() {
        _uiState.value = _uiState.value.copy(lastCouponCode = null, lastCouponTitle = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(
        private val rewardsRepository: RewardsRepository,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RewardsViewModel(rewardsRepository, authRepository) as T
        }
    }
}
