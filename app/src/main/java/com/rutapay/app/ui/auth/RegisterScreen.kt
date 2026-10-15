package com.rutapay.app.ui.auth

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutapay.app.ui.theme.Lexend

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var apellidoPaterno by remember { mutableStateOf("") }
    var apellidoMaterno by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }

    val authState by viewModel.state.collectAsState()
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    val darkBlue = Color(0xFF082B48)
    val buttonBlue = Color(0xFF3898EC)
    val lightBorder = Color(0xFFCBD5E1)
    val darkText = Color(0xFF0F172A)
    val grayDot = Color(0xFF94A3B8)

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
                Text(
                    text = "Empieza a usar RutaPay",
                    style = TextStyle(fontFamily = Lexend, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = darkBlue)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Regístrate para comenzar a usar nuestra plataforma de pagos.",
                    style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = darkBlue, lineHeight = 18.sp)
                )
                Spacer(modifier = Modifier.height(20.dp))

                RegisterField(nombre, "Nombre(s)", grayDot, buttonBlue, lightBorder) { nombre = it }
                Spacer(modifier = Modifier.height(10.dp))
                RegisterField(apellidoPaterno, "Apellido Paterno", grayDot, buttonBlue, lightBorder) { apellidoPaterno = it }
                Spacer(modifier = Modifier.height(10.dp))
                RegisterField(apellidoMaterno, "Apellido Materno", grayDot, buttonBlue, lightBorder) { apellidoMaterno = it }
                Spacer(modifier = Modifier.height(10.dp))
                RegisterField(email, "Correo Electrónico", grayDot, buttonBlue, lightBorder, KeyboardType.Email) { email = it }
                Spacer(modifier = Modifier.height(10.dp))
                RegisterField(telefono, "Número de Teléfono", grayDot, buttonBlue, lightBorder, KeyboardType.Phone) { telefono = it }
                Spacer(modifier = Modifier.height(10.dp))
                RegisterField(password, "Contraseña", grayDot, buttonBlue, lightBorder, KeyboardType.Password, true) { password = it }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "La contraseña debe contener:",
                    style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = darkBlue)
                )
                Spacer(modifier = Modifier.height(8.dp))
                PasswordRequirementRow("Al menos 8 caracteres", has8Chars)
                PasswordRequirementRow("Al menos 1 número (0...9)", hasNumber)
                PasswordRequirementRow("Al menos 1 mayúscula (A...Z)", hasUpper)
                PasswordRequirementRow("Al menos 1 minúscula (a...z)", hasLower)
                PasswordRequirementRow("Al menos 1 símbolo (!...$)", hasSymbol)
                Spacer(modifier = Modifier.height(12.dp))
                RegisterField(confirmPassword, "Confirmar Contraseña", grayDot, buttonBlue, lightBorder, KeyboardType.Password, true) { confirmPassword = it }
                Spacer(modifier = Modifier.height(8.dp))
                PasswordRequirementRow("Las contraseñas coinciden", passwordsMatch)
                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(
                        checked = acceptTerms,
                        onCheckedChange = { acceptTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = buttonBlue, uncheckedColor = grayDot)
                    )
                    Text(text = "Acepto los ", style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = darkText))
                    Text(
                        text = "Términos y Condiciones",
                        style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = buttonBlue, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.clickable { }
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Medium)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val lastNames = listOf(apellidoPaterno, apellidoMaterno).filter { it.isNotBlank() }.joinToString(" ")
                        viewModel.register(nombre, lastNames, email, password, telefono, onRegisterSuccess)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = isFormValid && !isLoading,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonBlue,
                        disabledContainerColor = buttonBlue.copy(alpha = 0.5f),
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                    } else {
                        Text(text = "Registrarse", style = TextStyle(fontFamily = Lexend, fontSize = 16.sp, fontWeight = FontWeight.Bold))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "¿Ya tienes una cuenta? ", style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = darkText))
                    Text(
                        text = "Inicia sesión aquí",
                        style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = buttonBlue, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisterField(
    value: String,
    placeholder: String,
    grayDot: Color,
    buttonBlue: Color,
    lightBorder: Color,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(placeholder, color = grayDot) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = buttonBlue,
            unfocusedBorderColor = lightBorder,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
private fun PasswordRequirementRow(text: String, fulfilled: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = if (fulfilled) Color(0xFF22C55E) else Color(0xFF94A3B8), shape = CircleShape)
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
