package com.rutapay.app.data.repository

import android.util.Log
import com.rutapay.app.data.model.BalanceChangeDto
import com.rutapay.app.data.model.BalanceChangeResponseDto
import com.rutapay.app.data.model.CardResponseDto
import com.rutapay.app.data.model.ToggleCardResponseDto
import com.rutapay.app.data.model.TransactionDto
import com.rutapay.app.data.remote.ApiClient
import com.rutapay.app.ui.theme.WebErrors
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class CardRepository(
    private val apiClient: ApiClient
) {
    suspend fun getMyCard(): Result<CardResponseDto> {
        return try {
            val base = apiClient.baseUrl()
            val response = apiClient.client.get("$base/api/Cards/my-card")
            if (response.status.isSuccess()) {
                try {
                    Result.success(apiClient.parseAndLog<CardResponseDto>(response, "CardRepo", "GET my-card"))
                } catch (e: Exception) {
                    Log.e("CardRepo", "getMyCard: parseo fallido", e)
                    Result.failure(Exception(WebErrors.NETWORK_ERROR))
                }
            } else {
                val body = response.bodyAsText()
                Log.w("CardRepo", "getMyCard failed ${response.status}: ${body.take(300)}")
                Result.failure(Exception(WebErrors.NETWORK_ERROR))
            }
        } catch (e: Exception) {
            Log.e("CardRepo", "getMyCard error", e)
            Result.failure(Exception(WebErrors.NETWORK_ERROR))
        }
    }

    suspend fun toggleCardStatus(): Result<ToggleCardResponseDto> {
        return try {
            val base = apiClient.baseUrl()
            val response = apiClient.client.post("$base/api/Cards/toggle-status")
            if (response.status.isSuccess()) {
                Result.success(response.body<ToggleCardResponseDto>())
            } else {
                Result.failure(Exception(response.bodyAsText().ifBlank { WebErrors.TOGGLE_ERROR }))
            }
        } catch (e: Exception) {
            Result.failure(Exception(WebErrors.TOGGLE_NO_CONNECTION))
        }
    }

    suspend fun rechargeBalance(amount: Double, paymentMethod: String = "Card"): Result<BalanceChangeResponseDto> {
        if (amount <= 0) return Result.failure(Exception(WebErrors.RECHARGE_INVALID_AMOUNT))
        return try {
            val base = apiClient.baseUrl()
            val response = apiClient.client.post("$base/api/Cards/UpdateBalance") {
                setBody(BalanceChangeDto(balance = amount, oppType = "recharge", paymentMethod = paymentMethod))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body<BalanceChangeResponseDto>())
            } else {
                Result.failure(Exception(response.bodyAsText().ifBlank { WebErrors.RECHARGE_ERROR_FALLBACK }))
            }
        } catch (e: Exception) {
            Result.failure(Exception(WebErrors.RECHARGE_NO_CONNECTION))
        }
    }

    suspend fun getTransactions(): Result<List<TransactionDto>> {
        return try {
            val base = apiClient.baseUrl()
            val response = apiClient.client.get("$base/api/Cards/transactions")
            if (response.status.isSuccess()) {
                try {
                    Result.success(apiClient.parseAndLog<List<TransactionDto>>(response, "CardRepo", "GET transactions"))
                } catch (e: Exception) {
                    Log.e("CardRepo", "getTransactions: parseo fallido", e)
                    Result.failure(Exception(WebErrors.NETWORK_ERROR))
                }
            } else {
                val body = response.bodyAsText()
                Log.w("CardRepo", "getTransactions failed ${response.status}: ${body.take(300)}")
                Result.failure(Exception(WebErrors.NETWORK_ERROR))
            }
        } catch (e: Exception) {
            Result.failure(Exception(WebErrors.NETWORK_ERROR))
        }
    }
}
