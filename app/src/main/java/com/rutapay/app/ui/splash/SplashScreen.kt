package com.rutapay.app.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rutapay.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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