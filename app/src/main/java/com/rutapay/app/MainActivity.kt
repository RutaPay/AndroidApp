package com.rutapay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rutapay.app.data.ConfigManager
import com.rutapay.app.data.local.SessionManager
import com.rutapay.app.data.remote.ApiClient
import com.rutapay.app.data.repository.AuthRepository
import com.rutapay.app.data.repository.CardRepository
import com.rutapay.app.data.repository.PaymentRepository
import com.rutapay.app.data.repository.RewardsRepository
import com.rutapay.app.ui.auth.AuthViewModel
import com.rutapay.app.ui.navigation.NavigationDestinations
import com.rutapay.app.ui.theme.RutaPayTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RutaPayTheme {
                val context = LocalContext.current
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()

                val configManager = remember { ConfigManager(context) }
                val sessionManager = remember { SessionManager(context) }
                val apiClient = remember(sessionManager) { ApiClient(sessionManager) }
                val authRepository = remember(apiClient) { AuthRepository(apiClient, sessionManager) }
                val cardRepository = remember(apiClient) { CardRepository(apiClient) }
                val paymentRepository = remember(apiClient) { PaymentRepository(apiClient) }
                val rewardsRepository = remember(apiClient) { RewardsRepository(apiClient) }

                // null = DataStore todavía no ha respondido.
                val isLoggedIn by sessionManager.isLoggedIn
                    .map { it as Boolean? }
                    .collectAsState(initial = null)

                // La cookie JWT se persiste en disco, así que revalidarla en cada
                // arranque basta para conservar la sesión (o detectar que expiró).
                val sessionValidState = produceState<Boolean?>(initialValue = null, isLoggedIn) {
                    value = when (isLoggedIn) {
                        true -> authRepository.validateSession()
                        false -> false
                        null -> null
                    }
                }
                val sessionValid by sessionValidState

                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                // Tiempo mínimo del splash (animación), independiente de la red.
                var splashMinTimeDone by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(2600)
                    splashMinTimeDone = true
                }

                // Navega cuando el splash ya cumplió su tiempo Y la sesión fue
                // revalidada. Antes se usaba el valor inicial (null) del primer
                // callback y siempre mandaba a login.
                LaunchedEffect(splashMinTimeDone, sessionValid, currentRoute) {
                    if (!splashMinTimeDone || currentRoute != NavigationDestinations.SPLASH) return@LaunchedEffect
                    val valid = sessionValid ?: return@LaunchedEffect
                    navController.navigate(
                        if (valid) NavigationDestinations.DASHBOARD else NavigationDestinations.LOGIN
                    ) {
                        popUpTo(NavigationDestinations.SPLASH) { inclusive = true }
                    }
                }

                // Red de seguridad: si la revalidación tarda demasiado, no dejar
                // al usuario atrapado en el splash.
                LaunchedEffect(splashMinTimeDone) {
                    if (!splashMinTimeDone) return@LaunchedEffect
                    delay(8000)
                    if (sessionValidState.value == null &&
                        navController.currentDestination?.route == NavigationDestinations.SPLASH
                    ) {
                        navController.navigate(
                            if (isLoggedIn == true) NavigationDestinations.DASHBOARD else NavigationDestinations.LOGIN
                        ) {
                            popUpTo(NavigationDestinations.SPLASH) { inclusive = true }
                        }
                    }
                }

                // Guardián global de 401: si cualquier llamada limpia la sesión
                // (ver ApiClient), y estamos en el dashboard, volver a login.
                LaunchedEffect(isLoggedIn, currentRoute) {
                    if (isLoggedIn == false && currentRoute == NavigationDestinations.DASHBOARD) {
                        navController.navigate(NavigationDestinations.LOGIN) {
                            popUpTo(NavigationDestinations.DASHBOARD) { inclusive = true }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // El dashboard dibuja su propio contenido de borde a borde
                    // (mapa bajo la status bar y la barra de navegación). Las
                    // demás pantallas siguen usando los insets del Scaffold.
                    val isDashboard = currentRoute == NavigationDestinations.DASHBOARD
                    NavHost(
                        navController = navController,
                        startDestination = NavigationDestinations.SPLASH,
                        modifier = if (isDashboard) Modifier.fillMaxSize() else Modifier.padding(innerPadding)
                    ) {
                        composable(NavigationDestinations.SPLASH) {
                            com.rutapay.app.ui.splash.SplashScreen()
                        }
                        composable(NavigationDestinations.LOGIN) {
                            val authViewModel: AuthViewModel = viewModel(
                                factory = AuthViewModel.Factory(authRepository)
                            )
                            com.rutapay.app.ui.auth.LoginScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = {
                                    navController.navigate(NavigationDestinations.DASHBOARD) {
                                        popUpTo(NavigationDestinations.LOGIN) { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate(NavigationDestinations.REGISTER)
                                }
                            )
                        }
                        composable(NavigationDestinations.REGISTER) {
                            val authViewModel: AuthViewModel = viewModel(
                                factory = AuthViewModel.Factory(authRepository)
                            )
                            com.rutapay.app.ui.auth.RegisterScreen(
                                viewModel = authViewModel,
                                onRegisterSuccess = {
                                    // La API no inicia sesión al registrar (igual que
                                    // la web): se vuelve a login para autenticarse.
                                    navController.navigate(NavigationDestinations.LOGIN) {
                                        popUpTo(NavigationDestinations.REGISTER) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(NavigationDestinations.DASHBOARD) {
                            com.rutapay.app.ui.dashboard.DashboardScreen(
                                configManager = configManager,
                                sessionManager = sessionManager,
                                apiClient = apiClient,
                                authRepository = authRepository,
                                cardRepository = cardRepository,
                                paymentRepository = paymentRepository,
                                rewardsRepository = rewardsRepository,
                                onLogout = {
                                    scope.launch {
                                        authRepository.logout()
                                        configManager.setLoggedIn(false)
                                        navController.navigate(NavigationDestinations.LOGIN) {
                                            popUpTo(NavigationDestinations.DASHBOARD) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
