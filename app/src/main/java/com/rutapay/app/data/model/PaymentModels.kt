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
