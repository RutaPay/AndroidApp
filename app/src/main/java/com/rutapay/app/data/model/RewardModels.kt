package com.rutapay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// La API serializa en camelCase (p. ej. "pointsCost").

@Serializable
data class RewardDto(
    @SerialName("id") val id: Int = 0,
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("pointsCost") val pointsCost: Int = 0,
    @SerialName("rewardType") val rewardType: String = "",
    @SerialName("value") val value: Double = 0.0,
    @SerialName("isActive") val isActive: Boolean = true
)

@Serializable
data class RedeemRewardDto(
    @SerialName("rewardId") val rewardId: Int
)

@Serializable
data class UserRewardDto(
    @SerialName("id") val id: String = "",
    @SerialName("rewardId") val rewardId: Int = 0,
    @SerialName("title") val title: String = "Recompensa",
    @SerialName("redeemedAt") val redeemedAt: String = "",
    @SerialName("code") val code: String = "",
    @SerialName("isUsed") val isUsed: Boolean = false
)

@Serializable
data class RedeemResultDto(
    @SerialName("message") val message: String = "",
    @SerialName("remainingPoints") val remainingPoints: Int = 0,
    @SerialName("newBalance") val newBalance: Double = 0.0,
    @SerialName("couponCode") val couponCode: String = "",
    @SerialName("rewardType") val rewardType: String = ""
)
