package de.isikeren.nex101

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun NexIntroScreen(
    onFinished: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
    val nexOffsetX = remember { Animatable(-screenWidth) }
    var showEntertainment by remember { mutableStateOf(false) }
    var visibleLetters by remember { mutableIntStateOf(0) }

    val word = "Entertainment"

    LaunchedEffect(Unit) {
        nexOffsetX.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 700,
                easing = FastOutSlowInEasing
            )
        )

        delay(150)
        showEntertainment = true

        word.forEachIndexed { index, _ ->
            visibleLetters = index + 1
            delay(45)
        }

        delay(900)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F1115),
                        Color(0xFF050608)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = "NEX",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 6.sp,
                modifier = Modifier.offset(x = nexOffsetX.value.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(visible = showEntertainment) {
                Text(
                    text = word.take(visibleLetters),
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.2.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}