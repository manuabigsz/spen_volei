package com.spen.placar.ui.scoreboard.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val xRatio: Float,   // posição horizontal (0..1)
    val phase: Float,    // deslocamento no ciclo de queda
    val swaySpeed: Float,
    val swayAmp: Float,
    val spin: Float,
    val w: Float,
    val h: Float,
    val color: Color
)

/**
 * Animação sutil de confete caindo — usada na tela de vitória.
 * As partículas são geradas uma única vez e caem em loop suave.
 */
@Composable
fun Confetti(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    count: Int = 70
) {
    val particles = remember(colors) {
        val rnd = Random(42)
        List(count) {
            Particle(
                xRatio = rnd.nextFloat(),
                phase = rnd.nextFloat(),
                swaySpeed = 1f + rnd.nextFloat() * 2f,
                swayAmp = 12f + rnd.nextFloat() * 26f,
                spin = 0.5f + rnd.nextFloat() * 2f,
                w = 8f + rnd.nextFloat() * 8f,
                h = 14f + rnd.nextFloat() * 12f,
                color = colors[rnd.nextInt(colors.size)]
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fall"
    )

    Canvas(modifier = modifier) {
        particles.forEach { p ->
            val progress = (t + p.phase) % 1f
            val y = progress * (size.height * 1.2f) - size.height * 0.1f
            val sway = sin(progress * p.swaySpeed * 2f * PI.toFloat()) * p.swayAmp
            val x = p.xRatio * size.width + sway
            rotate(degrees = progress * 360f * p.spin, pivot = Offset(x + p.w / 2, y + p.h / 2)) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(x, y),
                    size = Size(p.w, p.h)
                )
            }
        }
    }
}
