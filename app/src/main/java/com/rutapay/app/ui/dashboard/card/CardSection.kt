package com.rutapay.app.ui.dashboard.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rutapay.app.data.model.TransactionDto
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
import com.rutapay.app.ui.theme.accountBadgeColor
import com.rutapay.app.ui.theme.accountLabel
import com.rutapay.app.ui.theme.fareFor
import com.rutapay.app.ui.theme.formatCardUid
import com.rutapay.app.ui.theme.formatMoney
import com.rutapay.app.ui.theme.formatWebDate
import com.rutapay.app.ui.theme.tariffDescription
import com.rutapay.app.ui.theme.transactionDetail
import com.rutapay.app.ui.theme.transactionTitle

/**
 * Vista Tarjeta al estilo de RutaPayWeb (CardView.vue + CardRechargeView.vue):
 * tarjeta digital oscura, saldo, recargas rápidas, historial con filtros
 * y comprobante "Comprobante Oficial de Movilidad".
 */
@Composable
fun CardSection(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    viewModel: CardViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var filter by remember { mutableStateOf("all") }
    var showRecharge by remember { mutableStateOf(false) }
    var presetForDialog by remember { mutableIntStateOf(100) }
    var receiptTx by remember { mutableStateOf<TransactionDto?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    val card = state.card
    val user = state.user
    val isBlocked = (card?.state ?: user?.cardStatus ?: "Active") == "Blocked"
    val balance = card?.balance ?: user?.cardBalance?.toDoubleOrNull() ?: 0.0
    val holderName = user?.fullName?.ifBlank { null }
        ?: card?.userFullName?.ifBlank { null }
        ?: user?.userName?.ifBlank { null }
        ?: "Usuario RutaPay"
    val accountType = user?.accountType ?: "User"
    val uid = card?.uid ?: user?.cardUID ?: "RPAY-0000-0000"

    val filteredTx = remember(state.transactions, filter) {
        if (filter == "all") state.transactions
        else state.transactions.filter { it.type == filter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WebBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ---- Header: Mi Tarjeta Digital + estado ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, WebCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mi Tarjeta Digital",
                            style = TextStyle(fontFamily = Lexend, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = WebDark)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Gestiona tu saldo, consulta tus movimientos y protege tu tarjeta ante extravíos.",
                            style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = WebMutedText)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(isBlocked = isBlocked, modifier = Modifier.weight(1f))
                    Button(
                        onClick = { viewModel.toggleStatus() },
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBlocked) WebSuccess else Color(0xFFF3F4F6),
                            contentColor = if (isBlocked) Color.White else Color(0xFF374151)
                        ),
                        border = if (isBlocked) null else BorderStroke(1.dp, WebCardBorder)
                    ) {
                        Icon(
                            imageVector = if (isBlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBlocked) "Reactivar Tarjeta" else "Bloquear Tarjeta",
                            style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ---- Tarjeta visual + saldo ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, WebCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Tarjeta física/digital
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = if (isBlocked) BorderStroke(2.dp, WebError) else BorderStroke(1.dp, Color(0xFF374151))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isBlocked) Brush.linearGradient(
                                    listOf(Color(0xFF111827), Color(0xFF450A0A), Color.Black)
                                ) else Brush.linearGradient(
                                    listOf(Color(0xFF111827), WebDark, WebPrimary.copy(alpha = 0.8f))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "RP",
                            style = TextStyle(fontFamily = Lexend, fontSize = 72.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.align(Alignment.BottomEnd)
                        )
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "RutaPay Celaya",
                                        style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WebPrimary)
                                    )
                                    Text(
                                        text = "Transporte Urbano",
                                        style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (accountType == "User" || accountType == "Admin") Color(0xFF1F2937) else accountBadgeColor(accountType)
                                ) {
                                    Text(
                                        text = accountLabel(accountType),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Identificador NFC / Digital",
                                style = TextStyle(fontFamily = Lexend, fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                            )
                            Text(
                                text = formatCardUid(uid),
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(text = "Titular", style = TextStyle(fontFamily = Lexend, fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f)))
                                    Text(text = holderName, style = TextStyle(fontFamily = Lexend, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Estado", style = TextStyle(fontFamily = Lexend, fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f)))
                                    Text(
                                        text = if (isBlocked) "Bloqueada" else "Activa",
                                        style = TextStyle(
                                            fontFamily = Lexend, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                            color = if (isBlocked) Color(0xFFF87171) else Color(0xFF4ADE80)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Saldo
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Saldo Disponible",
                        style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WebMutedText)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$${formatMoney(balance)}",
                            style = TextStyle(fontFamily = Lexend, fontSize = 44.sp, fontWeight = FontWeight.Black, color = Color(0xFF111827))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MXN",
                            style = TextStyle(fontFamily = Lexend, fontSize = 15.sp, color = WebMutedText),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Text(
                        text = "Tarifa por viaje: ${fareFor(accountType)}. Recarga en línea para nunca quedarte sin viajar.",
                        style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebMutedText),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { presetForDialog = 100; showRecharge = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WebPrimary, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recargar Saldo Ahora", style = TextStyle(fontFamily = Lexend, fontSize = 15.sp, fontWeight = FontWeight.Bold))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Recargas rápidas web
                Text(
                    text = "Recargas Rápidas",
                    style = TextStyle(fontFamily = Lexend, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Recargas rápidas en cuadrícula 2x2 (táctil en móvil)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickAmountChip(label = "$50", modifier = Modifier.weight(1f), onClick = { presetForDialog = 50; showRecharge = true })
                        QuickAmountChip(label = "$100", modifier = Modifier.weight(1f), onClick = { presetForDialog = 100; showRecharge = true })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickAmountChip(label = "$200", modifier = Modifier.weight(1f), onClick = { presetForDialog = 200; showRecharge = true })
                        QuickAmountChip(
                            label = "Otro Monto",
                            isPrimary = true,
                            modifier = Modifier.weight(1f),
                            onClick = { presetForDialog = 0; showRecharge = true }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ---- Historial ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, WebCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Historial de Transacciones",
                    style = TextStyle(fontFamily = Lexend, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                )
                Text(
                    text = "Consulta tus recargas, pagos de pasajes y bonificaciones.",
                    style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebMutedText)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Filtros web con scroll horizontal en móvil: Todos / Recargas / Viajes / Recompensas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    FilterChipWeb("Todos", "all", filter) { filter = it }
                    FilterChipWeb("Recargas", "Recharge", filter) { filter = it }
                    FilterChipWeb("Viajes", "TripPayment", filter) { filter = it }
                    FilterChipWeb("Recompensas", "RewardRedemption", filter) { filter = it }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (state.error != null) {
                    Text(
                        text = state.error ?: "",
                        style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebError, fontWeight = FontWeight.Medium)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when {
                    state.isLoading && state.transactions.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = WebPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(WebErrors.TX_LOADING, style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = WebMutedText))
                            }
                        }
                    }
                    filteredTx.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFFF3F4F6)) {
                                Icon(Icons.Default.SwapVert, contentDescription = null, tint = WebMutedText, modifier = Modifier.padding(12.dp).size(28.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(WebErrors.TX_EMPTY_TITLE, style = TextStyle(fontFamily = Lexend, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151)))
                            Text(WebErrors.TX_EMPTY_SUBTITLE, style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, color = WebMutedText))
                        }
                    }
                    else -> {
                        filteredTx.forEach { tx ->
                            TransactionRowWeb(tx = tx, onTicket = { receiptTx = tx })
                        }
                    }
                }
            }
        }

        if (state.lastMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, WebSuccess)
            ) {
                Text(
                    text = state.lastMessage ?: "",
                    modifier = Modifier.padding(12.dp),
                    style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebSuccessDark, fontWeight = FontWeight.Medium)
                )
            }
        }
    }

    // ---- Diálogo de recarga (CardRechargeView.vue) ----
    if (showRecharge) {
        RechargeDialogWeb(
            initialAmount = presetForDialog,
            isProcessing = state.isLoading,
            error = state.error,
            onDismiss = { showRecharge = false; viewModel.clearMessage() },
            onConfirm = { amount, method ->
                viewModel.recharge(amount, method) { showRecharge = false }
            }
        )
    }

    // ---- Comprobante oficial ----
    receiptTx?.let { tx ->
        ReceiptDialogWeb(
            tx = tx,
            holderName = holderName,
            cardUid = formatCardUid(uid),
            accountType = accountType,
            onDismiss = { receiptTx = null }
        )
    }
}

@Composable
private fun StatusBadge(isBlocked: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isBlocked) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
        border = BorderStroke(1.dp, if (isBlocked) WebError else WebSuccess)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (isBlocked) WebError else WebSuccess, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isBlocked) "Bloqueada por Extravío" else "Tarjeta Activa",
                style = TextStyle(
                    fontFamily = Lexend, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = if (isBlocked) WebError else WebSuccessDark
                )
            )
        }
    }
}

@Composable
private fun QuickAmountChip(label: String, modifier: Modifier = Modifier, isPrimary: Boolean = false, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isPrimary) WebPrimary.copy(alpha = 0.08f) else Color(0xFFF9FAFB),
        border = BorderStroke(1.dp, if (isPrimary) WebPrimary.copy(alpha = 0.3f) else WebCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = Lexend,
                    fontSize = if (isPrimary) 14.sp else 19.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isPrimary) WebPrimary else Color(0xFF111827)
                )
            )
            Text(
                text = if (isPrimary) "Personalizado" else "Recargar",
                style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, color = WebMutedText)
            )
        }
    }
}

@Composable
private fun FilterChipWeb(label: String, value: String, selected: String, onSelect: (String) -> Unit) {
    val isSelected = selected == value
    Surface(
        modifier = Modifier.clickable { onSelect(value) },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color.White else Color.Transparent,
        shadowElevation = if (isSelected) 1.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            style = TextStyle(
                fontFamily = Lexend,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color(0xFF111827) else WebMutedText
            )
        )
    }
}

@Composable
private fun TransactionRowWeb(tx: TransactionDto, onTicket: () -> Unit) {
    val iconBg = when (tx.type) {
        "Recharge" -> Color(0xFFDCFCE7)
        "TripPayment" -> Color(0xFFDBEAFE)
        else -> Color(0xFFFEF3C7)
    }
    val iconTint = when (tx.type) {
        "Recharge" -> WebSuccessDark
        "TripPayment" -> WebPrimary
        else -> Color(0xFFB45309)
    }
    val icon = when (tx.type) {
        "Recharge" -> Icons.Default.Add
        "TripPayment" -> Icons.Default.DirectionsBus
        else -> Icons.Default.CardGiftcard
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTicket)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = RoundedCornerShape(20.dp), color = iconBg) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.padding(8.dp).size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transactionTitle(tx.type),
                style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            )
            Text(
                text = transactionDetail(tx.type, tx.routeName, tx.busUnitId, tx.paymentMethod),
                style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, color = WebMutedText)
            )
            Text(
                text = "${tx.reference} • ${formatWebDate(tx.createdAt)}",
                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = WebMutedText)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if (tx.type == "TripPayment") "-" else "+"}$${formatMoney(tx.amount)}",
                style = TextStyle(
                    fontFamily = Lexend, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = if (tx.type == "TripPayment") Color(0xFF1F2937) else WebSuccess
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = onTicket) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = WebPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ticket", style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = WebPrimary))
            }
        }
    }
}

// Sello digital como en la web (hash hexadecimal de id-reference-amount-createdAt).
fun digitalSeal(id: String, reference: String, amount: Double, createdAt: String): String {
    val seed = "$id-$reference-$amount-$createdAt"
    val hex = seed.toByteArray().joinToString("") { "%02x".format(it) }
    return (hex + "E892BF309A41C6D910F4BC9281A7").take(48).uppercase()
}

@Composable
private fun ReceiptDialogWeb(
    tx: TransactionDto,
    holderName: String,
    cardUid: String,
    accountType: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Encabezado oscuro
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WebDark)
                        .padding(20.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd).size(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFF9CA3AF))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("● RutaPay Celaya", style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, fontWeight = FontWeight.Black, color = WebPrimary))
                        Text(
                            text = "Comprobante Oficial de Movilidad",
                            style = TextStyle(fontFamily = Lexend, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Text(
                            text = "Sistema Integrado de Transporte Público",
                            style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, color = Color(0xFFD1D5DB))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.1f), border = BorderStroke(1.dp, WebPrimary.copy(alpha = 0.3f))) {
                            Text(
                                text = "Folio: RPAY-${tx.reference.uppercase().take(10)}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = TextStyle(fontFamily = Lexend, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = WebPrimary)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    ReceiptLine("Titular", holderName)
                    ReceiptLine("Tarjeta NFC", cardUid, mono = true)
                    ReceiptLine(
                        "Modalidad", when (accountType) {
                            "Student" -> "Tarifa Estudiante"
                            "Health" -> "Tarifa Salud"
                            "Adult" -> "Tarifa Adulto Mayor"
                            else -> "Tarifa Ordinaria"
                        }
                    )
                    ReceiptLine("Fecha y Hora", formatWebDate(tx.createdAt))
                    Spacer(modifier = Modifier.height(8.dp))
                    ReceiptLine(
                        "Concepto", when (tx.type) {
                            "TripPayment" -> "Pago de Pasaje Urbano"
                            "Recharge" -> "Recarga de Saldo Digital"
                            else -> "Canje de Recompensa"
                        }
                    )
                    if (tx.type == "TripPayment") {
                        ReceiptLine("Ruta / Autobús", "${tx.routeName ?: "Ruta Urbana"} • Unidad ${tx.busUnitId ?: "01"}")
                        ReceiptLine("Terminal Validador", "VAL-NFC-04 (Activo)", mono = true)
                    }
                    if (tx.type == "Recharge") {
                        ReceiptLine("Canal de Pago", tx.paymentMethod ?: "Tarjeta Digital")
                    }
                    ReceiptLine("Categoría Tarifa", tariffDescription(tx.type, tx.amount, tx.paymentMethod))

                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        border = BorderStroke(1.dp, WebCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Saldo Anterior:", style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, color = WebMutedText))
                                Text("$${formatMoney(tx.previousBalance)} MXN", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = WebMutedText))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (tx.type == "TripPayment") "Total Cobrado:" else "Total Abonado:",
                                    style = TextStyle(fontFamily = Lexend, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
                                )
                                Text(
                                    "${if (tx.type == "TripPayment") "-" else "+"}$${formatMoney(tx.amount)} MXN",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                                        color = if (tx.type == "TripPayment") Color(0xFF111827) else WebSuccess
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Saldo Nuevo en Tarjeta:", style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)))
                                Text("$${formatMoney(tx.currentBalance)} MXN", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Black, color = WebPrimary))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color(0xFF374151), modifier = Modifier.size(28.dp))
                        Text("*${tx.reference}*", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sello Digital de Seguridad",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = WebMutedText)
                        )
                        Text(
                            text = "SHA256:${digitalSeal(tx.id, tx.reference, tx.amount, tx.createdAt)}",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = WebMutedText)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Comprobante electrónico válido como seguro de viaje para usuario registrado en RutaPay Celaya.",
                            style = TextStyle(fontFamily = Lexend, fontSize = 10.sp, color = WebMutedText)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WebPrimary)
                    ) {
                        Text("Cerrar", style = TextStyle(fontFamily = Lexend, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptLine(label: String, value: String, mono: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label + ":", style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebMutedText))
        Text(
            text = value,
            style = TextStyle(
                fontFamily = if (mono) FontFamily.Monospace else Lexend,
                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827)
            )
        )
    }
}

@Composable
private fun RechargeDialogWeb(
    initialAmount: Int,
    isProcessing: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amountText by remember(initialAmount) {
        mutableStateOf(if (initialAmount > 0) initialAmount.toString() else "")
    }
    var selectedMethod by remember { mutableStateOf("Card") }
    var cardNumber by remember { mutableStateOf("4152 3134 5678 9010") }
    var expiry by remember { mutableStateOf("12/28") }
    var cvv by remember { mutableStateOf("789") }

    val presets = listOf(50, 100, 200, 300, 500)
    val amount = amountText.toDoubleOrNull() ?: 0.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Recargar Tarjeta RutaPay",
                            style = TextStyle(fontFamily = Lexend, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        )
                        Text(
                            text = "Selecciona el monto y método de pago simulado.",
                            style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, color = WebMutedText)
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = WebMutedText)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Monto a Recargar (MXN)",
                    style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B5563))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        val selected = amount == preset.toDouble()
                        Surface(
                            modifier = Modifier.weight(1f).clickable { amountText = preset.toString() },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) WebPrimary else Color(0xFFF9FAFB),
                            border = BorderStroke(1.dp, if (selected) WebPrimary else WebCardBorder)
                        ) {
                            Text(
                                text = "$$preset",
                                modifier = Modifier.padding(vertical = 10.dp),
                                style = TextStyle(
                                    fontFamily = Lexend, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else Color(0xFF374151)
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    prefix = { Text("$", color = WebMutedText, fontWeight = FontWeight.Bold) },
                    placeholder = { Text("Ingresa otro monto") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WebPrimary,
                        unfocusedBorderColor = WebCardBorder,
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Método de Pago",
                    style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B5563))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaymentMethodChip("Tarjeta Bancaria", Icons.Default.CreditCard, "Card", selectedMethod, Modifier.weight(1f)) { selectedMethod = it }
                    PaymentMethodChip("Transferencia SPEI", Icons.Default.AccountBalance, "SPEI", selectedMethod, Modifier.weight(1f)) { selectedMethod = it }
                    PaymentMethodChip("OXXO Pay", Icons.Default.Store, "OXXO", selectedMethod, Modifier.weight(1f)) { selectedMethod = it }
                }

                Spacer(modifier = Modifier.height(8.dp))
                when (selectedMethod) {
                    "Card" -> {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            border = BorderStroke(1.dp, WebCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Número de Tarjeta (Simulador)", style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, color = WebMutedText))
                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { cardNumber = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = WebPrimary,
                                        unfocusedBorderColor = WebCardBorder,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                                Text(
                                    "Emisor: ${cardIssuer(cardNumber)}",
                                    style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, color = WebMutedText)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = expiry,
                                        onValueChange = { expiry = it },
                                        label = { Text("Vigencia", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = WebPrimary, unfocusedBorderColor = WebCardBorder,
                                            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                                        )
                                    )
                                    OutlinedTextField(
                                        value = cvv,
                                        onValueChange = { cvv = it.take(4) },
                                        label = { Text("CVV", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = WebPrimary, unfocusedBorderColor = WebCardBorder,
                                            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                    "SPEI" -> {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                SpeiLine("Banco Receptor:", "STP - RutaPay Celaya")
                                SpeiLine("CLABE Interbancaria:", "646180123456789012", mono = true)
                                SpeiLine("Concepto / Referencia:", "RPAY-RECARGA", mono = true)
                                Text(
                                    "Al presionar \"Confirmar Abono SPEI\", se simula la recepción y conciliación bancaria instantánea.",
                                    style = TextStyle(fontFamily = Lexend, fontSize = 10.sp, color = WebMutedText)
                                )
                            }
                        }
                    }
                    else -> {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Referencia de Pago en Tiendas OXXO:", style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, color = Color(0xFF57534E)))
                                Text(
                                    "9342-8812-4521-90",
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                )
                                Text(
                                    "Al confirmar, el sistema acreditará el pago como si hubiera sido registrado en caja de conveniencia.",
                                    style = TextStyle(fontFamily = Lexend, fontSize = 10.sp, color = WebMutedText)
                                )
                            }
                        }
                    }
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebError, fontWeight = FontWeight.Medium))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onConfirm(amount, selectedMethod) },
                    enabled = !isProcessing && amount > 0,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WebPrimary, contentColor = Color.White)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Procesando Recarga...", style = TextStyle(fontFamily = Lexend, fontWeight = FontWeight.Bold))
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirmar Abono de $$amount MXN", style = TextStyle(fontFamily = Lexend, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    selected: String,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    val isSelected = selected == value
    Surface(
        modifier = modifier.clickable { onSelect(value) },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) WebPrimary.copy(alpha = 0.08f) else Color.White,
        border = BorderStroke(1.dp, if (isSelected) WebPrimary else WebCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) WebPrimary else Color(0xFF374151), modifier = Modifier.size(22.dp))
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = Lexend, fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) WebPrimary else Color(0xFF374151)
                )
            )
        }
    }
}

@Composable
private fun SpeiLine(label: String, value: String, mono: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, color = WebMutedText))
        Text(
            value,
            style = TextStyle(
                fontFamily = if (mono) FontFamily.Monospace else Lexend,
                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)
            )
        )
    }
}

fun cardIssuer(number: String): String {
    val cleaned = number.replace(Regex("[\\s-]"), "")
    if (cleaned.startsWith("4")) return "Visa"
    if (cleaned.startsWith("51") || cleaned.startsWith("52") || cleaned.startsWith("53") ||
        cleaned.startsWith("54") || cleaned.startsWith("55")
    ) return "Mastercard"
    if (cleaned.length >= 4) {
        val prefix = cleaned.substring(0, 4).toIntOrNull()
        if (prefix != null && prefix in 2221..2720) return "Mastercard"
    }
    return "Unknown"
}
