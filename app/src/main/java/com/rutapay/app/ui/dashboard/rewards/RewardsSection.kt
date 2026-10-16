package com.rutapay.app.ui.dashboard.rewards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rutapay.app.data.model.RewardDto
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

/**
 * Vista Recompensas al estilo de RutaPayWeb (RewardsView.vue):
 * banner de puntos, cómo ganar puntos, catálogo canjeable y mis cupones.
 */
@Composable
fun RewardsSection(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    viewModel: RewardsViewModel
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    val user = state.user
    val points = user?.points ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WebBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ---- Título ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, WebCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Puntos y Recompensas",
                    style = TextStyle(fontFamily = Lexend, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = WebDark)
                )
                Text(
                    text = "Gana puntos en cada viaje y recarga, y canjéalos por saldo directo y beneficios sustentables.",
                    style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = WebMutedText)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ---- Banner de puntos ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, WebCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Hola, ${user?.userName?.ifBlank { null } ?: "Pasajero"}!",
                    style = TextStyle(fontFamily = Lexend, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = WebDark)
                )
                Text(
                    text = "Miembro RutaPay desde: ${user?.createdOn?.ifBlank { null } ?: "2026"}",
                    style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebMutedText)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = WebPrimary.copy(alpha = 0.06f),
                        border = BorderStroke(1.dp, WebPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Puntos Acumulados",
                                style = TextStyle(fontFamily = Lexend, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WebMutedText)
                            )
                            Text(
                                text = "$points",
                                style = TextStyle(fontFamily = Lexend, fontSize = 26.sp, fontWeight = FontWeight.Black, color = WebPrimary)
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF9FAFB),
                        border = BorderStroke(1.dp, WebCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Viajes Realizados",
                                style = TextStyle(fontFamily = Lexend, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WebMutedText)
                            )
                            Text(
                                text = "Nivel 1",
                                style = TextStyle(fontFamily = Lexend, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFF1F2937))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ---- Cómo ganar puntos ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, WebCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "¿Cómo Ganar Más Puntos?",
                    style = TextStyle(fontFamily = Lexend, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WebDark)
                )
                Spacer(modifier = Modifier.height(12.dp))
                HowToCard(
                    icon = Icons.Default.EmojiEvents,
                    iconBg = WebPrimary.copy(alpha = 0.1f),
                    iconTint = WebPrimary,
                    title = "Viaja en Transporte Público",
                    body = "Gana 1 punto por cada pasaje regular (\$8.00) y 2 puntos con tarifa preferencial (\$5.50)."
                )
                Spacer(modifier = Modifier.height(8.dp))
                HowToCard(
                    icon = Icons.Default.Star,
                    iconBg = Color(0xFFDCFCE7),
                    iconTint = WebSuccessDark,
                    title = "Recargas Periódicas",
                    body = "Recarga tu tarjeta con \$100 o más y mantén saldo suficiente para recibir bonos de lealtad cada quincena."
                )
                Spacer(modifier = Modifier.height(8.dp))
                HowToCard(
                    icon = Icons.Default.CardGiftcard,
                    iconBg = Color(0xFFDBEAFE),
                    iconTint = WebPrimary,
                    title = "Tarifa Preferencial",
                    body = "Acredítate como estudiante, trabajador de la salud o adulto mayor y duplica tus puntos en cada viaje."
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ---- Catálogo ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, WebCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Catálogo de Recompensas",
                    style = TextStyle(fontFamily = Lexend, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WebDark)
                )
                Text(
                    text = "Canjea tus puntos por saldo inmediato para viajar o boletos de rifa.",
                    style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebMutedText)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (state.error != null) {
                    Text(
                        text = state.error ?: "",
                        style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebError, fontWeight = FontWeight.Medium)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when {
                    state.isLoading -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = WebPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(WebErrors.REWARDS_LOADING, style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = WebMutedText))
                            }
                        }
                    }
                    state.rewards.isEmpty() -> {
                        Text(
                            text = "No hay recompensas disponibles por el momento.",
                            style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = WebMutedText)
                        )
                    }
                    else -> {
                        // Cuadrícula de 2 columnas en móvil (la web usa 2 en sm y 4 en lg)
                        state.rewards.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { reward ->
                                    RewardCardWeb(
                                        reward = reward,
                                        canAfford = points >= reward.pointsCost,
                                        isRedeeming = state.isRedeeming,
                                        onRedeem = { viewModel.redeem(reward) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // ---- Mis cupones ----
        if (state.myRewards.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WebCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Mis Cupones y Canjes Recientes",
                        style = TextStyle(fontFamily = Lexend, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WebDark)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    state.myRewards.forEach { mr ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            border = BorderStroke(1.dp, WebCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(mr.title, style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)))
                                Text(
                                    text = "Código: ${mr.code}",
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WebPrimary)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF0FDF4)
                                ) {
                                    Text(
                                        text = if (mr.isUsed) "Aplicado a tu cuenta" else "Listo para usar",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = TextStyle(fontFamily = Lexend, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = WebSuccessDark)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // ---- Modal ¡Canje Completado! ----
    if (state.lastCouponCode != null) {
        Dialog(onDismissRequest = { viewModel.dismissCoupon() }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(shape = RoundedCornerShape(32.dp), color = Color(0xFFDCFCE7)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WebSuccess, modifier = Modifier.padding(14.dp).size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "¡Canje Completado!",
                        style = TextStyle(fontFamily = Lexend, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    )
                    Text(
                        text = state.lastCouponTitle ?: "",
                        style = TextStyle(fontFamily = Lexend, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Tu recompensa ha sido procesada con éxito y aplicada a tu cuenta.",
                        style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebMutedText),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.lastCouponCode ?: "",
                            modifier = Modifier.padding(12.dp),
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WebPrimary),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.dismissCoupon() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WebPrimary)
                    ) {
                        Text("Aceptar", style = TextStyle(fontFamily = Lexend, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
private fun HowToCard(icon: ImageVector, iconBg: Color, iconTint: Color, title: String, body: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        border = BorderStroke(1.dp, WebCardBorder)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Surface(shape = RoundedCornerShape(12.dp), color = iconBg) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.padding(10.dp).size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = TextStyle(fontFamily = Lexend, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)))
                Text(body, style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color(0xFF4B5563)))
            }
        }
    }
}

@Composable
private fun RewardCardWeb(
    reward: RewardDto,
    canAfford: Boolean,
    isRedeeming: Boolean,
    onRedeem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, WebCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = WebPrimary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = if (reward.rewardType == "Balance") "Saldo Inmediato" else "Sorteo Ecológico",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = TextStyle(fontFamily = Lexend, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = WebPrimary)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                reward.title,
                style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                reward.description,
                style = TextStyle(fontFamily = Lexend, fontSize = 10.sp, color = WebMutedText),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${reward.pointsCost} pts",
                style = TextStyle(fontFamily = Lexend, fontSize = 16.sp, fontWeight = FontWeight.Black, color = WebPrimary)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onRedeem,
                enabled = canAfford && !isRedeeming,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) WebPrimary else Color(0xFFF3F4F6),
                    contentColor = if (canAfford) Color.White else WebMutedText
                )
            ) {
                if (isRedeeming) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        if (canAfford) "Canjear" else "Sin puntos",
                        style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
