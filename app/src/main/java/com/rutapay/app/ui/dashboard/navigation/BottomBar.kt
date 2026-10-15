package com.rutapay.app.ui.dashboard.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutapay.app.ui.theme.Lexend

// Movido verbatim desde ui/Screens.kt (eliminado). Sin cambios visuales.
@Composable
fun ModernAnimatedBottomBar(
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
                        item = NavTabItem("Puntos", Icons.Default.CardGiftcard, Icons.Outlined.CardGiftcard),
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
fun NavTabButton(
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

data class NavTabItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
