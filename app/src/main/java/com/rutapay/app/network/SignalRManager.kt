package com.rutapay.app.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.microsoft.signalr.Action1
import com.microsoft.signalr.Action2
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Actualización de pago enviada por el hub (`PaymentHub`):
 * `Clients.Group(transactionId).SendAsync("Transaction Status", estado, payload)`.
 */
data class PaymentStatusUpdate(
    @SerializedName("Status") val status: String = "",
    @SerializedName("Message") val message: String? = null,
    @SerializedName("TransactionId") val transactionId: String? = null,
    @SerializedName("Reference") val reference: String? = null,
    @SerializedName("PassengerName") val passengerName: String? = null,
    @SerializedName("TariffType") val tariffType: String? = null,
    @SerializedName("FareDeducted") val fareDeducted: Double? = null,
    @SerializedName("RemainingBalance") val remainingBalance: Double? = null,
    @SerializedName("PointsEarned") val pointsEarned: Int? = null,
    @SerializedName("TotalPoints") val totalPoints: Int? = null,
    @SerializedName("Route") val route: String? = null,
    @SerializedName("BusUnit") val busUnit: String? = null,
    @SerializedName("RequiredFare") val requiredFare: Double? = null,
    @SerializedName("CurrentBalance") val currentBalance: Double? = null
)

/**
 * Conexión en vivo con `/Payments/paymentHub` para confirmación de pagos
 * en tiempo real. El teléfono se une al grupo del `transactionId` generado
 * y recibe el resultado cuando el validador procesa el QR.
 */
class SignalRManager(private val hubUrl: String) {
    private var hubConnection: HubConnection? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()

    private val _statusUpdates = MutableSharedFlow<PaymentStatusUpdate>(extraBufferCapacity = 4)
    val statusUpdates = _statusUpdates.asSharedFlow()

    private val _connectionState =
        MutableStateFlow(HubConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    fun connectAndJoin(transactionId: String) {
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) {
            joinPaymentChannel(transactionId)
            return
        }
        scope.launch {
            try {
                val connection = HubConnectionBuilder.create(hubUrl).build()

                connection.on(
                    "Transaction Status",
                    Action2<String, JsonElement> { state, payload ->
                        Log.d("SignalR", "Transaction Status: $state -> $payload")
                        try {
                            val update = gson.fromJson(payload, PaymentStatusUpdate::class.java)
                            _statusUpdates.tryEmit(
                                update.copy(status = update.status.ifBlank { state })
                            )
                        } catch (e: Exception) {
                            Log.e("SignalR", "No se pudo parsear el payload", e)
                            _statusUpdates.tryEmit(
                                PaymentStatusUpdate(status = state, message = payload?.toString())
                            )
                        }
                    },
                    String::class.java,
                    JsonElement::class.java
                )

                connection.on(
                    "Connection Status",
                    Action1 { message: String ->
                        Log.d("SignalR", "Connection Status: $message")
                    },
                    String::class.java
                )

                connection.onClosed { exception ->
                    Log.e("SignalR", "Connection closed", exception)
                    _connectionState.value = HubConnectionState.DISCONNECTED
                }

                hubConnection = connection
                connection.start().blockingAwait()
                _connectionState.value = HubConnectionState.CONNECTED
                Log.d("SignalR", "Connected to $hubUrl")
                joinPaymentChannel(transactionId)
            } catch (e: Exception) {
                Log.e("SignalR", "Error connecting to $hubUrl", e)
                _connectionState.value = HubConnectionState.DISCONNECTED
            }
        }
    }

    fun joinPaymentChannel(transactionId: String) {
        scope.launch {
            try {
                hubConnection?.send("JoinPaymentChannel", transactionId)
                Log.d("SignalR", "Joined payment channel $transactionId")
            } catch (e: Exception) {
                Log.e("SignalR", "Error joining channel $transactionId", e)
            }
        }
    }

    fun stop() {
        scope.launch {
            try {
                hubConnection?.stop()?.blockingAwait()
            } catch (e: Exception) {
                Log.e("SignalR", "Error stopping connection", e)
            } finally {
                _connectionState.value = HubConnectionState.DISCONNECTED
            }
        }
    }

    fun release() {
        scope.cancel()
    }
}
