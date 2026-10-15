package com.rutapay.app.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta tomada de RutaPayWeb (src/assets/main.css @theme).
val WebPrimary = Color(0xFF449DD1)
val WebSecondary = Color(0xFF78C0E0)
val WebDark = Color(0xFF052B4A)
val WebTextLight = Color(0xFF707070)
val WebTextDark = Color(0xFF2E2E2E)

val WebBackground = Color(0xFFF9FAFB)
val WebCardBorder = Color(0xFFE5E7EB)
val WebMutedText = Color(0xFF6B7280)

val WebCardStudent = Color(0xFF22DD6D)
val WebCardHealth = Color(0xFFED145B)
val WebCardAdult = Color(0xFFF5A623)

val WebError = Color(0xFFD93025)
val WebSuccess = Color(0xFF229954)
val WebSuccessDark = Color(0xFF15803D)
val WebLogout = Color(0xFFD93025)

val WebRouteIda = Color(0xFF449DD1)
val WebRouteRegreso = Color(0xFF052B4A)

// Radio de tarjetas web: rounded-3xl (24px) y rounded-2xl (16px).
const val WebCardRadiusLarge = 24
const val WebCardRadiusMedium = 16

/**
 * Mensajes de error/éxito tomados literalmente de RutaPayWeb
 * (toasts de LogInView, RegisterView, CardRechargeView, CardView,
 * RewardsView y authService).
 */
object WebErrors {
    const val LOGIN_BAD_CREDENTIALS =
        "Credenciales incorrectas. Por favor, verifica tu correo electrónico y contraseña."
    const val LOGIN_NO_CONNECTION =
        "No se pudo conectar con el servidor. Por favor, verifica tu conexión a internet."
    const val LOGIN_WELCOME = "Bienvenido de nuevo a RutaPay."

    const val REGISTER_INVALID_FORM =
        "Por favor, completa todos los campos correctamente."
    const val REGISTER_SUCCESS = "Ahora puedes iniciar sesión."
    const val EMAIL_IN_USE = "El correo electrónico ya está en uso."
    const val NAME_INVALID = "Nombre inválido."
    const val REGISTER_ERROR =
        "Error al registrar el usuario. Inténtalo de nuevo más tarde."
    const val NETWORK_ERROR = "No se pudo conectar con el servidor."

    const val RECHARGE_INVALID_AMOUNT = "Por favor ingresa un monto válido mayor a \$0."
    const val RECHARGE_ERROR_FALLBACK = "Error al procesar el abono. Inténtalo más tarde."
    const val RECHARGE_NO_CONNECTION =
        "No se pudo conectar con el servidor de pagos RutaPay."

    const val TOGGLE_ERROR = "No se pudo cambiar el estado de la tarjeta."
    const val TOGGLE_NO_CONNECTION = "Error al conectar con el servidor."

    const val REDEEM_NO_CONNECTION = "No se pudo procesar el canje."

    const val LOGOUT_DONE = "Has cerrado sesión exitosamente."
    const val LOGOUT_NO_CONNECTION = "No se pudo conectar con el servidor."

    const val TX_EMPTY_TITLE = "No se encontraron movimientos"
    const val TX_EMPTY_SUBTITLE =
        "Realiza una recarga o viaja en autobús para ver tus registros aquí."
    const val TX_LOADING = "Cargando movimientos..."
    const val REWARDS_LOADING = "Cargando recompensas disponibles..."
}

data class TierInfo(
    val title: String,
    val benefits: List<String>
)

// Copia de RutaPayWeb/public/data/benefits.json para la vista Perfil.
val AccountTiers: Map<String, TierInfo> = mapOf(
    "base" to TierInfo(
        title = "RutaPay Base",
        benefits = listOf(
            "Tarjeta Base RutaPay",
            "Tarifa Prepago: \$8.00 MXN por viaje",
            "1 punto por cada \$20 que gastes",
            "Acceso a recompensas para miembros",
            "Soporte estándar"
        )
    ),
    "student" to TierInfo(
        title = "RutaPay Estudiantes y Menores de 12 años",
        benefits = listOf(
            "Tarjeta Estudiante RutaPay",
            "Tarifa Prepago Preferencial: \$5.50 MXN por viaje",
            "2 puntos por cada \$20 que gastes",
            "Tarjeta Virtual especial para estudiantes",
            "Acceso a recompensas para miembros",
            "Soporte prioritario"
        )
    ),
    "health" to TierInfo(
        title = "RutaPay Sector Salud",
        benefits = listOf(
            "Tarjeta Sector Salud RutaPay",
            "Tarifa Prepago Preferencial: \$5.50 MXN por viaje",
            "2 puntos por cada \$20 que gastes",
            "Tarjeta Virtual especial para el sector salud",
            "Acceso a recompensas para miembros",
            "Soporte prioritario"
        )
    ),
    "adult" to TierInfo(
        title = "RutaPay Adultos Mayores y Personas con Discapacidad",
        benefits = listOf(
            "Tarjeta Adulto Mayor RutaPay",
            "Tarifa Prepago Preferencial: \$5.50 MXN por viaje",
            "2 puntos por cada \$20 que gastes",
            "Tarjeta Virtual especial para Adultos Mayores y Personas con Discapacidad",
            "Acceso a recompensas para miembros",
            "Soporte prioritario"
        )
    ),
    "admin" to TierInfo(
        title = "RutaPay Administradores",
        benefits = listOf(
            "Tarjeta Administrador RutaPay",
            "Tarifa Prepago: \$8.00 MXN por viaje",
            "1 punto por cada \$20 que gastes",
            "Ocupas recompensas?",
            "Tu eres el soporte"
        )
    )
)

fun tierKeyFor(accountType: String?): String = when (accountType) {
    "Student" -> "student"
    "Health" -> "health"
    "Adult" -> "adult"
    "Admin" -> "admin"
    else -> "base"
}

fun isPreferentialAccount(accountType: String?): Boolean =
    accountType == "Student" || accountType == "Health" || accountType == "Adult"

fun fareFor(accountType: String?): String =
    if (isPreferentialAccount(accountType)) "\$5.50 MXN (Preferencial)" else "\$8.00 MXN"

fun accountLabel(accountType: String?): String = when (accountType) {
    "Student" -> "Estudiante"
    "Health" -> "Salud"
    "Adult" -> "Adultos/Discapacitados"
    "Admin" -> "Admin"
    else -> "Ordinario"
}

fun accountBadgeColor(accountType: String?): Color = when (accountType) {
    "Student" -> WebCardStudent
    "Health" -> WebCardHealth
    "Adult" -> WebCardAdult
    else -> Color(0xFF1F2937)
}

// Formato del UID como en la web: grupos de 4 en mayúsculas.
fun formatCardUid(uid: String): String {
    val cleaned = uid.replace(Regex("[^a-zA-Z0-9]"), "").uppercase()
    if (cleaned.isEmpty()) return uid.ifBlank { "RPAY-0000-0000" }
    return cleaned.chunked(4).joinToString(" ")
}

fun formatMoney(amount: Double): String = "%.2f".format(amount)

// Fecha estilo web (es-MX): "02 ene 2026, 14:30".
fun formatWebDate(iso: String): String {
    if (iso.isBlank()) return "Reciente"
    return try {
        val instant = java.time.Instant.parse(iso)
        val zdt = instant.atZone(java.time.ZoneId.systemDefault())
        val fmt = java.time.format.DateTimeFormatter
            .ofPattern("dd MMM yyyy, HH:mm", java.util.Locale("es", "MX"))
        fmt.format(zdt)
    } catch (_: Exception) {
        try {
            iso.substring(0, 16).replace("T", ", ")
        } catch (_: Exception) {
            "Reciente"
        }
    }
}

fun transactionTitle(type: String): String = when (type) {
    "Recharge" -> "Recarga de Saldo"
    "TripPayment" -> "Cobro de Pasaje"
    else -> "Canje Recompensa"
}

fun transactionDetail(
    type: String,
    routeName: String?,
    busUnitId: String?,
    paymentMethod: String?
): String = when (type) {
    "TripPayment" -> "${routeName ?: "Ruta Urbana"} (${busUnitId ?: "Unidad 01"})"
    "Recharge" -> "Método: ${paymentMethod ?: "Tarjeta"}"
    else -> "Puntos RutaPay"
}

fun tariffDescription(type: String, amount: Double, paymentMethod: String?): String =
    when (type) {
        "TripPayment" -> if (amount <= 5.5)
            "Tarifa Preferencial con Subsidio (Estudiante / Salud / Adulto Mayor)"
        else "Tarifa Ordinaria General"
        "Recharge" -> "Abono Electrónico (${paymentMethod ?: "Tarjeta"})"
        "RewardRedemption" -> "Canje de Puntos de Lealtad RutaPay"
        else -> "Operación General de Transporte"
    }
