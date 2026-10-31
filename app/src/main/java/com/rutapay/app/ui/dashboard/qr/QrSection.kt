package com.rutapay.app.ui.dashboard.qr

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.rutapay.app.data.model.AuthorizePaymentResultDto
import com.rutapay.app.ui.dashboard.navigation.BottomBarHeight
import com.rutapay.app.ui.theme.Lexend
import com.rutapay.app.ui.theme.WebBackground
import com.rutapay.app.ui.theme.WebCardBorder
import com.rutapay.app.ui.theme.WebDark
import com.rutapay.app.ui.theme.WebError
import com.rutapay.app.ui.theme.WebErrors
import com.rutapay.app.ui.theme.WebMutedText
import com.rutapay.app.ui.theme.WebPrimary
import com.rutapay.app.ui.theme.WebSuccess
import com.rutapay.app.ui.theme.WebSuccessDark
import com.rutapay.app.ui.theme.formatMoney
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class PayMode(val label: String, val icon: ImageVector) {
    SCAN("Escanear QR", Icons.Default.QrCodeScanner),
    NFC("NFC", Icons.Default.Nfc)
}

/**
 * Vista Pagar al estilo RutaPayWeb. La terminal del autobús genera el token de
 * sesión (su QR o su etiqueta NFC); la app del pasajero solo lo lee y autoriza
 * el cobro con su propia sesión (`POST /api/Payments/authorize`).
 */
@Composable
fun QrSection(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    viewModel: QrViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var mode by remember { mutableStateOf(PayMode.SCAN) }

    LaunchedEffect(mode) { viewModel.clear() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WebBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            // El contenido pasa por detrás de la barra blanca; este espacio
            // evita que la última tarjeta quede oculta por ella.
            .navigationBarsPadding()
            .padding(bottom = BottomBarHeight),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pagar pasaje",
            style = TextStyle(fontFamily = Lexend, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = WebDark)
        )
        Text(
            text = "Escanea el QR de la terminal o acerca tu teléfono al lector NFC.",
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
                            entry.icon,
                            contentDescription = null,
                            tint = if (selected) Color.White else WebMutedText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            entry.label,
                            style = TextStyle(
                                fontFamily = Lexend,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) Color.White else WebMutedText
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (mode) {
            PayMode.SCAN -> ScanPane(
                isBusy = state.isAuthorizing,
                onCode = { viewModel.authorize(it, "QR_Terminal") }
            )
            PayMode.NFC -> NfcPane(
                isBusy = state.isAuthorizing,
                onToken = { viewModel.authorize(it, "NFC_Terminal") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        PaymentFeedback(
            state = state,
            onRetry = { viewModel.retry() },
            onClear = { viewModel.clear() }
        )
    }
}

// ------------------------------------------------------------------
// Modo QR: cámara + ZXing
// ------------------------------------------------------------------
@Composable
private fun ScanPane(isBusy: Boolean, onCode: (String) -> Unit) {
    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }

    val scannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contents = result.contents
        if (!contents.isNullOrBlank()) {
            onCode(contents)
        }
    }

    fun launchScanner() {
        scannerLauncher.launch(
            ScanOptions()
                // Pantalla propia en vertical con diseño RutaPay. La
                // orientación la fija el manifest de QrScannerActivity; dejamos
                // que ZXing no la altere para no invertirla en algunos giros.
                .setCaptureActivity(QrScannerActivity::class.java)
                .setOrientationLocked(false)
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setBeepEnabled(true)
                .setPrompt("Apunta al código QR de la terminal")
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionDenied = !granted
        if (granted) launchScanner()
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
            Icon(
                Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = WebPrimary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Escanear código de la terminal",
                style = TextStyle(fontFamily = Lexend, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WebDark)
            )
            Text(
                text = "Apunta la cámara al QR que muestra el validador del autobús.",
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
                        launchScanner()
                    } else {
                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                },
                enabled = !isBusy,
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
}

// ------------------------------------------------------------------
// Modo NFC: lee el token del NDEF de la terminal
// ------------------------------------------------------------------
@Composable
private fun NfcPane(isBusy: Boolean, onToken: (String) -> Unit) {
    val context = LocalContext.current
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    val scope = rememberCoroutineScope()
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var nfcEnabled by remember { mutableStateOf(nfcAdapter?.isEnabled == true) }
    var listening by remember { mutableStateOf(false) }
    var reading by remember { mutableStateOf(false) }
    var nfcError by remember { mutableStateOf<String?>(null) }
    var readingDone by remember { mutableStateOf(false) }

    // Evita lecturas duplicadas mientras la etiqueta sigue pegada al teléfono.
    val readingRef = remember { java.util.concurrent.atomic.AtomicBoolean(false) }
    val doneRef = remember { java.util.concurrent.atomic.AtomicBoolean(false) }

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
                    text = "Este dispositivo no cuenta con hardware NFC. Usa Escanear QR.",
                    style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebMutedText)
                )
            }
        }
        return
    }

    LaunchedEffect(nfcAdapter) {
        nfcEnabled = nfcAdapter.isEnabled
    }

    DisposableEffect(nfcAdapter, nfcEnabled) {
        val activity = context as? Activity
        if (activity != null && nfcEnabled) {
            val callback = NfcAdapter.ReaderCallback { tag ->
                if (doneRef.get() || !readingRef.compareAndSet(false, true)) return@ReaderCallback
                mainHandler.post {
                    reading = true
                    listening = false
                }
                scope.launch(Dispatchers.IO) {
                    val token = readNdefToken(tag)
                    mainHandler.post {
                        reading = false
                        readingDone = true
                        doneRef.set(true)
                        nfcError = if (token.isNullOrBlank()) WebErrors.NFC_EMPTY_TAG else null
                        if (!token.isNullOrBlank()) onToken(token)
                    }
                }
            }

            try {
                nfcAdapter.enableReaderMode(
                    activity,
                    callback,
                    NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V,
                    null
                )
                listening = true
            } catch (_: Exception) {
                listening = false
            }
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
                Icon(
                    Icons.Default.Nfc,
                    contentDescription = null,
                    tint = WebPrimary,
                    modifier = Modifier.padding(14.dp).size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Pago con NFC",
                style = TextStyle(fontFamily = Lexend, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            )

            when {
                !nfcEnabled -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = WebErrors.NFC_DISABLED,
                        style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            runCatching { context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WebPrimary, contentColor = Color.White)
                    ) {
                        Text("Abrir ajustes NFC", style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
                    }
                    TextButton(onClick = { nfcEnabled = nfcAdapter.isEnabled }) {
                        Text("Ya está activado, reintentar", style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebPrimary))
                    }
                }

                reading || isBusy -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Leyendo pase de la terminal…",
                        style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(color = WebPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }

                readingDone -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = nfcError ?: "Pase leído. Revisa el resultado abajo.",
                        style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    )
                    TextButton(onClick = {
                        readingDone = false
                        nfcError = null
                        doneRef.set(false)
                        readingRef.set(false)
                        listening = true
                    }) {
                        Text("Leer otra etiqueta", style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebPrimary))
                    }
                }

                else -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (listening) "Acerca tu teléfono al lector de la terminal…" else "Activando lector…",
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
}

private fun readNdefToken(tag: Tag): String? {
    val ndef = try {
        Ndef.get(tag)
    } catch (_: Exception) {
        null
    } ?: return null

    return try {
        val cached = ndef.cachedNdefMessage
        val message = cached ?: run {
            ndef.connect()
            ndef.ndefMessage
        }
        message?.records?.firstNotNullOfOrNull { decodeRecord(it) }
    } catch (_: Exception) {
        null
    } finally {
        runCatching { ndef.close() }
    }
}

private fun decodeRecord(record: NdefRecord): String? {
    if (record.tnf == NdefRecord.TNF_WELL_KNOWN) {
        when {
            record.type.contentEquals(NdefRecord.RTD_TEXT) -> decodeTextRecord(record.payload)?.let { return it }
            record.type.contentEquals(NdefRecord.RTD_URI) -> decodeUriRecord(record.payload)?.let { return it }
        }
    }

    // Fallback: etiquetas con MIME/external type que traen el JWT como texto
    // plano o dentro de un JSON ({"token":"..."}).
    return try {
        val text = String(record.payload, Charsets.UTF_8)
        jwtRegex.find(text)?.value ?: text.takeIf { it.isNotBlank() && it.length < 4096 }
    } catch (_: Exception) {
        null
    }
}

private val jwtRegex = Regex("[A-Za-z0-9_-]{8,}\\.[A-Za-z0-9_-]{8,}\\.[A-Za-z0-9_-]{8,}")

private fun decodeTextRecord(payload: ByteArray): String? {
    if (payload.size < 3) return null
    val status = payload[0].toInt() and 0xFF
    val utf16 = (status and 0x80) != 0
    val languageLength = status and 0x3F
    val textStart = 1 + languageLength
    if (textStart >= payload.size) return null
    val charset = if (utf16) Charsets.UTF_16 else Charsets.UTF_8
    return try {
        String(payload, textStart, payload.size - textStart, charset).trim()
    } catch (_: Exception) {
        null
    }
}

private val ndefUriPrefixes = arrayOf(
    "", "http://www.", "https://www.", "http://", "https://", "tel:", "mailto:",
    "ftp://anonymous:anonymous@", "ftp://ftp.", "ftps://", "sftp://", "smb://",
    "nfs://", "ftp://", "dav://", "news:", "telnet://", "imap:", "rtsp://",
    "urn:", "pop:", "sip:", "sips:", "tftp:", "btspp://", "btl2cap://",
    "btgoep://", "tcpobex://", "irdaobex://", "file://", "urn:epc:id:",
    "urn:epc:tag:", "urn:epc:pat:", "urn:epc:raw:", "urn:epc:", "urn:nfc:"
)

private fun decodeUriRecord(payload: ByteArray): String? {
    if (payload.size < 2) return null
    val prefixIndex = payload[0].toInt() and 0xFF
    val prefix = ndefUriPrefixes.getOrElse(prefixIndex) { "" }
    return try {
        prefix + String(payload, 1, payload.size - 1, Charsets.UTF_8)
    } catch (_: Exception) {
        null
    }
}

// ------------------------------------------------------------------
// Resultado / errores del cobro
// ------------------------------------------------------------------
@Composable
private fun PaymentFeedback(
    state: QrUiState,
    onRetry: () -> Unit,
    onClear: () -> Unit
) {
    if (state.isAuthorizing) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, WebCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = WebPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Autorizando pago…",
                    style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = WebMutedText)
                )
            }
        }
        return
    }

    state.error?.let { error ->
        FeedbackCard(
            success = false,
            title = "No se pudo completar el pago",
            message = error,
            details = emptyList(),
            primaryLabel = if (state.lastToken != null) "Reintentar" else null,
            onPrimary = onRetry,
            secondaryLabel = "Escanear otro",
            onSecondary = onClear
        )
        return
    }

    state.result?.let { result ->
        val valid = result.status.equals("Valid", ignoreCase = true)
        val title = when {
            valid && result.alreadyProcessed -> "Pasaje ya cobrado"
            valid -> "¡Pasaje pagado!"
            result.status.equals("InsufficientBalance", ignoreCase = true) -> "Saldo insuficiente"
            result.status.equals("CardBlocked", ignoreCase = true) -> "Tarjeta bloqueada"
            else -> "Pago rechazado"
        }

        val details = buildList {
            result.passengerName?.let { add("Pasajero" to it) }
            result.route?.let { add("Ruta" to it) }
            result.terminalCode?.let { add("Terminal" to it) }
            result.busUnit?.let { add("Unidad" to it) }
            result.tariffType?.let { add("Tarifa" to it) }
            result.fareDeducted?.let { add("Cobrado" to "$${formatMoney(it)} MXN") }
            result.remainingBalance?.let { add("Saldo restante" to "$${formatMoney(it)} MXN") }
            result.pointsEarned?.takeIf { it > 0 }?.let { add("Puntos ganados" to "+$it") }
            result.totalPoints?.let { add("Puntos totales" to "$it") }
            result.reference?.let { add("Referencia" to it) }
            result.requiredFare?.let { add("Tarifa requerida" to "$${formatMoney(it)} MXN") }
            result.currentBalance?.let { add("Saldo actual" to "$${formatMoney(it)} MXN") }
        }

        FeedbackCard(
            success = valid,
            title = title,
            message = result.message,
            details = details,
            primaryLabel = "Escanear otro pago",
            onPrimary = onClear,
            secondaryLabel = null,
            onSecondary = onClear
        )
    }
}

@Composable
private fun FeedbackCard(
    success: Boolean,
    title: String,
    message: String?,
    details: List<Pair<String, String>>,
    primaryLabel: String?,
    onPrimary: () -> Unit,
    secondaryLabel: String?,
    onSecondary: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (success) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
        ),
        border = BorderStroke(1.dp, if (success) WebSuccess else WebError),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (success) WebSuccessDark else WebError,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = Lexend,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (success) WebSuccessDark else WebError
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            if (!message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = message,
                    style = TextStyle(
                        fontFamily = Lexend,
                        fontSize = 12.sp,
                        color = if (success) WebSuccessDark else WebError
                    )
                )
            }

            if (details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                details.forEach { (label, value) -> InfoLine(label, value) }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (primaryLabel != null) {
                    Button(
                        onClick = onPrimary,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (success) WebDark else WebPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        if (!success) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            primaryLabel,
                            style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
                if (secondaryLabel != null) {
                    TextButton(onClick = onSecondary) {
                        Text(
                            secondaryLabel,
                            style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebMutedText)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebMutedText)
        )
        Text(
            text = value,
            style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = WebDark)
        )
    }
}
