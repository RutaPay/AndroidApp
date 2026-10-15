package com.rutapay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    @SerialName("Id") val id: String = "",
    @SerialName("Type") val type: String = "",
    @SerialName("Amount") val amount: Double = 0.0,
    @SerialName("PreviousBalance") val previousBalance: Double = 0.0,
    @SerialName("CurrentBalance") val currentBalance: Double = 0.0,
    @SerialName("Status") val status: String = "",
    @SerialName("RouteName") val routeName: String? = null,
    @SerialName("BusUnitId") val busUnitId: String? = null,
    @SerialName("PaymentMethod") val paymentMethod: String? = null,
    @SerialName("Reference") val reference: String = "",
    @SerialName("CreatedAt") val createdAt: String = ""
)
