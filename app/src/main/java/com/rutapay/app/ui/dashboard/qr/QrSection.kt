package com.rutapay.app.ui.dashboard.qr

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.rutapay.app.data.model.ValidatePaymentResultDto
import com.rutapay.app.ui.theme.Lexend
import com.rutapay.app.ui.theme.WebDark
import com.rutapay.app.ui.theme.WebError
import com.rutapay.app.ui.theme.WebPrimary
import com.rutapay.app.ui.theme.WebSecondary
import com.rutapay.app.ui.theme.WebSuccess
import com.rutapay.app.ui.theme.WebSuccessDark
import com.rutapay.app.ui.theme.accountLabel
import com.rutapay.app.ui.theme.formatMoney
import com.rutapay.app.ui.theme.isPreferentialAccount

@Composable
fun QrSection(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    viewModel: QrViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }

    val user = state.user
    val card = state.card

    // Scanner Launcher
    val scannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            viewModel.validateScanned(result.contents)
        }
    }

    // Camera Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionDenied = !granted
        if (granted) {
            scannerLauncher.launch(
                ScanOptions()
                    .setBeepEnabled(true)
                    .setOrientationLocked(false)
                    .setPrompt("Apunta al código QR del autobús")
            )
        }
    }

    fun launchCameraScanner() {
        val granted = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            permissionDenied = false
            scannerLauncher.launch(
                ScanOptions()
                    .setBeepEnabled(true)
                    .setOrientationLocked(false)
                    .setPrompt("Apunta al código QR del autobús")
            )
        } else {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    // Auto-launch camera when entering the screen
    LaunchedEffect(Unit) {
        viewModel.loadData()
        launchCameraScanner()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WebDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP HEADER BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(40.dp))

                Text(
                    text = "Escaneo del QR",
                    style = TextStyle(
                        fontFamily = Lexend,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Icono QR",
                            tint = WebSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. SUB-HEADER INSTRUCTION CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Escanea el código QR del Autobus para pagar:",
                        style = TextStyle(
                            fontFamily = Lexend,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE2E8F0)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. USER ACCOUNT & FARE SESSION BAR
            val registeredName = remember(user) {
                val full = user?.fullName?.trim().orEmpty()
                if (full.isNotBlank()) full
                else user?.userName?.ifBlank { "Usuario RutaPay" } ?: "Usuario RutaPay"
            }
            val accountType = user?.accountType
            val isPreferential = isPreferentialAccount(accountType)
            val fareCostText = if (isPreferential) "$6.50 MXN" else "$13.00 MXN"
            val accountLabelText = accountLabel(accountType)
            val availableBalance = card?.balance ?: user?.cardBalance?.toDoubleOrNull() ?: 0.0

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = WebPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = registeredName,
                                style = TextStyle(
                                    fontFamily = Lexend,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$accountLabelText • Pasaje: $fareCostText",
                                style = TextStyle(
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Saldo Disponible",
                            style = TextStyle(
                                fontFamily = Lexend,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                        Text(
                            text = "$${formatMoney(availableBalance)} MXN",
                            style = TextStyle(
                                fontFamily = Lexend,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // 4. QR VIEWPORT SQUARED BOX WITH CORNER MARKINGS
            QrScanReticleBox(
                modifier = Modifier
                    .size(290.dp)
                    .clickable { launchCameraScanner() },
                borderColor = WebSecondary,
                borderWidth = 3.5.dp,
                cornerLength = 32.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = WebSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { launchCameraScanner() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WebPrimary,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Toca aquí para escanear el código QR",
                                style = TextStyle(
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            if (permissionDenied) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Se requiere permiso de cámara para escanear el código QR del autobús.",
                    style = TextStyle(
                        fontFamily = Lexend,
                        fontSize = 12.sp,
                        color = WebError
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. VALIDATION IN PROGRESS
            if (state.isValidating) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, WebPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = WebSecondary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Procesando pago con el validador...",
                            style = TextStyle(
                                fontFamily = Lexend,
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // 6. SCAN ERROR CARD
            state.scanError?.let { errorMsg ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                    border = BorderStroke(1.dp, WebError),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = WebError,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pago no procesado",
                                style = TextStyle(
                                    fontFamily = Lexend,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WebError
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorMsg,
                            style = TextStyle(
                                fontFamily = Lexend,
                                fontSize = 13.sp,
                                color = Color(0xFFFCA5A5)
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.clearScan()
                                launchCameraScanner()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WebError)
                        ) {
                            Text(
                                text = "Reintentar Escaneo",
                                style = TextStyle(
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // 7. SCAN SUCCESS RESULT CARD
            state.scanResult?.let { result ->
                ScanResultCard(
                    result = result,
                    onClear = {
                        viewModel.clearScan()
                        launchCameraScanner()
                    }
                )
            }
        }
    }
}

@Composable
private fun QrScanReticleBox(
    modifier: Modifier = Modifier,
    borderColor: Color = WebSecondary,
    borderWidth: Dp = 3.5.dp,
    cornerLength: Dp = 32.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1E293B).copy(alpha = 0.85f), RoundedCornerShape(16.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = borderWidth.toPx()
            val len = cornerLength.toPx()
            val w = size.width
            val h = size.height

            // Top-left corner
            drawLine(borderColor, Offset(0f, 0f), Offset(len, 0f), strokeWidth = stroke)
            drawLine(borderColor, Offset(0f, 0f), Offset(0f, len), strokeWidth = stroke)

            // Top-right corner
            drawLine(borderColor, Offset(w - len, 0f), Offset(w, 0f), strokeWidth = stroke)
            drawLine(borderColor, Offset(w, 0f), Offset(w, len), strokeWidth = stroke)

            // Bottom-left corner
            drawLine(borderColor, Offset(0f, h - len), Offset(0f, h), strokeWidth = stroke)
            drawLine(borderColor, Offset(0f, h), Offset(len, h), strokeWidth = stroke)

            // Bottom-right corner
            drawLine(borderColor, Offset(w - len, h), Offset(w, h), strokeWidth = stroke)
            drawLine(borderColor, Offset(w, h - len), Offset(w, h), strokeWidth = stroke)
        }
        content()
    }
}

@Composable
private fun ScanResultCard(
    result: ValidatePaymentResultDto,
    onClear: () -> Unit
) {
    val isValid = result.status.equals("Valid", ignoreCase = true)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isValid) Color(0xFF064E3B) else Color(0xFF450A0A)
        ),
        border = BorderStroke(1.dp, if (isValid) WebSuccess else WebError),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isValid) Color(0xFF34D399) else WebError,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isValid) "¡Pago Aceptado!" else "Pago Rechazado",
                    style = TextStyle(
                        fontFamily = Lexend,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isValid) Color(0xFF34D399) else WebError
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isValid) {
                result.passengerName?.let { QrDetailRow("Pasajero", it) }
                result.fareDeducted?.let { QrDetailRow("Tarifa Cobrada", "$${formatMoney(it)} MXN") }
                result.remainingBalance?.let { QrDetailRow("Saldo Restante", "$${formatMoney(it)} MXN") }
                result.route?.let { QrDetailRow("Ruta", it) }
                result.reference?.let { QrDetailRow("Referencia", it) }
                result.pointsEarned?.let { QrDetailRow("Puntos Ganados", "+$it pts") }
            } else {
                Text(
                    text = result.message ?: "El validador no pudo autorizar el pasaje.",
                    style = TextStyle(
                        fontFamily = Lexend,
                        fontSize = 13.sp,
                        color = Color(0xFFFCA5A5)
                    )
                )
                result.requiredFare?.let { QrDetailRow("Tarifa Requerida", "$${formatMoney(it)} MXN") }
                result.currentBalance?.let { QrDetailRow("Saldo Actual", "$${formatMoney(it)} MXN") }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onClear,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isValid) WebSuccessDark else WebError,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Escanear Otro Código",
                    style = TextStyle(
                        fontFamily = Lexend,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun QrDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = Lexend,
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1)
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = Lexend,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}
