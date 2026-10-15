package com.rutapay.app.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rutapay.app.data.ConfigManager
import com.rutapay.app.data.local.SessionManager
import com.rutapay.app.data.remote.ApiClient
import com.rutapay.app.data.repository.AuthRepository
import com.rutapay.app.data.repository.CardRepository
import com.rutapay.app.data.repository.PaymentRepository
import com.rutapay.app.data.repository.RewardsRepository
import com.rutapay.app.ui.dashboard.navigation.ModernAnimatedBottomBar
import com.rutapay.app.ui.dashboard.card.CardSection
import com.rutapay.app.ui.dashboard.card.CardViewModel
import com.rutapay.app.ui.dashboard.map.MapSection
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
        factory = QrViewModel.Factory(paymentRepository, sessionManager)
    )
    val rewardsViewModel: RewardsViewModel = viewModel(
        factory = RewardsViewModel.Factory(rewardsRepository, authRepository)
    )
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(authRepository)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = WebBackground,
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
                    0 -> MapSection(primaryColor, secondaryColor, tertiaryColor)
                    1 -> CardSection(primaryColor, secondaryColor, tertiaryColor, cardViewModel)
                    2 -> QrSection(primaryColor, secondaryColor, tertiaryColor, qrViewModel)
                    3 -> RewardsSection(primaryColor, secondaryColor, tertiaryColor, rewardsViewModel)
                    4 -> ProfileSection(primaryColor, secondaryColor, tertiaryColor, configManager, sessionManager, profileViewModel, onLogout)
                }
            }
        }
    }
}
