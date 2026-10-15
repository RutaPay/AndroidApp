package com.rutapay.app.ui.dashboard.qr

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.nfc.NfcAdapter
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode2
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.rutapay.app.data.model.ValidatePaymentResultDto
import com.rutapay.app.network.PaymentStatusUpdate
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

private enum class PayMode(val label: String, val icon: ImageVector) {
    MY_QR("Mi QR", Icons.Default.QrCode2),
    SCAN("Escanear", Icons.Default.PhotoCamera),
    NFC("NFC", Icons.Default.Nfc)
}

/**
 * Vista Pagar con QR/NFC al estilo RutaPayWeb, con 3 modos:
 * - Mi QR: genera el token (`POST /api/Payments/generate`) y escucha la
 *   confirmación en vivo por SignalR ("Transaction Status").
 * - Escanear: lee un QR con la cámara y lo valida (`POST /api/Payments/validate`).
 * - NFC: lee el UID de una tarjeta con el lector NFC (si el equipo lo soporta).
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
    var mode by remember { mutableStateOf(PayMode.MY_QR) }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopLive() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WebBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pagar con QR / NFC",
            style = TextStyle(fontFamily = Lexend, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = WebDark)
        )
        Text(
            text = "Genera tu pasaje digital, escanea un código o acerca tu tarjeta al validador.",
            style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = WebMutedText)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // ---- Selector de modo ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PayMode.entries.forEach { entry ->
                val selected = mode == entry
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { mode = entry },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) WebPrimary else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            entry.icon, contentDescription = null,
                            tint = if (selected) Color.White else WebMutedText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            entry.label,
                            style = TextStyle(
                                fontFamily = Lexend, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                color = if (selected) Color.White else WebMutedText
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (mode) {
            PayMode.MY_QR -> MyQrPane(viewModel)
            PayMode.SCAN -> ScanPane(viewModel)
            PayMode.NFC -> NfcPane()
        }
    }
}

// ------------------------------------------------------------------
// Modo 1: mi QR + confirmación en vivo
// ------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyQrPane(viewModel: QrViewModel) {
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WebCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
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

                // ---- Confirmación en vivo por SignalR ----
                state.liveUpdate?.let { update ->
                    LiveResultCard(update = update, onDismiss = { viewModel.dismissLiveUpdate() })
                } ?: run {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (secondsLeft > 0) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, if (secondsLeft > 0) WebSuccess else WebError)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (secondsLeft > 0 && state.liveConnected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(WebSuccess, RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pasaje activo · esperando al validador…",
                                    style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WebSuccessDark)
                                )
                            } else if (secondsLeft > 0) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = WebSuccessDark)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Conectando canal en vivo…",
                                    style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WebSuccessDark)
                                )
                            } else {
                                Text(
                                    text = "Pasaje vencido",
                                    style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WebError)
                                )
                            }
                        }
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
}

@Composable
private fun LiveResultCard(update: PaymentStatusUpdate, onDismiss: () -> Unit) {
    val isValid = update.status.equals("Valid", ignoreCase = true)
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isValid) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
        ),
        border = BorderStroke(1.dp, if (isValid) WebSuccess else WebError)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isValid) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isValid) WebSuccessDark else WebError,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isValid) "¡Pago aceptado!" else "Pago rechazado",
                    style = TextStyle(
                        fontFamily = Lexend, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                        color = if (isValid) WebSuccessDark else WebError
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (isValid) {
                update.fareDeducted?.let { QrInfoLine("Tarifa cobrada", "$$${formatMoney(it)} MXN") }
                update.remainingBalance?.let { QrInfoLine("Saldo restante", "$$${formatMoney(it)} MXN") }
                update.reference?.let { QrInfoLine("Referencia", it) }
                update.tariffType?.let { QrInfoLine("Tarifa", it) }
                update.totalPoints?.let { QrInfoLine("Puntos totales", "$it") }
            } else {
                Text(
                    text = update.message ?: "El validador rechazó el pago.",
                    style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebError)
                )
                update.requiredFare?.let { QrInfoLine("Tarifa requerida", "$$${formatMoney(it)} MXN") }
                update.currentBalance?.let { QrInfoLine("Saldo actual", "$$${formatMoney(it)} MXN") }
            }
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(onClick = onDismiss) {
                Text("Descartar", style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = WebMutedText))
            }
        }
    }
}

// ------------------------------------------------------------------
// Modo 2: escanear QR y validar contra la API
// ------------------------------------------------------------------
@Composable
private fun ScanPane(viewModel: QrViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }

    val scannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            viewModel.validateScanned(result.contents)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionDenied = !granted
        if (granted) {
            scannerLauncher.launch(
                ScanOptions()
                    .setBeepEnabled(true)
                    .setOrientationLocked(false)
                    .setPrompt("Apunta al código QR de pago")
            )
        }
    }

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
            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = WebPrimary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Escanear código de pago",
                style = TextStyle(fontFamily = Lexend, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WebDark)
            )
            Text(
                text = "Lee el QR de otro pasajero o un código de prueba y valídalo contra la API.",
                style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebMutedText)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        permissionDenied = false
                        scannerLauncher.launch(
                            ScanOptions()
                                .setBeepEnabled(true)
                                .setOrientationLocked(false)
                                .setPrompt("Apunta al código QR de pago")
                        )
                    } else {
                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WebPrimary, contentColor = Color.White)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Abrir Cámara", style = TextStyle(fontFamily = Lexend, fontWeight = FontWeight.Bold))
            }
            if (permissionDenied) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Se necesita permiso de cámara para escanear. Actívalo en Ajustes.",
                    style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebError)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (state.isValidating) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, WebCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(color = WebPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Validando código…", style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = WebMutedText))
            }
        }
    }

    state.scanError?.let { error ->
        Spacer(modifier = Modifier.height(0.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            border = BorderStroke(1.dp, WebError),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = error,
                modifier = Modifier.padding(14.dp),
                style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebError)
            )
        }
    }

    state.scanResult?.let { result ->
        Spacer(modifier = Modifier.height(0.dp))
        ScanResultCard(result = result, onClear = { viewModel.clearScan() })
    }
}

@Composable
private fun ScanResultCard(result: ValidatePaymentResultDto, onClear: () -> Unit) {
    val isValid = result.status.equals("Valid", ignoreCase = true)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isValid) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
        ),
        border = BorderStroke(1.dp, if (isValid) WebSuccess else WebError),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isValid) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isValid) WebSuccessDark else WebError,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isValid) "Código válido" else "Código inválido",
                    style = TextStyle(
                        fontFamily = Lexend, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = if (isValid) WebSuccessDark else WebError
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isValid) {
                result.passengerName?.let { QrInfoLine("Pasajero", it) }
                result.fareDeducted?.let { QrInfoLine("Tarifa cobrada", "$$${formatMoney(it)} MXN") }
                result.remainingBalance?.let { QrInfoLine("Saldo restante", "$$${formatMoney(it)} MXN") }
                result.reference?.let { QrInfoLine("Referencia", it) }
                result.route?.let { QrInfoLine("Ruta", it) }
            } else {
                Text(
                    text = result.message ?: "Token de pago inválido o expirado.",
                    style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebError)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onClear,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WebDark, contentColor = Color.White)
            ) {
                Text("Escanear Otro", style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
            }
        }
    }
}

// ------------------------------------------------------------------
// Modo 3: NFC (si el dispositivo lo soporta)
// ------------------------------------------------------------------
@Composable
private fun NfcPane() {
    val context = LocalContext.current
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    var tagUid by remember { mutableStateOf<String?>(null) }
    var listening by remember { mutableStateOf(false) }

    if (nfcAdapter == null) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, WebCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Nfc, contentDescription = null, tint = WebMutedText, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "NFC no disponible",
                    style = TextStyle(fontFamily = Lexend, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WebDark)
                )
                Text(
                    text = "Este dispositivo no cuenta con hardware NFC. Usa Mi QR o Escanear.",
                    style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebMutedText)
                )
            }
        }
        return
    }

    // Lector NFC activo solo mientras este modo está visible.
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val mainHandler = Handler(Looper.getMainLooper())
        try {
            activity?.let {
                nfcAdapter.enableReaderMode(
                    it,
                    { tag ->
                        val uid = tag.id?.joinToString("") { b -> "%02X".format(b) } ?: ""
                        mainHandler.post {
                            tagUid = uid
                            listening = false
                        }
                    },
                    NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V or
                        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    null
                )
                mainHandler.post { listening = true }
            }
        } catch (e: Exception) {
            mainHandler.post { listening = false }
        }
        onDispose {
            try {
                activity?.let { nfcAdapter.disableReaderMode(it) }
            } catch (_: Exception) {
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WebDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = WebPrimary.copy(alpha = 0.2f)) {
                Icon(Icons.Default.Nfc, contentDescription = null, tint = WebPrimary, modifier = Modifier.padding(14.dp).size(36.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Pago con NFC",
                style = TextStyle(fontFamily = Lexend, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            )
            if (tagUid != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tarjeta detectada",
                    style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))
                )
                Text(
                    text = tagUid ?: "",
                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WebPrimary)
                )
                Text(
                    text = "El validador VAL-NFC-04 asocia este UID con tu tarjeta RutaPay.",
                    style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { tagUid = null; listening = true }) {
                    Text("Leer Otra", style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebPrimary))
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (listening) "Acerca una tarjeta al lector…" else "Activando lector…",
                    style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                )
                if (listening) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(color = WebPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
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
