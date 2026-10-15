package com.rutapay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RewardDto(
    @SerialName("Id") val id: Int = 0,
    @SerialName("Title") val title: String = "",
    @SerialName("Description") val description: String = "",
    @SerialName("PointsCost") val pointsCost: Int = 0,
    @SerialName("RewardType") val rewardType: String = "",
    @SerialName("Value") val value: Double = 0.0,
    @SerialName("IsActive") val isActive: Boolean = true
)

@Serializable
data class RedeemRewardDto(
    @SerialName("RewardId") val rewardId: Int
)

@Serializable
data class UserRewardDto(
    @SerialName("Id") val id: String = "",
    @SerialName("RewardId") val rewardId: Int = 0,
    @SerialName("Title") val title: String = "Recompensa",
    @SerialName("RedeemedAt") val redeemedAt: String = "",
    @SerialName("Code") val code: String = "",
    @SerialName("IsUsed") val isUsed: Boolean = false
)

@Serializable
data class RedeemResultDto(
    @SerialName("Message") val message: String = "",
    @SerialName("RemainingPoints") val remainingPoints: Int = 0,
    @SerialName("NewBalance") val newBalance: Double = 0.0,
    @SerialName("CouponCode") val couponCode: String = "",
    @SerialName("RewardType") val rewardType: String = ""
)
