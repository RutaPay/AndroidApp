package com.rutapay.app.ui.dashboard.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rutapay.app.ui.theme.Lexend

private val BarHeight = 72.dp

private val FabSize = 60.dp

private val NotchRadius = FabSize / 2

private val BarTotalHeight = BarHeight + FabSize / 2

private val CenterGap = (NotchRadius * 2) + 24.dp

val BottomBarSpace = BarTotalHeight

val BottomBarHeight = BarHeight

private val UnselectedIcon = Color(0xFF64748B)
private val FabRing = Color(0xFFE9EEF3)

@Composable
fun ModernAnimatedBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavTabItem("Mapa", Icons.Default.Map, Icons.Outlined.Map),
        NavTabItem("Tarjeta", Icons.Default.CreditCard, Icons.Outlined.CreditCard),
        NavTabItem("Puntos", Icons.Default.CardGiftcard, Icons.Outlined.CardGiftcard),
        NavTabItem("Perfil", Icons.Default.Person, Icons.Outlined.Person)
    )

    val navInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BarTotalHeight + navInset),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight + navInset),
            shape = NotchedBarShape(
                cornerRadius = 28.dp,
                notchRadius = NotchRadius,
                roundBottom = false
            ),
            color = Color.White,
            contentColor = tertiaryColor,
            tonalElevation = 0.dp,
            shadowElevation = 14.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = navInset)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTabButton(
                    item = items[0],
                    isSelected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    activeColor = tertiaryColor,
                    modifier = Modifier.weight(1f)
                )
                NavTabButton(
                    item = items[1],
                    isSelected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    activeColor = tertiaryColor,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(CenterGap))

                NavTabButton(
                    item = items[2],
                    isSelected = selectedTab == 3,
                    onClick = { onTabSelected(3) },
                    activeColor = tertiaryColor,
                    modifier = Modifier.weight(1f)
                )
                NavTabButton(
                    item = items[3],
                    isSelected = selectedTab == 4,
                    onClick = { onTabSelected(4) },
                    activeColor = tertiaryColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ---- Botón central de pago ----
        val isQrSelected = selectedTab == 2
        val qrScale by animateFloatAsState(
            targetValue = if (isQrSelected) 1.06f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "qrScale"
        )

        Surface(
            onClick = { onTabSelected(2) },
            shape = CircleShape,
            color = if (isQrSelected) secondaryColor else tertiaryColor,
            contentColor = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 10.dp,
            border = BorderStroke(4.dp, FabRing),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(FabSize)
                .graphicsLayer {
                    scaleX = qrScale
                    scaleY = qrScale
                }
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Pagar",
                    tint = if (isQrSelected) Color.White else primaryColor,
                    modifier = Modifier.size(26.dp)
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
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "iconScale"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            tint = if (isSelected) activeColor else UnselectedIcon,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )

        val labelAlpha by animateFloatAsState(
            targetValue = if (isSelected) 1f else 0f,
            animationSpec = tween(160),
            label = "labelAlpha"
        )
        Text(
            text = item.label,
            style = TextStyle(
                fontFamily = Lexend,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = activeColor,
                letterSpacing = 0.2.sp
            ),
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .graphicsLayer { alpha = labelAlpha }
        )
    }
}

data class NavTabItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Barra con un recorte semicircular en el centro del borde superior. El
 * recorte se dibuja hacia adentro (hacia abajo) para que el botón central
 * parezca encajado en la barra y no simplemente encima de ella.
 *
 * [roundBottom] permite dejar el borde inferior recto cuando la barra está
 * anclada al borde de la pantalla (así no aparecen esquinas recortadas).
 */
private class NotchedBarShape(
    private val cornerRadius: Dp,
    private val notchRadius: Dp,
    private val roundBottom: Boolean = true
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val corner = with(density) { cornerRadius.toPx() }
        val notch = with(density) { notchRadius.toPx() }
        val cx = size.width / 2f

        val path = Path().apply {
            moveTo(corner, 0f)
            lineTo(cx - notch, 0f)
            // Semicírculo inferior: entra por la izquierda y sale por la derecha.
            arcTo(
                rect = Rect(cx - notch, -notch, cx + notch, notch),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(size.width - corner, 0f)
            arcTo(
                rect = Rect(size.width - 2 * corner, 0f, size.width, 2 * corner),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(size.width, size.height - if (roundBottom) corner else 0f)
            if (roundBottom) {
                arcTo(
                    rect = Rect(size.width - 2 * corner, size.height - 2 * corner, size.width, size.height),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
            } else {
                lineTo(size.width, size.height)
            }
            lineTo(if (roundBottom) corner else 0f, size.height)
            if (roundBottom) {
                arcTo(
                    rect = Rect(0f, size.height - 2 * corner, 2 * corner, size.height),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
            }
            lineTo(0f, corner)
            arcTo(
                rect = Rect(0f, 0f, 2 * corner, 2 * corner),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        }

        return Outline.Generic(path)
    }
}
