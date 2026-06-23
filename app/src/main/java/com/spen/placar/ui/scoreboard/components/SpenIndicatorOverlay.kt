package com.spen.placar.ui.scoreboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spen.placar.ui.scoreboard.SpenFeedback
import kotlinx.coroutines.delay

/**
 * Indicador visual transitório exibido quando um comando da S Pen é recebido.
 * Aparece no centro da tela e some sozinho após um curto período.
 */
@Composable
fun SpenIndicatorOverlay(
    feedback: SpenFeedback?,
    onConsumed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(feedback?.token) {
        if (feedback != null) {
            visible = true
            delay(900)
            visible = false
            delay(200)
            onConsumed()
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = visible && feedback != null,
            enter = fadeIn() + scaleIn(initialScale = 0.7f),
            exit = fadeOut()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.92f))
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit, // ícone que remete à caneta
                    contentDescription = "Comando S Pen",
                    tint = Color.Black
                )
                Text(
                    text = feedback?.message ?: "",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
    }
}
