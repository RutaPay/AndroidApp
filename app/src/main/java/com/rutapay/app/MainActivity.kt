package com.rutapay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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

                val isLoggedIn by sessionManager.isLoggedIn.collectAsState(initial = false)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = NavigationDestinations.SPLASH,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(NavigationDestinations.SPLASH) {
                            com.rutapay.app.ui.SplashScreen(onTimeOut = {
                                val destination = if (isLoggedIn) NavigationDestinations.DASHBOARD else NavigationDestinations.LOGIN
                                navController.navigate(destination) {
                                    popUpTo(NavigationDestinations.SPLASH) { inclusive = true }
                                }
                            })
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
                                    navController.navigate(NavigationDestinations.DASHBOARD) {
                                        popUpTo(NavigationDestinations.REGISTER) { inclusive = true }
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
                        composable(NavigationDestinations.CLIENT) {
                            com.rutapay.app.ui.ClientScreen(configManager)
                        }
                    }
                }
            }
        }
    }
}
