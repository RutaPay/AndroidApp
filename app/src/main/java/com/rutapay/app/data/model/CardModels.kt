package com.rutapay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CardResponseDto(
    @SerialName("UID") val uid: String = "",
    @SerialName("Balance") val balance: Double = 0.0,
    @SerialName("State") val state: String = "Active",
    @SerialName("UserFullName") val userFullName: String = "",
    @SerialName("UserEmail") val userEmail: String = ""
)

@Serializable
data class BalanceChangeDto(
    @SerialName("Balance") val balance: Double,
    @SerialName("OppType") val oppType: String = "recharge",
    @SerialName("PaymentMethod") val paymentMethod: String = "Card"
)

@Serializable
data class BalanceChangeResponseDto(
    @SerialName("Message") val message: String = "",
    @SerialName("NewBalance") val newBalance: Double = 0.0,
    @SerialName("TransactionId") val transactionId: JsonElement? = null,
    @SerialName("Reference") val reference: String = ""
)

@Serializable
data class ToggleCardResponseDto(
    @SerialName("Message") val message: String = "",
    @SerialName("CurrentState") val currentState: String = "Active"
)
