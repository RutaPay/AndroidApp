package com.rutapay.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    @SerialName("Email") val email: String,
    @SerialName("Password") val password: String
)

@Serializable
data class RegisterRequestDto(
    @SerialName("FullName") val fullName: String,
    @SerialName("LastNames") val lastNames: String,
    @SerialName("Email") val email: String,
    @SerialName("Password") val password: String,
    @SerialName("PhoneNumber") val phoneNumber: String
)

@Serializable
data class NewUserDto(
    @SerialName("UserName") val userName: String = "",
    @SerialName("Email") val email: String = "",
    @SerialName("PhoneNumber") val phoneNumber: String = ""
)

@Serializable
data class UserDto(
    @SerialName("UserName") val userName: String = "",
    @SerialName("FullName") val fullName: String = "",
    @SerialName("LastNames") val lastNames: String = "",
    @SerialName("Email") val email: String = "",
    @SerialName("PhoneNumber") val phoneNumber: String = "",
    @SerialName("CardUID") val cardUID: String = "",
    @SerialName("CardBalance") val cardBalance: String = "0.00",
    @SerialName("CardStatus") val cardStatus: String = "Active",
    @SerialName("Points") val points: Int = 0,
    @SerialName("CreatedOn") val createdOn: String = "",
    @SerialName("AccountType") val accountType: String = "User",
    @SerialName("IsAuthenticated") val isAuthenticated: Boolean = false
)
