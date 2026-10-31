package com.rutapay.app.data.repository

import android.util.Log
import com.rutapay.app.data.model.AuthorizePaymentRequestDto
import com.rutapay.app.data.model.AuthorizePaymentResultDto
import com.rutapay.app.data.remote.ApiClient
import com.rutapay.app.ui.theme.WebErrors
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.serialization.decodeFromString

class PaymentRepository(
    private val apiClient: ApiClient
) {
    /**
     * Autoriza el cobro del token de terminal leído por QR o NFC
     * (`POST /api/Payments/authorize`). Requiere la sesión del pasajero.
     *
     * El endpoint responde 200 en éxito y 400 con un body `{ status, message }`
     * en rechazos (saldo insuficiente, tarjeta bloqueada, token inválido). Ambos
     * casos se devuelven como éxito del repositorio para que la UI muestre el
     * motivo real del rechazo.
     */
    suspend fun authorizePayment(
        terminalToken: String,
        paymentMethod: String
    ): Result<AuthorizePaymentResultDto> {
        return try {
            val base = apiClient.baseUrl()
            val response = apiClient.client.post("$base/api/Payments/authorize") {
                setBody(
                    AuthorizePaymentRequestDto(
                        terminalToken = terminalToken.trim(),
                        paymentMethod = paymentMethod
                    )
                )
            }

            val body = response.bodyAsText()
            Log.d("PaymentRepo", "authorize (${response.status.value}) body: ${body.take(2000)}")

            if (response.status == HttpStatusCode.Unauthorized) {
                return Result.failure(Exception(WebErrors.SESSION_EXPIRED))
            }

            val parsed = try {
                apiClient.json.decodeFromString<AuthorizePaymentResultDto>(body)
            } catch (e: Exception) {
                Log.e("PaymentRepo", "authorize: parseo fallido", e)
                null
            }

            if (parsed != null && (response.status.isSuccess() || parsed.status.isNotBlank())) {
                Result.success(parsed)
            } else {
                Result.failure(
                    Exception(
                        body.takeIf { it.isNotBlank() && it.length <= 300 }
                            ?: WebErrors.PAYMENT_ERROR
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("PaymentRepo", "authorize error", e)
            Result.failure(Exception(WebErrors.PAYMENT_NO_CONNECTION))
        }
    }
}
