package com.rutapay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// La API serializa en camelCase (p. ej. "userName"). Los nombres deben
// coincidir exactamente: con ignoreUnknownKeys un desajuste no falla,
// simplemente deja todos los campos en sus defaults (pantallas en blanco).

@Serializable
data class LoginRequestDto(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class RegisterRequestDto(
    @SerialName("fullName") val fullName: String,
    @SerialName("lastNames") val lastNames: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    @SerialName("phoneNumber") val phoneNumber: String
)

@Serializable
data class NewUserDto(
    @SerialName("userName") val userName: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("phoneNumber") val phoneNumber: String = ""
)

@Serializable
data class UserDto(
    @SerialName("userName") val userName: String = "",
    @SerialName("fullName") val fullName: String = "",
    @SerialName("lastNames") val lastNames: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("phoneNumber") val phoneNumber: String = "",
    @SerialName("cardUID") val cardUID: String = "",
    @SerialName("cardBalance") val cardBalance: String = "0.00",
    @SerialName("cardStatus") val cardStatus: String = "Active",
    @SerialName("points") val points: Int = 0,
    @SerialName("createdOn") val createdOn: String = "",
    @SerialName("accountType") val accountType: String = "User",
    @SerialName("isAuthenticated") val isAuthenticated: Boolean = false
)
