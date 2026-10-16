package com.rutapay.app.ui.dashboard.profile

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Savings
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rutapay.app.data.local.SessionManager
import com.rutapay.app.data.ConfigManager
import com.rutapay.app.ui.theme.AccountTiers
import com.rutapay.app.ui.theme.Lexend
import com.rutapay.app.ui.theme.WebBackground
import com.rutapay.app.ui.theme.WebCardBorder
import com.rutapay.app.ui.theme.WebDark
import com.rutapay.app.ui.theme.WebError
import com.rutapay.app.ui.theme.WebLogout
import com.rutapay.app.ui.theme.WebMutedText
import com.rutapay.app.ui.theme.WebPrimary
import com.rutapay.app.ui.theme.WebSuccess
import com.rutapay.app.ui.theme.tierKeyFor

/**
 * Vista Perfil al estilo de RutaPayWeb (AccountView.vue):
 * banner, avatar, datos de cuenta, beneficios del nivel y cerrar sesión.
 */
@Composable
fun ProfileSection(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    configManager: ConfigManager,
    sessionManager: SessionManager,
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showVerifyInfo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }

    val user = state.user
    val tier = AccountTiers[tierKeyFor(user?.accountType)] ?: AccountTiers["base"]!!
    val displayName = user?.userName?.ifBlank { null } ?: "Usuario"
    val initial = displayName.firstOrNull()?.uppercase() ?: "U"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WebBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, WebCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                // Banner con avatar superpuesto al borde inferior
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(128.dp)
                            .background(Color(0xFFD1D5DB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 48.dp),
                        shape = CircleShape,
                        color = WebDark,
                        border = BorderStroke(4.dp, Color.White),
                        shadowElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                style = TextStyle(fontFamily = Lexend, fontSize = 38.sp, fontWeight = FontWeight.Black, color = Color.White)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(top = 56.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (state.isLoading && user == null) {
                        CircularProgressIndicator(color = WebPrimary)
                    } else {
                        Text(
                            text = displayName,
                            style = TextStyle(fontFamily = Lexend, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = WebDark),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Usuario de RutaPay Celaya",
                            style = TextStyle(fontFamily = Lexend, fontSize = 15.sp, color = WebMutedText)
                        )
                        Text(
                            text = "${user?.email ?: ""} • ${user?.phoneNumber ?: ""}",
                            style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebMutedText),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (state.error != null && user == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.error ?: "",
                            style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = WebError, fontWeight = FontWeight.Medium),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Reintentar",
                            style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WebPrimary),
                            modifier = Modifier.clickable { viewModel.load() }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Franja miembro/estado (apilada en móvil)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Miembro desde: ${user?.createdOn?.ifBlank { null } ?: "09/09/2026"}",
                                style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color(0xFF4B5563))
                            )
                            Text(
                                text = "Estado: ● Activa",
                                style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WebSuccess)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Beneficios
                    Text(
                        text = "Mis Beneficios - ${tier.title}",
                        style = TextStyle(fontFamily = Lexend, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WebDark),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    tier.benefits.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { benefit ->
                                BenefitCell(benefit = benefit, modifier = Modifier.weight(1f))
                            }
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (user?.accountType != "Admin") {
                        Text(
                            text = "Aplicar para tarjeta preferencial",
                            style = TextStyle(fontFamily = Lexend, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WebPrimary),
                            modifier = Modifier.clickable { showVerifyInfo = true }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Button(
                        onClick = { viewModel.logout(onLogout) },
                        enabled = !state.isLoggingOut,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WebLogout, contentColor = Color.White)
                    ) {
                        if (state.isLoggingOut) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cerrando sesión...", style = TextStyle(fontFamily = Lexend, fontWeight = FontWeight.Medium))
                        } else {
                            Text("Cerrar Sesión", style = TextStyle(fontFamily = Lexend, fontWeight = FontWeight.Medium))
                        }
                    }
                }
            }
        }
    }

    if (showVerifyInfo) {
        Dialog(onDismissRequest = { showVerifyInfo = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Tarjeta preferencial",
                        style = TextStyle(fontFamily = Lexend, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WebDark)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "La acreditación como estudiante, trabajador de la salud o adulto mayor se realiza con verificación de documentos. Pronto podrás iniciar la solicitud desde la app.",
                        style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = WebMutedText)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showVerifyInfo = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WebPrimary)
                    ) {
                        Text("Entendido", style = TextStyle(fontFamily = Lexend, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
private fun BenefitCell(benefit: String, modifier: Modifier = Modifier) {
    val lower = benefit.lowercase()
    val icon: ImageVector = when {
        lower.contains("tarjeta") -> Icons.Default.CreditCard
        lower.contains("tarifa") -> Icons.Default.Savings
        lower.contains("punto") -> Icons.Default.Star
        lower.contains("recompensa") -> Icons.Default.CardGiftcard
        lower.contains("soporte") -> Icons.Default.HeadsetMic
        else -> Icons.Default.Person
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFF9FAFB),
            border = BorderStroke(1.dp, WebCardBorder),
            shadowElevation = 1.dp
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF374151), modifier = Modifier.padding(14.dp).size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = benefit,
            style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color(0xFF374151)),
            textAlign = TextAlign.Center
        )
    }
}
