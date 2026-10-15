package com.rutapay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeneratePaymentRequestDto(
    @SerialName("Route") val route: String? = null,
    @SerialName("BusUnitId") val busUnitId: String? = null
)

@Serializable
data class PaymentQrTokenResponseDto(
    @SerialName("TransactionId") val transactionId: String = "",
    @SerialName("Token") val token: String = "",
    @SerialName("ExpiresInSeconds") val expiresInSeconds: Int = 300,
    @SerialName("Route") val route: String = "",
    @SerialName("UserFullName") val userFullName: String = "",
    @SerialName("CardBalance") val cardBalance: Double = 0.0
)

@Serializable
data class ValidatePaymentRequestDto(
    @SerialName("Token") val token: String,
    @SerialName("Route") val route: String? = null,
    @SerialName("BusUnitId") val busUnitId: String? = null,
    @SerialName("SrGroupId") val srGroupId: String? = null
)

@Serializable
data class ValidatePaymentResultDto(
    @SerialName("Status") val status: String = "",
    @SerialName("Message") val message: String? = null,
    @SerialName("TransactionId") val transactionId: String? = null,
    @SerialName("Reference") val reference: String? = null,
    @SerialName("PassengerName") val passengerName: String? = null,
    @SerialName("TariffType") val tariffType: String? = null,
    @SerialName("FareDeducted") val fareDeducted: Double? = null,
    @SerialName("RemainingBalance") val remainingBalance: Double? = null,
    @SerialName("PointsEarned") val pointsEarned: Int? = null,
    @SerialName("TotalPoints") val totalPoints: Int? = null,
    @SerialName("Route") val route: String? = null,
    @SerialName("BusUnit") val busUnit: String? = null,
    @SerialName("RequiredFare") val requiredFare: Double? = null,
    @SerialName("CurrentBalance") val currentBalance: Double? = null
)
