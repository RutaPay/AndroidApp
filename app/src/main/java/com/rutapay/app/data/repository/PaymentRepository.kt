package com.rutapay.app.data.repository

import android.util.Log
import com.rutapay.app.data.model.GeneratePaymentRequestDto
import com.rutapay.app.data.model.PaymentQrTokenResponseDto
import com.rutapay.app.data.remote.ApiClient
import com.rutapay.app.ui.theme.WebErrors
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class PaymentRepository(
    private val apiClient: ApiClient
) {
    suspend fun generateQrPayment(route: String? = null, busUnitId: String? = null): Result<PaymentQrTokenResponseDto> {
        return try {
            val base = apiClient.baseUrl()
            val response = apiClient.client.post("$base/api/Payments/generate") {
                setBody(GeneratePaymentRequestDto(route = route, busUnitId = busUnitId))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body<PaymentQrTokenResponseDto>())
            } else {
                val msg = response.bodyAsText()
                Log.w("PaymentRepo", "generate failed: $msg")
                Result.failure(Exception(msg.ifBlank { WebErrors.NETWORK_ERROR }))
            }
        } catch (e: Exception) {
            Log.e("PaymentRepo", "generate error", e)
            Result.failure(Exception(WebErrors.RECHARGE_NO_CONNECTION))
        }
    }
}
