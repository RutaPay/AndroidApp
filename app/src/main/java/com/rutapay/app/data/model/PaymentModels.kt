package com.rutapay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// La API serializa en camelCase (p. ej. "terminalToken", "fareDeducted").
// Flujo actual: la terminal emite un token de sesión (QR/NFC) y la app del
// pasajero lo autoriza con su propia sesión.

@Serializable
data class AuthorizePaymentRequestDto(
    @SerialName("terminalToken") val terminalToken: String,
    @SerialName("paymentMethod") val paymentMethod: String = "QR_Terminal"
)

@Serializable
data class AuthorizePaymentResultDto(
    @SerialName("status") val status: String = "",
    @SerialName("message") val message: String? = null,
    @SerialName("alreadyProcessed") val alreadyProcessed: Boolean = false,
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
    @SerialName("terminalCode") val terminalCode: String? = null,
    @SerialName("requiredFare") val requiredFare: Double? = null,
    @SerialName("currentBalance") val currentBalance: Double? = null,
    @SerialName("timestamp") val timestamp: String? = null
)
