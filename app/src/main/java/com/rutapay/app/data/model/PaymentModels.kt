package com.rutapay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// La API serializa en camelCase (p. ej. "transactionId", "fareDeducted").

@Serializable
data class GeneratePaymentRequestDto(
    @SerialName("route") val route: String? = null,
    @SerialName("busUnitId") val busUnitId: String? = null
)

@Serializable
data class PaymentQrTokenResponseDto(
    @SerialName("transactionId") val transactionId: String = "",
    @SerialName("token") val token: String = "",
    @SerialName("expiresInSeconds") val expiresInSeconds: Int = 300,
    @SerialName("route") val route: String = "",
    @SerialName("userFullName") val userFullName: String = "",
    @SerialName("cardBalance") val cardBalance: Double = 0.0
)

@Serializable
data class ValidatePaymentRequestDto(
    @SerialName("token") val token: String,
    @SerialName("route") val route: String? = null,
    @SerialName("busUnitId") val busUnitId: String? = null,
    @SerialName("srGroupId") val srGroupId: String? = null
)

@Serializable
data class ValidatePaymentResultDto(
    @SerialName("status") val status: String = "",
    @SerialName("message") val message: String? = null,
    @SerialName("transactionId") val transactionId: String? = null,
    @SerialName("reference") val reference: String? = null,
    @SerialName("passengerName") val passengerName: String? = null,
    @SerialName("tariffType") val tariffType: String? = null,
    @SerialName("fareDeducted") val fareDeducted: Double? = null,
    @SerialName("remainingBalance") val remainingBalance: Double? = null,
    @SerialName("pointsEarned") val pointsEarned: Int? = null,
    @SerialName("totalPoints") val totalPoints: Int? = null,
    @SerialName("route") val route: String? = null,
    @SerialName("busUnit") val busUnit: String? = null,
    @SerialName("requiredFare") val requiredFare: Double? = null,
    @SerialName("currentBalance") val currentBalance: Double? = null
)
