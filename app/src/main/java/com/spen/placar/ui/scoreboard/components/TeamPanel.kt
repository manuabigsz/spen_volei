package com.spen.placar.ui.scoreboard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spen.placar.ui.theme.TeamNameStyle

/**
 * Painel de uma equipe em layout **horizontal** (faixa larga):
 * informações à esquerda, número grande ao centro e botões à direita.
 *
 * O tamanho do número se adapta sozinho à altura/largura disponível
 * (responsivo para celular e tablet). O painel inteiro soma ponto ao toque.
 *
 * @param setPoints pontos desta equipe nos sets já encerrados (placar).
 */
@Composable
fun TeamPanel(
    name: String,
    points: Int,
    sets: Int,
    setPoints: List<Int>,
    color: Color,
    onAddPoint: () -> Unit,
    onRemovePoint: () -> Unit,
    onEditName: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(color.copy(alpha = 0.06f))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.18f)), RoundedCornerShape(32.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onAddPoint)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            // Número responsivo ao tamanho da faixa.
            val scoreSp = with(LocalDensity.current) {
                val byHeight = maxHeight * 0.62f
                val byWidth = maxWidth * 0.26f
                minOf(byHeight, byWidth).coerceIn(64.dp, 220.dp).toSp()
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Esquerda: nome, sets e placar dos sets
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = name.uppercase(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = TeamNameStyle,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onEditName)
                            .padding(vertical = 4.dp)
                    )
                    SetsPips(sets = sets, color = color)
                    if (setPoints.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            setPoints.forEach { p ->
                                Text(
                                    text = p.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Centro: número gigante animado
                Box(
                    modifier = Modifier.weight(1.1f),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = points,
                        transitionSpec = {
                            val spec = tween<Float>(220)
                            if (targetState >= initialState) {
                                (slideInVertically(tween(220)) { it / 3 } + fadeIn(spec)) togetherWith
                                    (slideOutVertically(tween(220)) { -it / 3 } + fadeOut(spec))
                            } else {
                                (slideInVertically(tween(220)) { -it / 3 } + fadeIn(spec)) togetherWith
                                    (slideOutVertically(tween(220)) { it / 3 } + fadeOut(spec))
                            }.using(SizeTransform(clip = false))
                        },
                        label = "score"
                    ) { value ->
                        Text(
                            text = value.toString(),
                            color = color,
                            fontWeight = FontWeight.Medium,
                            fontSize = scoreSp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Direita: botões − / +
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleControl(
                        onClick = onRemovePoint,
                        container = MaterialTheme.colorScheme.surfaceVariant,
                        content = MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 52.dp,
                        icon = { tint ->
                            Icon(Icons.Filled.Remove, contentDescription = "Remover ponto de $name", tint = tint)
                        }
                    )
                    CircleControl(
                        onClick = onAddPoint,
                        container = color,
                        content = Color.White,
                        size = 68.dp,
                        icon = { tint ->
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "Adicionar ponto para $name",
                                tint = tint,
                                modifier = Modifier.size(32.dp)
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
        modifier = Modifier.padding(top = 8.dp)
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (i < sets) color else color.copy(alpha = 0.20f))
            )
        }
    }
}
