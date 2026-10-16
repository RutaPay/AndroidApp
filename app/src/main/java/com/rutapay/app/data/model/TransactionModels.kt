package com.rutapay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// La API serializa en camelCase (p. ej. "id", "routeName").

@Serializable
data class TransactionDto(
    @SerialName("id") val id: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("previousBalance") val previousBalance: Double = 0.0,
    @SerialName("currentBalance") val currentBalance: Double = 0.0,
    @SerialName("status") val status: String = "",
    @SerialName("routeName") val routeName: String? = null,
    @SerialName("busUnitId") val busUnitId: String? = null,
    @SerialName("paymentMethod") val paymentMethod: String? = null,
    @SerialName("reference") val reference: String = "",
    @SerialName("createdAt") val createdAt: String = ""
)
