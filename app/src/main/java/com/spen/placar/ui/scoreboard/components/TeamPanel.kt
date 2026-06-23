package com.spen.placar.ui.scoreboard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spen.placar.ui.theme.TeamNameStyle

/**
 * Card de uma equipe — premium minimalista (Apple/Stripe).
 *
 * Conteúdo vertical: nome no topo, placar gigante no centro, sets logo abaixo
 * e botões −1 / +1 na base. O card inteiro soma ponto ao toque (1 toque).
 *
 * @param highlighted realça o card por alguns instantes (feedback da S Pen).
 * @param setPoints pontos desta equipe nos sets encerrados (placar discreto).
 */
@Composable
fun TeamPanel(
    name: String,
    points: Int,
    sets: Int,
    setPoints: List<Int>,
    accent: Color,
    highlighted: Boolean,
    onAddPoint: () -> Unit,
    onRemovePoint: () -> Unit,
    onEditName: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }

    // Realce do card ao receber comando da S Pen.
    val borderColor by animateColorAsState(
        targetValue = if (highlighted) accent else MaterialTheme.colorScheme.outline,
        animationSpec = tween(250),
        label = "border"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (highlighted) 2.5.dp else 1.dp,
        animationSpec = tween(250),
        label = "borderW"
    )

    // Pulse ao vencer um set.
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(sets) {
        if (sets > 0) {
            pulse.animateTo(1.03f, tween(140))
            pulse.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    Box(
        modifier = modifier
            .scale(pulse.value)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(16.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onAddPoint)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 18.dp)
        ) {
            val scoreSp = with(LocalDensity.current) {
                minOf(maxWidth * 0.46f, maxHeight * 0.34f).coerceIn(64.dp, 132.dp).toSp()
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Nome
                Text(
                    text = name.uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = TeamNameStyle,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onEditName)
                        .padding(vertical = 4.dp)
                )

                // Placar gigante + sets
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = points,
                        transitionSpec = {
                            (scaleIn(tween(180), initialScale = 0.6f) + fadeIn(tween(180)))
                                .togetherWith(scaleOut(tween(180), targetScale = 1.2f) + fadeOut(tween(180)))
                                .using(SizeTransform(clip = false))
                        },
                        label = "score"
                    ) { value ->
                        Text(
                            text = value.toString(),
                            color = accent,
                            fontWeight = FontWeight.Black,
                            fontSize = scoreSp,
                            textAlign = TextAlign.Center
                        )
                    }
                    SetsPips(sets = sets, color = accent)
                    if (setPoints.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            setPoints.forEach { p ->
                                Text(
                                    text = p.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Botões −1 / +1
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleControl(
                        onClick = onRemovePoint,
                        container = MaterialTheme.colorScheme.surfaceVariant,
                        content = MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 48.dp,
                        icon = { tint ->
                            Icon(Icons.Filled.Remove, contentDescription = "Remover ponto de $name", tint = tint)
                        }
                    )
                    CircleControl(
                        onClick = onAddPoint,
                        container = accent,
                        content = Color.White,
                        size = 72.dp,
                        icon = { tint ->
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "Adicionar ponto para $name",
                                tint = tint,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleControl(
    onClick: () -> Unit,
    container: Color,
    content: Color,
    size: Dp,
    icon: @Composable (Color) -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(container)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        icon(content)
    }
}

/** Indicador minimalista dos sets vencidos. */
@Composable
private fun SetsPips(sets: Int, color: Color, total: Int = 3) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier.padding(top = 12.dp)
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(if (i < sets) color else color.copy(alpha = 0.18f))
            )
        }
    }
}
