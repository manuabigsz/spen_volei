package com.spen.placar.ui.scoreboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

/**
 * Painel de uma equipe: grande, colorido e inteiramente clicável para
 * adicionar ponto (otimizado para velocidade durante o jogo).
 *
 * @param scoreFontSize tamanho do número do placar (escalado conforme a tela).
 */
@Composable
fun TeamPanel(
    name: String,
    points: Int,
    sets: Int,
    color: Color,
    scoreFontSize: Dp,
    onAddPoint: () -> Unit,
    onRemovePoint: () -> Unit,
    onEditName: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pequeno "pulo" do número a cada alteração para feedback visual.
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(120),
        label = "scoreScale"
    )

    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(28.dp))
            .background(color.copy(alpha = 0.18f))
            // Toque em qualquer lugar do painel = +1 ponto (operação rápida).
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onAddPoint
            )
            .semantics { contentDescription = "Adicionar ponto para $name" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Nome + sets vencidos
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = name.uppercase(),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onEditName)
                        .padding(4.dp)
                )
                SetsPips(sets = sets, color = color)
            }

            // Placar gigante
            Text(
                text = points.toString(),
                color = color,
                fontWeight = FontWeight.Black,
                fontSize = with(androidx.compose.ui.platform.LocalDensity.current) {
                    scoreFontSize.toSp()
                },
                modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
            )

            // Botões +1 / -1
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onRemovePoint,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Filled.Remove,
                        contentDescription = "Remover ponto de $name",
                        tint = color
                    )
                }
                FilledIconButton(
                    onClick = onAddPoint,
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = color)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Adicionar ponto para $name",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

/** Bolinhas representando os sets vencidos pela equipe. */
@Composable
private fun SetsPips(sets: Int, color: Color, total: Int = 3) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 6.dp)
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (i < sets) color else color.copy(alpha = 0.25f))
            )
        }
    }
}
