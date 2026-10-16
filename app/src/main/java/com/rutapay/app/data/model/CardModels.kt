package com.rutapay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// La API serializa en camelCase (p. ej. "uid", "balance").

@Serializable
data class CardResponseDto(
    @SerialName("uid") val uid: String = "",
    @SerialName("balance") val balance: Double = 0.0,
    @SerialName("state") val state: String = "Active",
    @SerialName("userFullName") val userFullName: String = "",
    @SerialName("userEmail") val userEmail: String = ""
)

@Serializable
data class BalanceChangeDto(
    @SerialName("balance") val balance: Double,
    @SerialName("oppType") val oppType: String = "recharge",
    @SerialName("paymentMethod") val paymentMethod: String = "Card"
)

@Serializable
data class BalanceChangeResponseDto(
    @SerialName("message") val message: String = "",
    @SerialName("newBalance") val newBalance: Double = 0.0,
    @SerialName("transactionId") val transactionId: JsonElement? = null,
    @SerialName("reference") val reference: String = ""
)

@Serializable
data class ToggleCardResponseDto(
    @SerialName("message") val message: String = "",
    @SerialName("currentState") val currentState: String = "Active"
)
