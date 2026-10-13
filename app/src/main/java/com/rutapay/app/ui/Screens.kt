package com.rutapay.app.ui

import com.rutapay.app.R
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.rutapay.app.ui.theme.Lexend
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.rutapay.app.data.ConfigManager
import com.rutapay.app.ui.theme.*
import com.rutapay.app.util.QrUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun LoginScreen(
    configManager: ConfigManager,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val lightBorder = Color(0xFFC3C3C3)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, lightBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Título
                Text(
                    text = "Inicia sesión para usar RutaPay",
                    style = TextStyle(
                        fontFamily = Lexend,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        lineHeight = 32.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subtítulo
                Text(
                    text = "Accede para usar nuestra plataforma de pagos para el transporte público.",
                    style = TextStyle(
                        fontFamily = Lexend,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextDark,
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Campo Correo Electrónico
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Correo Electrónico", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RutaPayPrimary,
                        unfocusedBorderColor = lightBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Campo Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Contraseña", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RutaPayPrimary,
                        unfocusedBorderColor = lightBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Link Olvidaste tu contraseña
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "¿Olvidaste tu contraseña? ",
                        style = TextStyle(fontFamily = Lexend, fontSize = 14.sp, color = TextDark, fontWeight = FontWeight.Normal)
                    )
                    Text(
                        text = "Restablecer contraseña",
                        style = TextStyle(
                            fontFamily = Lexend,
                            fontSize = 14.sp,
                            color = RutaPayPrimary,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.clickable {
                            // Acción para restablecer contraseña
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Iniciar Sesión
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            delay(300.milliseconds) // Breve indicador de carga visual
                            configManager.setLoggedIn(true)
                            isLoading = false
                            onLoginSuccess() // Salta directamente al Dashboard
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RutaPayPrimary,
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Iniciar Sesión",
                            style = TextStyle(
                                fontFamily = Lexend,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Link No tienes una cuenta
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "¿No tienes una cuenta? ",
                        style = TextStyle(fontFamily = Lexend, fontSize = 14.sp, color = TextDark, fontWeight = FontWeight.Normal)
                    )
                    Text(
                        text = "Regístrate aquí",
                        style = TextStyle(
                            fontFamily = Lexend,
                            fontSize = 14.sp,
                            color = RutaPayPrimary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.clickable {
                            onNavigateToRegister()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    configManager: ConfigManager,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val apiUrl by configManager.apiUrl.collectAsState(initial = "")
    var nombre by remember { mutableStateOf("") }
    var apellidoPaterno by remember { mutableStateOf("") }
    var apellidoMaterno by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val darkBlue = Color(0xFF082B48)
    val buttonBlue = Color(0xFF3898EC)
    val lightBorder = Color(0xFFCBD5E1)
    val darkText = Color(0xFF0F172A)
    val grayDot = Color(0xFF94A3B8)

    // Validaciones de contraseña
    val has8Chars = password.length >= 8
    val hasNumber = password.any { it.isDigit() }
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasSymbol = password.any { !it.isLetterOrDigit() }
    val passwordsMatch = password.isNotEmpty() && password == confirmPassword

    val isFormValid = nombre.isNotBlank() &&
            apellidoPaterno.isNotBlank() &&
            email.isNotBlank() &&
            telefono.isNotBlank() &&
            has8Chars && hasNumber && hasUpper && hasLower && hasSymbol &&
            passwordsMatch && acceptTerms

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, lightBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Título
                Text(
                    text = "Empieza a usar RutaPay",
                    style = TextStyle(
                        fontFamily = Lexend,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkBlue
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtítulo
                Text(
                    text = "Regístrate para comenzar a usar nuestra plataforma de pagos.",
                    style = TextStyle(
                        fontFamily = Lexend,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = darkBlue,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Nombre(s)
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    placeholder = { Text("Nombre(s)", color = grayDot) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = buttonBlue,
                        unfocusedBorderColor = lightBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Apellido Paterno
                OutlinedTextField(
                    value = apellidoPaterno,
                    onValueChange = { apellidoPaterno = it },
                    placeholder = { Text("Apellido Paterno", color = grayDot) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = buttonBlue,
                        unfocusedBorderColor = lightBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Apellido Materno
                OutlinedTextField(
                    value = apellidoMaterno,
                    onValueChange = { apellidoMaterno = it },
                    placeholder = { Text("Apellido Materno", color = grayDot) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = buttonBlue,
                        unfocusedBorderColor = lightBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Correo Electrónico
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Correo Electrónico", color = grayDot) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = buttonBlue,
                        unfocusedBorderColor = lightBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Número de Teléfono
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    placeholder = { Text("Número de Teléfono", color = grayDot) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = buttonBlue,
                        unfocusedBorderColor = lightBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Contraseña", color = grayDot) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = buttonBlue,
                        unfocusedBorderColor = lightBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Requisitos de Contraseña
                Text(
                    text = "La contraseña debe contener:",
                    style = TextStyle(
                        fontFamily = Lexend,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = darkBlue
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                PasswordRequirementItem(text = "Al menos 8 caracteres", fulfilled = has8Chars)
                PasswordRequirementItem(text = "Al menos 1 número (0...9)", fulfilled = hasNumber)
                PasswordRequirementItem(text = "Al menos 1 mayúscula (A...Z)", fulfilled = hasUpper)
                PasswordRequirementItem(text = "Al menos 1 minúscula (a...z)", fulfilled = hasLower)
                PasswordRequirementItem(text = "Al menos 1 símbolo (!...$)", fulfilled = hasSymbol)

                Spacer(modifier = Modifier.height(12.dp))

                // Confirmar Contraseña
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("Confirmar Contraseña", color = grayDot) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = buttonBlue,
                        unfocusedBorderColor = lightBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                PasswordRequirementItem(text = "Las contraseñas coinciden", fulfilled = passwordsMatch)

                Spacer(modifier = Modifier.height(14.dp))

                // Términos y Condiciones
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = acceptTerms,
                        onCheckedChange = { acceptTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = buttonBlue,
                            uncheckedColor = grayDot
                        )
                    )
                    Text(
                        text = "Acepto los ",
                        style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = darkText)
                    )
                    Text(
                        text = "Términos y Condiciones",
                        style = TextStyle(
                            fontFamily = Lexend,
                            fontSize = 12.sp,
                            color = buttonBlue,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.clickable {
                            // Ver términos y condiciones
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Botón Registrarse
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            delay(1000)
                            configManager.setLoggedIn(true)
                            isLoading = false
                            onRegisterSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = isFormValid && !isLoading,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonBlue,
                        disabledContainerColor = buttonBlue.copy(alpha = 0.5f),
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Registrarse",
                            style = TextStyle(
                                fontFamily = Lexend,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enlace a Iniciar Sesión
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿Ya tienes una cuenta? ",
                        style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = darkText)
                    )
                    Text(
                        text = "Inicia sesión aquí",
                        style = TextStyle(
                            fontFamily = Lexend,
                            fontSize = 13.sp,
                            color = buttonBlue,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.clickable {
                            onNavigateToLogin()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordRequirementItem(text: String, fulfilled: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (fulfilled) Color(0xFF22C55E) else Color(0xFF94A3B8),
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = TextStyle(
                fontFamily = Lexend,
                fontSize = 12.sp,
                color = if (fulfilled) Color(0xFF15803D) else Color(0xFF475569),
                fontWeight = if (fulfilled) FontWeight.Medium else FontWeight.Normal
            )
        )
    }
}

@Composable
fun ClientScreen(configManager: ConfigManager) {
    var scanResult by remember { mutableStateOf("Nada escaneado aún") }
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            scanResult = "QR Escaneado: ${result.contents}"
            // Aquí podrías llamar a la API para procesar el pago
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Modo Cliente", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            val options = ScanOptions()
            options.setBeepEnabled(true)
            options.setOrientationLocked(false)
            scanLauncher.launch(options)
        }) {
            Text("Escanear QR")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(scanResult)
    }
}

@Composable
fun SplashScreen(onTimeOut: () -> Unit) {
    // Animación de entrada para el bus y el logo
    val busAlpha = remember { Animatable(0f) }
    val busScale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        // Aparición animada
        launch {
            busAlpha.animateTo(1f, animationSpec = tween(1000))
        }
        launch {
            busScale.animateTo(
                1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        }

        // El tiempo total del Splash
        delay(3000)
        onTimeOut()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Patrón geométrico superior izquierdo ajustado para no solaparse con la barra de estado
        Image(
            painter = painterResource(id = R.drawable.bg_pattern),
            contentDescription = null,
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopStart)
                .graphicsLayer {
                    translationX = -40.dp.toPx()
                    translationY = 0.dp.toPx()
                },
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Bus Icon (más grande)
            Image(
                painter = painterResource(id = R.drawable.ic_bus),
                contentDescription = "Bus Icon",
                modifier = Modifier
                    .size(260.dp)
                    .graphicsLayer {
                        alpha = busAlpha.value
                        scaleX = busScale.value
                        scaleY = busScale.value
                    }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Logo RutaPay (reemplaza las BouncingLetters)
            Image(
                painter = painterResource(id = R.drawable.logo_rutapay),
                contentDescription = "RutaPay Logo",
                modifier = Modifier
                    .width(280.dp)
                    .height(64.dp)
                    .graphicsLayer {
                        alpha = busAlpha.value
                        scaleX = busScale.value
                        scaleY = busScale.value
                    },
                contentScale = ContentScale.Fit
            )
        }
    }
}

// =============================================================
// DASHBOARD PRINCIPAL
// =============================================================
@Composable
fun DashboardScreen(
    configManager: ConfigManager,
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val primaryColor = Color(0xFF78C0E0)   // #78C0E0 Primario
    val secondaryColor = Color(0xFF449DD1) // #449DD1 Secundario
    val tertiaryColor = Color(0xFF052B4A)  // #052B4A Terciario

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF1F5F9),
        bottomBar = {
            ModernAnimatedBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                tertiaryColor = tertiaryColor
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) + slideInHorizontally { width -> if (targetState > initialState) width / 2 else -width / 2 } togetherWith
                            fadeOut(animationSpec = tween(250)) + slideOutHorizontally { width -> if (targetState > initialState) -width / 2 else width / 2 }
                },
                label = "DashboardTabTransition"
            ) { tab ->
                when (tab) {
                    0 -> MapViewSection(primaryColor, secondaryColor, tertiaryColor)
                    1 -> CardViewSection(primaryColor, secondaryColor, tertiaryColor)
                    2 -> QrViewSection(primaryColor, secondaryColor, tertiaryColor)
                    3 -> RewardsViewSection(primaryColor, secondaryColor, tertiaryColor)
                    4 -> ProfileViewSection(primaryColor, secondaryColor, tertiaryColor, configManager, onLogout)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. MAP VIEW SECTION
// -------------------------------------------------------------
@Composable
private fun MapViewSection(primaryColor: Color, secondaryColor: Color, tertiaryColor: Color) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val pulseAnimation = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by pulseAnimation.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Map Canvas Drawing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Terrain background
            drawRect(color = Color(0xFFE2E8F0))

            // Green Areas
            drawCircle(color = Color(0xFFD1FAE5), radius = w * 0.35f, center = Offset(w * 0.2f, h * 0.25f))
            drawCircle(color = Color(0xFFD1FAE5), radius = w * 0.4f, center = Offset(w * 0.85f, h * 0.7f))

            // River / Water
            val riverPath = Path().apply {
                moveTo(0f, h * 0.45f)
                cubicTo(w * 0.3f, h * 0.4f, w * 0.6f, h * 0.55f, w, h * 0.5f)
            }
            drawPath(path = riverPath, color = Color(0xFFBAE6FD), style = Stroke(width = 48f, cap = StrokeCap.Round))

            // Roads
            val mainRoad1 = Path().apply { moveTo(w * 0.1f, 0f); lineTo(w * 0.1f, h) }
            val mainRoad2 = Path().apply { moveTo(0f, h * 0.3f); lineTo(w, h * 0.3f) }
            val mainRoad3 = Path().apply { moveTo(0f, h * 0.7f); lineTo(w, h * 0.7f) }
            val routePath = Path().apply {
                moveTo(w * 0.15f, h * 0.1f)
                cubicTo(w * 0.7f, h * 0.15f, w * 0.3f, h * 0.6f, w * 0.85f, h * 0.85f)
            }

            drawPath(mainRoad1, color = Color.White, style = Stroke(width = 28f))
            drawPath(mainRoad2, color = Color.White, style = Stroke(width = 28f))
            drawPath(mainRoad3, color = Color.White, style = Stroke(width = 28f))

            // Bus Route Line
            drawPath(path = routePath, color = secondaryColor, style = Stroke(width = 16f, cap = StrokeCap.Round))

            // Bus Stops
            val stops = listOf(
                Offset(w * 0.18f, h * 0.12f),
                Offset(w * 0.42f, h * 0.28f),
                Offset(w * 0.45f, h * 0.55f),
                Offset(w * 0.75f, h * 0.78f)
            )
            stops.forEach { stop ->
                drawCircle(color = tertiaryColor, radius = 14f, center = stop)
                drawCircle(color = Color.White, radius = 7f, center = stop)
            }

            // Animated Bus Location
            val busLocation = Offset(w * 0.45f, h * 0.55f)
            drawCircle(color = primaryColor.copy(alpha = pulseAlpha), radius = 45f * pulseScale, center = busLocation)
            drawCircle(color = tertiaryColor, radius = 22f, center = busLocation)
            drawCircle(color = primaryColor, radius = 14f, center = busLocation)
        }

        // Top Collapsible Search Bar Header
        AnimatedContent(
            targetState = isSearchExpanded,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) + expandHorizontally() togetherWith
                        fadeOut(animationSpec = tween(200)) + shrinkHorizontally()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            label = "SearchExpandTransition"
        ) { expanded ->
            if (!expanded) {
                // Small Circular Search Box Button
                Surface(
                    onClick = { isSearchExpanded = true },
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = tertiaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                // Expanded Bordered Search Bar
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.5.dp, secondaryColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = tertiaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Buscar rutas, paradas...",
                                    style = TextStyle(fontFamily = Lexend, fontSize = 14.sp, color = Color(0xFF94A3B8))
                                )
                            }
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                        }
                        IconButton(
                            onClick = {
                                isSearchExpanded = false
                                searchQuery = ""
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Floating Route Detail Card at Bottom
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBus,
                                    contentDescription = null,
                                    tint = tertiaryColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ruta 4B • Centro - Universidad",
                                style = TextStyle(fontFamily = Lexend, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = tertiaryColor)
                            )
                            Text(
                                text = "Llegando a parada: Terminal Central",
                                style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color(0xFF64748B))
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "3 min",
                            style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D)),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tarifa: $12.00 MXN",
                        style = TextStyle(fontFamily = Lexend, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = tertiaryColor)
                    )
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                    ) {
                        Text("Ver Paradas", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. CARD VIEW SECTION (TARJETA)
// -------------------------------------------------------------
@Composable
private fun CardViewSection(primaryColor: Color, secondaryColor: Color, tertiaryColor: Color) {
    var isCardActive by remember { mutableStateOf(true) }
    var currentBalance by remember { mutableDoubleStateOf(250.50) }
    var activeSector by remember { mutableStateOf("Estudiante") }
    var showRechargeDialog by remember { mutableStateOf(false) }

    // Dialog State
    var selectedPresetAmount by remember { mutableIntStateOf(100) }
    var customAmountText by remember { mutableStateOf("100") }
    var selectedPaymentMethod by remember { mutableStateOf("Tarjeta Bancaria") } // Tarjeta Bancaria, Transferencia SPEI, OXXO Pay
    var cardNumber by remember { mutableStateOf("4152 3134 5678 9010") }
    var expiryDate by remember { mutableStateOf("12/28") }
    var cvvCode by remember { mutableStateOf("123") }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var recentActivity by remember {
        mutableStateOf(
            listOf(
                Triple("Ruta 4B - Centro", "- $12.00 MXN", "Hoy, 08:30 AM"),
                Triple("Recarga OXXO", "+ $100.00 MXN", "Ayer, 06:15 PM"),
                Triple("Ruta 12 - Universidad", "- $12.00 MXN", "Ayer, 01:20 PM")
            )
        )
    }

    // Auto dismiss success toast after 3s
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(3000)
            successMessage = null
        }
    }

    // Smooth blur and backdrop overlay animation
    val blurRadius by animateDpAsState(
        targetValue = if (showRechargeDialog) 16.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "blurAnimation"
    )
    val backdropAlpha by animateFloatAsState(
        targetValue = if (showRechargeDialog) 0.5f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "backdropAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Screen Content (Blurred when dialog is active)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mi Tarjeta Digital",
                        style = TextStyle(fontFamily = Lexend, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = tertiaryColor)
                    )
                    Text(
                        text = "Gestiona tu saldo, consulta tus movimientos y protege tu tarjeta",
                        style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = Color(0xFF64748B))
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Digital Credit/Transport Card Preview
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 10.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isCardActive) {
                                Brush.linearGradient(
                                    colors = listOf(tertiaryColor, secondaryColor, primaryColor)
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF334155), Color(0xFF475569), Color(0xFF64748B))
                                )
                            }
                        )
                        .padding(22.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top row of Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "RUTAPAY CELAYA",
                                    style = TextStyle(fontFamily = Lexend, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                )
                                Text(
                                    text = "Transporte Urbano",
                                    style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                )
                            }

                            // Active Account Type Sector Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = activeSector,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                )
                            }
                        }

                        // Middle row: NFC / Digital ID & Card Number
                        Column {
                            Text(
                                text = "IDENTIFICADOR NFC / DIGITAL",
                                style = TextStyle(fontFamily = Lexend, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), letterSpacing = 1.sp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "RPAY 0000 0000",
                                style = TextStyle(fontFamily = Lexend, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 2.sp)
                            )
                        }

                        // Bottom row: Titular & Estado (NO EXPIRATION)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "TITULAR",
                                    style = TextStyle(fontFamily = Lexend, fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                                )
                                Text(
                                    text = "Usuario RutaPay",
                                    style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                )
                            }

                            // Estado Badge
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "ESTADO",
                                    style = TextStyle(fontFamily = Lexend, fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = if (isCardActive) Color(0xFF22C55E) else Color(0xFFEF4444),
                                                shape = CircleShape
                                            )
                                    )
                                    Text(
                                        text = if (isCardActive) "Activa" else "Desactivada",
                                        style = TextStyle(
                                            fontFamily = Lexend,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCardActive) Color(0xFF4ADE80) else Color(0xFFFCA5A5)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Balance & Fare Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SALDO DISPONIBLE",
                                style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            )
                            Text(
                                text = "$${String.format(java.util.Locale.US, "%.2f", currentBalance)} MXN",
                                style = TextStyle(fontFamily = Lexend, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = tertiaryColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tarifa por viaje: $6.50 MXN (Preferencial - Estudiante)",
                        style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: Recargar vs Activar/Desactivar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Recargar Button
                Button(
                    onClick = { showRechargeDialog = true },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recargar", fontWeight = FontWeight.Bold, fontFamily = Lexend)
                }

                // Activar / Desactivar Button
                Button(
                    onClick = { isCardActive = !isCardActive },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCardActive) Color(0xFFDC2626) else Color(0xFF16A34A)
                    )
                ) {
                    Icon(
                        imageVector = if (isCardActive) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCardActive) "Desactivar" else "Activar",
                        fontWeight = FontWeight.Bold,
                        fontFamily = Lexend,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Historial de Transacciones
            Text(
                text = "Historial de Transacciones",
                style = TextStyle(fontFamily = Lexend, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = tertiaryColor)
            )

            Spacer(modifier = Modifier.height(12.dp))

            recentActivity.forEach { (title, amount, date) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(title, fontWeight = FontWeight.SemiBold, color = tertiaryColor, fontFamily = Lexend)
                            Text(date, fontSize = 12.sp, color = Color(0xFF94A3B8), fontFamily = Lexend)
                        }
                        Text(
                            amount,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Lexend,
                            color = if (amount.startsWith("+")) Color(0xFF16A34A) else tertiaryColor
                        )
                    }
                }
            }
        }

        // Animated Scrim Overlay for Blur / Dialog backdrop
        if (backdropAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = backdropAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showRechargeDialog = false
                    }
            )
        }

        // RECHARGE POP-OUT DIALOG WITH SMOOTH ANIMATION
        AnimatedVisibility(
            visible = showRechargeDialog,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(animationSpec = tween(250)) +
                    scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                    slideInVertically(initialOffsetY = { it / 6 }),
            exit = fadeOut(animationSpec = tween(200)) +
                    scaleOut(targetScale = 0.92f) +
                    slideOutVertically(targetOffsetY = { it / 6 })
        ) {
            RechargeDialogContent(
                secondaryColor = secondaryColor,
                tertiaryColor = tertiaryColor,
                selectedPresetAmount = selectedPresetAmount,
                customAmountText = customAmountText,
                selectedPaymentMethod = selectedPaymentMethod,
                cardNumber = cardNumber,
                expiryDate = expiryDate,
                cvvCode = cvvCode,
                onPresetAmountSelected = { amount ->
                    selectedPresetAmount = amount
                    customAmountText = amount.toString()
                },
                onCustomAmountChanged = { newText ->
                    customAmountText = newText
                    newText.toIntOrNull()?.let { selectedPresetAmount = it }
                },
                onPaymentMethodSelected = { method -> selectedPaymentMethod = method },
                onCardNumberChanged = { cardNumber = it },
                onExpiryDateChanged = { expiryDate = it },
                onCvvCodeChanged = { cvvCode = it },
                onClose = { showRechargeDialog = false },
                onConfirm = {
                    val amountToAdd = customAmountText.toDoubleOrNull() ?: selectedPresetAmount.toDouble()
                    currentBalance += amountToAdd
                    val formattedAmount = String.format(java.util.Locale.US, "%.2f", amountToAdd)
                    recentActivity = listOf(
                        Triple("Recarga $selectedPaymentMethod", "+ $$formattedAmount MXN", "Ahora mismo")
                    ) + recentActivity
                    successMessage = "¡Recarga de $$formattedAmount MXN realizada con éxito!"
                    showRechargeDialog = false
                }
            )
        }

        // Success Toast Notification
        AnimatedVisibility(
            visible = successMessage != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF16A34A),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = successMessage ?: "",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = Lexend
                    )
                }
            }
        }
    }
}

@Composable
private fun RechargeDialogContent(
    secondaryColor: Color,
    tertiaryColor: Color,
    selectedPresetAmount: Int,
    customAmountText: String,
    selectedPaymentMethod: String,
    cardNumber: String,
    expiryDate: String,
    cvvCode: String,
    onPresetAmountSelected: (Int) -> Unit,
    onCustomAmountChanged: (String) -> Unit,
    onPaymentMethodSelected: (String) -> Unit,
    onCardNumberChanged: (String) -> Unit,
    onExpiryDateChanged: (String) -> Unit,
    onCvvCodeChanged: (String) -> Unit,
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recargar Tarjeta RutaPay",
                        style = TextStyle(
                            fontFamily = Lexend,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Selecciona el monto y método de pago simulado.",
                        style = TextStyle(
                            fontFamily = Lexend,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. MONTO A RECARGAR (MXN)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MONTO A RECARGAR (MXN)",
                    style = TextStyle(
                        fontFamily = Lexend,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF0F9FF)
                ) {
                    Text(
                        text = "Máx: $1,000 MXN",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = TextStyle(
                            fontFamily = Lexend,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Preset Amount Chips ($50, $100, $200, $500, $1000)
            val presetAmounts = listOf(50, 100, 200, 500, 1000)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetAmounts.forEach { amount ->
                    val isSelected = selectedPresetAmount == amount && customAmountText == amount.toString()
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable { onPresetAmountSelected(amount) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) secondaryColor else Color(0xFFF1F5F9)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$$amount",
                                style = TextStyle(
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF334155)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val parsedAmount = customAmountText.toDoubleOrNull() ?: 0.0
            val isExceedingLimit = parsedAmount > 1000.0

            // Custom Amount Input Field
            OutlinedTextField(
                value = customAmountText,
                onValueChange = onCustomAmountChanged,
                modifier = Modifier.fillMaxWidth(),
                isError = isExceedingLimit,
                prefix = {
                    Text(
                        "$ ",
                        style = TextStyle(fontFamily = Lexend, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isExceedingLimit) Color(0xFFDC2626) else Color(0xFF0F172A))
                    )
                },
                textStyle = TextStyle(fontFamily = Lexend, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isExceedingLimit) Color(0xFFDC2626) else Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isExceedingLimit) Color(0xFFDC2626) else secondaryColor,
                    unfocusedBorderColor = if (isExceedingLimit) Color(0xFFDC2626) else Color(0xFFE2E8F0),
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC)
                )
            )

            if (isExceedingLimit) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "El monto máximo permitido por recarga es de $1,000.00 MXN",
                    style = TextStyle(
                        fontFamily = Lexend,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFDC2626)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. MÉTODO DE PAGO
            Text(
                text = "MÉTODO DE PAGO",
                style = TextStyle(
                    fontFamily = Lexend,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            val paymentMethods = listOf(
                Triple("Tarjeta Bancaria", Icons.Default.CreditCard, "Tarjeta Bancaria"),
                Triple("Transferencia SPEI", Icons.Default.AccountBalance, "Transferencia SPEI"),
                Triple("OXXO Pay", Icons.Default.Storefront, "OXXO Pay")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                paymentMethods.forEach { (name, icon, label) ->
                    val isSelected = selectedPaymentMethod == name
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clickable { onPaymentMethodSelected(name) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFFF0F9FF) else Color.White,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) secondaryColor else Color(0xFFE2E8F0)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = name,
                                tint = if (isSelected) secondaryColor else Color(0xFF64748B),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = TextStyle(
                                    fontFamily = Lexend,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) secondaryColor else Color(0xFF475569)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. PAYMENT METHOD FORM DETAILS CONTAINER
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (selectedPaymentMethod) {
                        "Tarjeta Bancaria" -> {
                            val cleanCardNumber = cardNumber.filter { it.isDigit() }
                            val isCardNumberInvalid = cleanCardNumber.isNotEmpty() && cleanCardNumber.length < 16

                            Text(
                                text = "Número de Tarjeta (Simulador)",
                                style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color(0xFF64748B))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = onCardNumberChanged,
                                modifier = Modifier.fillMaxWidth(),
                                isError = isCardNumberInvalid,
                                textStyle = TextStyle(fontFamily = Lexend, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(10.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isCardNumberInvalid) Color(0xFFEF4444) else secondaryColor,
                                    unfocusedBorderColor = if (isCardNumberInvalid) Color(0xFFEF4444) else Color(0xFFCBD5E1),
                                    errorBorderColor = Color(0xFFEF4444),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )

                            if (isCardNumberInvalid) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Número de tarjeta no válido (se requieren 16 dígitos)",
                                    style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, color = Color(0xFFEF4444))
                                )
                            }

                            val issuer = if (cardNumber.startsWith("4")) "Visa" else if (cardNumber.startsWith("5")) "Mastercard" else "Unknown"
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Emisor: $issuer",
                                style = TextStyle(fontFamily = Lexend, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Vigencia",
                                        style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color(0xFF64748B))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = expiryDate,
                                        onValueChange = onExpiryDateChanged,
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextStyle(fontFamily = Lexend, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A)),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFCBD5E1),
                                            unfocusedBorderColor = Color(0xFFCBD5E1),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "CVV",
                                        style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color(0xFF64748B))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = cvvCode,
                                        onValueChange = onCvvCodeChanged,
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextStyle(fontFamily = Lexend, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A)),
                                        shape = RoundedCornerShape(10.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFCBD5E1),
                                            unfocusedBorderColor = Color(0xFFCBD5E1),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        "Transferencia SPEI" -> {
                            Text(
                                text = "CLABE Interbancaria (Simulador)",
                                style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color(0xFF64748B))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Text(
                                    text = "0121 8001 5492 8371 04",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    style = TextStyle(fontFamily = Lexend, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Realiza tu transferencia desde la banca móvil de tu elección. El saldo se acreditará automáticamente.",
                                style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color(0xFF64748B))
                            )
                        }

                        "OXXO Pay" -> {
                            Text(
                                text = "Código de Referencia OXXO (Simulador)",
                                style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color(0xFF64748B))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Text(
                                    text = "9382-1049-5820-1928",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    style = TextStyle(fontFamily = Lexend, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Muestra este código en la caja de cualquier tienda OXXO para realizar tu abono.",
                                style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color(0xFF64748B))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Button
            val displayAmount = customAmountText.ifEmpty { "0" }
            val cleanCardNumber = cardNumber.filter { it.isDigit() }
            val isCardValid = if (selectedPaymentMethod == "Tarjeta Bancaria") cleanCardNumber.length == 16 else true
            val isFormValid = !isExceedingLimit && parsedAmount > 0.0 && isCardValid

            Button(
                onClick = onConfirm,
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = secondaryColor,
                    disabledContainerColor = Color(0xFFCBD5E1)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isExceedingLimit) "Monto Excede el Límite de $1,000 MXN"
                               else if (!isCardValid) "Número de Tarjeta Incompleto"
                               else "Confirmar Abono de $$displayAmount MXN",
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
    }
}

// -------------------------------------------------------------
// 3. QR VIEW SECTION
// -------------------------------------------------------------
@Composable
private fun QrViewSection(primaryColor: Color, secondaryColor: Color, tertiaryColor: Color) {
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        qrBitmap = withContext(Dispatchers.Default) {
            QrUtils.generateQrCode("RUTAPAY_USER_PASS_9988")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Escáner y Pago QR",
            style = TextStyle(fontFamily = Lexend, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = tertiaryColor)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Acerca este código a la terminal del autobús",
            style = TextStyle(fontFamily = Lexend, fontSize = 14.sp, color = Color(0xFF64748B))
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier
                .size(280.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                qrBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Código QR",
                        modifier = Modifier.size(220.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = tertiaryColor)
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Escanear Código QR del Camión", fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// 4. REWARDS VIEW SECTION (RECOMPENSAS)
// -------------------------------------------------------------
@Composable
private fun RewardsViewSection(primaryColor: Color, secondaryColor: Color, tertiaryColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Recompensas RutaPay",
            style = TextStyle(fontFamily = Lexend, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = tertiaryColor)
        )
        Text(
            text = "Acumula puntos en cada viaje y canjea beneficios",
            style = TextStyle(fontFamily = Lexend, fontSize = 14.sp, color = Color(0xFF64748B))
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = tertiaryColor)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Tus Puntos", color = primaryColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("1,250 Pts", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Text("Nivel Plata • 250 Pts para Oro", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Beneficios Disponibles",
            style = TextStyle(fontFamily = Lexend, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = tertiaryColor)
        )

        Spacer(modifier = Modifier.height(12.dp))

        val rewardsList = listOf(
            Pair("1 Viaje Gratis en Cualquier Ruta", "500 Pts"),
            Pair("50% Descuento en Pase Semanal", "800 Pts"),
            Pair("Pase Mensual Gratis", "2,500 Pts")
        )

        rewardsList.forEach { (title, cost) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.SemiBold, color = tertiaryColor)
                        Text(cost, fontSize = 13.sp, color = secondaryColor, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                    ) {
                        Text("Canjear", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. PROFILE VIEW SECTION (PERFIL)
// -------------------------------------------------------------
@Composable
private fun ProfileViewSection(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    configManager: ConfigManager,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = CircleShape,
            color = secondaryColor.copy(alpha = 0.2f),
            modifier = Modifier.size(90.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = tertiaryColor,
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Usuario RutaPay", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = tertiaryColor)
        Text("usuario@rutapay.com", fontSize = 14.sp, color = Color(0xFF64748B))

        Spacer(modifier = Modifier.height(28.dp))

        val options = listOf(
            Pair("Mis Tarjetas Guardadas", Icons.Default.CreditCard),
            Pair("Historial de Viajes", Icons.Default.DirectionsBus),
            Pair("Notificaciones", Icons.Default.Notifications),
            Pair("Seguridad y Contraseña", Icons.Default.Security),
            Pair("Ayuda y Soporte", Icons.Default.Help)
        )

        options.forEach { (option, icon) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = secondaryColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(option, fontWeight = FontWeight.Medium, color = tertiaryColor, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cerrar Sesión
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .clickable {
                    scope.launch {
                        configManager.setLoggedIn(false)
                        onLogout()
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión", tint = Color(0xFFDC2626))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626), modifier = Modifier.weight(1f))
            }
        }
    }
}

// -------------------------------------------------------------
// MODERN ANIMATED BOTTOM NAVIGATION BAR
// -------------------------------------------------------------
@Composable
private fun ModernAnimatedBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Base Bar Container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left 2 items: Mapa (0) & Tarjeta (1)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavTabButton(
                        item = NavTabItem("Mapa", Icons.Default.Map, Icons.Outlined.Map),
                        isSelected = selectedTab == 0,
                        onClick = { onTabSelected(0) },
                        primaryColor = primaryColor,
                        tertiaryColor = tertiaryColor
                    )
                    NavTabButton(
                        item = NavTabItem("Tarjeta", Icons.Default.CreditCard, Icons.Outlined.CreditCard),
                        isSelected = selectedTab == 1,
                        onClick = { onTabSelected(1) },
                        primaryColor = primaryColor,
                        tertiaryColor = tertiaryColor
                    )
                }

                // Space in middle for Central Elevated QR Button
                Spacer(modifier = Modifier.width(68.dp))

                // Right 2 items: Recompensas (3) & Perfil (4)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavTabButton(
                        item = NavTabItem("Regalo", Icons.Default.CardGiftcard, Icons.Outlined.CardGiftcard),
                        isSelected = selectedTab == 3,
                        onClick = { onTabSelected(3) },
                        primaryColor = primaryColor,
                        tertiaryColor = tertiaryColor
                    )
                    NavTabButton(
                        item = NavTabItem("Perfil", Icons.Default.Person, Icons.Outlined.Person),
                        isSelected = selectedTab == 4,
                        onClick = { onTabSelected(4) },
                        primaryColor = primaryColor,
                        tertiaryColor = tertiaryColor
                    )
                }
            }
        }

        // Central Elevated QR Button (Matching Ref 1 + Ref 2)
        val isQrSelected = selectedTab == 2
        val qrScale by animateFloatAsState(
            targetValue = if (isQrSelected) 1.12f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "qrScale"
        )

        Surface(
            onClick = { onTabSelected(2) },
            shape = CircleShape,
            color = if (isQrSelected) secondaryColor else tertiaryColor,
            shadowElevation = 10.dp,
            border = BorderStroke(3.dp, Color.White),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-6).dp)
                .size(58.dp)
                .graphicsLayer {
                    scaleX = qrScale
                    scaleY = qrScale
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "QR",
                    tint = primaryColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun NavTabButton(
    item: NavTabItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    tertiaryColor: Color
) {
    val offsetY by animateDpAsState(
        targetValue = if (isSelected) (-6).dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "offsetY"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) tertiaryColor else Color(0xFF64748B),
        animationSpec = tween(200),
        label = "iconColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier
                .offset(y = offsetY)
                .size(24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = item.label,
            style = TextStyle(
                fontFamily = Lexend,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) tertiaryColor else Color(0xFF64748B)
            )
        )
    }
}

private data class NavTabItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
