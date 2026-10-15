package com.rutapay.app.ui.dashboard.qr

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutapay.app.ui.dashboard.map.CelayaRoutes
import com.rutapay.app.ui.theme.Lexend
import com.rutapay.app.ui.theme.WebBackground
import com.rutapay.app.ui.theme.WebCardBorder
import com.rutapay.app.ui.theme.WebDark
import com.rutapay.app.ui.theme.WebError
import com.rutapay.app.ui.theme.WebMutedText
import com.rutapay.app.ui.theme.WebPrimary
import com.rutapay.app.ui.theme.WebSuccess
import com.rutapay.app.ui.theme.WebSuccessDark
import com.rutapay.app.ui.theme.formatMoney
import com.rutapay.app.util.QrUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Vista Pagar con QR/NFC al estilo RutaPayWeb.
 * Genera el token de pago (`POST /api/Payments/generate`), lo muestra como QR
 * con vigencia de 300 s y conserva el identificador NFC de la tarjeta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrSection(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    viewModel: QrViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var selectedRoute by remember { mutableStateOf("Ruta General Celaya") }
    var expanded by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var secondsLeft by remember { mutableIntStateOf(0) }

    val payment = state.payment

    LaunchedEffect(payment?.token) {
        if (payment != null && payment.token.isNotBlank()) {
            qrBitmap = withContext(Dispatchers.Default) {
                try {
                    QrUtils.generateQrCode(payment.token)
                } catch (_: Exception) {
                    null
                }
            }
            secondsLeft = payment.expiresInSeconds
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
        } else {
            qrBitmap = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WebBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ---- Header ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, WebCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Pagar con QR / NFC",
                    style = TextStyle(fontFamily = Lexend, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = WebDark)
                )
                Text(
                    text = "Genera tu pasaje digital y acércalo a la terminal del autobús para acceder al transporte público de manera rápida y sin complicaciones.",
                    style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = WebMutedText)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Selector de ruta (las mismas de la web)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedRoute,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ruta del viaje", fontSize = 12.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WebPrimary,
                            unfocusedBorderColor = WebCardBorder,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ruta General Celaya") },
                            onClick = { selectedRoute = "Ruta General Celaya"; expanded = false }
                        )
                        CelayaRoutes.take(30).forEach { route ->
                            DropdownMenuItem(
                                text = { Text(route, fontSize = 13.sp) },
                                onClick = { selectedRoute = route; expanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.generate(selectedRoute) },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WebPrimary, contentColor = Color.White)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generando pasaje...", style = TextStyle(fontFamily = Lexend, fontWeight = FontWeight.Bold))
                    } else {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generar Código de Pago", style = TextStyle(fontFamily = Lexend, fontSize = 15.sp, fontWeight = FontWeight.Bold))
                    }
                }

                if (state.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error ?: "",
                        style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebError, fontWeight = FontWeight.Medium)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ---- QR generado ----
        if (payment != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WebCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, WebCardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(248.dp).padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val bmp = qrBitmap
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Código QR de pago",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                CircularProgressIndicator(color = WebPrimary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    QrInfoLine("Ruta", payment.route.ifBlank { selectedRoute })
                    QrInfoLine("Pasajero", payment.userFullName.ifBlank { "Usuario RutaPay" })
                    QrInfoLine("Saldo en tarjeta", "$${formatMoney(payment.cardBalance)} MXN")
                    QrInfoLine(
                        "Vigencia",
                        if (secondsLeft > 0) "Válido por ${secondsLeft / 60}:${"%02d".format(secondsLeft % 60)} min"
                        else "Código vencido, genera uno nuevo"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (secondsLeft > 0) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, if (secondsLeft > 0) WebSuccess else WebError)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (secondsLeft > 0) WebSuccessDark else WebError,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (secondsLeft > 0) "Pasaje activo" else "Pasaje vencido",
                                style = TextStyle(
                                    fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    color = if (secondsLeft > 0) WebSuccessDark else WebError
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.generate(selectedRoute) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WebDark, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generar Nuevo Código", style = TextStyle(fontFamily = Lexend, fontWeight = FontWeight.SemiBold, fontSize = 13.sp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ---- NFC ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = WebDark),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = WebPrimary.copy(alpha = 0.2f)) {
                    Icon(Icons.Default.Nfc, contentDescription = null, tint = WebPrimary, modifier = Modifier.padding(10.dp).size(26.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Pago con NFC",
                        style = TextStyle(fontFamily = Lexend, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        text = "Acerca tu tarjeta al validador VAL-NFC-04 del autobús.",
                        style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))
                    )
                }
            }
        }
    }
}

@Composable
private fun QrInfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label + ":", style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebMutedText))
        Text(
            text = value,
            style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = WebDark)
        )
    }
}
