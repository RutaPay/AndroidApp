package com.rutapay.app.ui.dashboard

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rutapay.app.data.ConfigManager
import com.rutapay.app.data.local.SessionManager
import com.rutapay.app.data.remote.ApiClient
import com.rutapay.app.data.repository.AuthRepository
import com.rutapay.app.data.repository.CardRepository
import com.rutapay.app.data.repository.PaymentRepository
import com.rutapay.app.data.repository.RewardsRepository
import com.rutapay.app.ui.dashboard.card.CardSection
import com.rutapay.app.ui.dashboard.card.CardViewModel
import com.rutapay.app.ui.dashboard.map.MapSection
import com.rutapay.app.ui.dashboard.navigation.ModernAnimatedBottomBar
import com.rutapay.app.ui.dashboard.profile.ProfileSection
import com.rutapay.app.ui.dashboard.profile.ProfileViewModel
import com.rutapay.app.ui.dashboard.qr.QrSection
import com.rutapay.app.ui.dashboard.qr.QrViewModel
import com.rutapay.app.ui.dashboard.rewards.RewardsSection
import com.rutapay.app.ui.dashboard.rewards.RewardsViewModel
import com.rutapay.app.ui.theme.WebBackground
import com.rutapay.app.ui.theme.WebDark
import com.rutapay.app.ui.theme.WebPrimary
import com.rutapay.app.ui.theme.WebSecondary
import kotlinx.coroutines.delay

private const val NAV_BAR_AUTOHIDE_MS = 6000L

@Composable
fun DashboardScreen(
    configManager: ConfigManager,
    sessionManager: SessionManager,
    apiClient: ApiClient,
    authRepository: AuthRepository,
    cardRepository: CardRepository,
    paymentRepository: PaymentRepository,
    rewardsRepository: RewardsRepository,
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Paleta web: primary #449DD1, secondary #78C0E0, dark #052B4A.
    val primaryColor = WebSecondary
    val secondaryColor = WebPrimary
    val tertiaryColor = WebDark

    val cardViewModel: CardViewModel = viewModel(
        factory = CardViewModel.Factory(cardRepository, authRepository)
    )
    val qrViewModel: QrViewModel = viewModel(
        factory = QrViewModel.Factory(paymentRepository)
    )
    val rewardsViewModel: RewardsViewModel = viewModel(
        factory = RewardsViewModel.Factory(rewardsRepository, authRepository)
    )
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(authRepository)
    )

    val isMap = selectedTab == 0

    // En el mapa la barra de navegación arranca oculta. El botón "menú" del
    // panel de rutas la muestra otra vez y se auto-oculta a los segundos.
    var navBarVisible by remember { mutableStateOf(false) }
    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) navBarVisible = false
    }
    LaunchedEffect(navBarVisible, selectedTab) {
        if (navBarVisible && selectedTab == 0) {
            delay(NAV_BAR_AUTOHIDE_MS)
            navBarVisible = false
        }
    }

    // Modo inmersivo: sólo en el mapa se ocultan las barras del sistema.
    val immersive = isMap && !navBarVisible
    val context = LocalContext.current
    val view = LocalView.current
    DisposableEffect(immersive) {
        val window = context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        if (controller != null) {
            if (immersive) {
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Sin Scaffold: el contenido ocupa toda la pantalla (el mapa llega hasta
    // las barras del sistema) y la barra inferior se dibuja encima.
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(250)) + slideInHorizontally { width -> if (targetState > initialState) width / 2 else -width / 2 } togetherWith
                    fadeOut(animationSpec = tween(250)) + slideOutHorizontally { width -> if (targetState > initialState) -width / 2 else width / 2 }
            },
            label = "DashboardTabTransition"
        ) { tab ->
            when (tab) {
                0 -> MapSection(
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    tertiaryColor = tertiaryColor,
                    navBarVisible = navBarVisible,
                    onShowNavBar = { navBarVisible = true }
                )
                else -> InsetTabContent {
                    when (tab) {
                        1 -> CardSection(primaryColor, secondaryColor, tertiaryColor, cardViewModel)
                        2 -> QrSection(primaryColor, secondaryColor, tertiaryColor, qrViewModel)
                        3 -> RewardsSection(primaryColor, secondaryColor, tertiaryColor, rewardsViewModel)
                        4 -> ProfileSection(primaryColor, secondaryColor, tertiaryColor, configManager, sessionManager, profileViewModel, onLogout)
                    }
                }
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = !isMap || navBarVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(200)),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(150)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ModernAnimatedBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                tertiaryColor = tertiaryColor
            )
        }
    }
}

/**
 * Envoltura para los tabs que no son el mapa: respeta la status bar y deja que
 * el contenido llegue hasta el borde inferior. La barra se dibuja encima con
 * fondo blanco, así que cada sección reserva su propio espacio al final del
 * scroll (ver [BottomBarHeight]) y nunca se ve una franja del fondo.
 */
@Composable
private fun InsetTabContent(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WebBackground)
            .statusBarsPadding()
    ) {
        content()
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}
