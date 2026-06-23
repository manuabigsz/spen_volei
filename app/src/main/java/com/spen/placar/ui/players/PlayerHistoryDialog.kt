package com.spen.placar.ui.players

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spen.placar.data.local.PlayerEntity
import com.spen.placar.data.local.PlayerHistoryEntity
import com.spen.placar.data.local.weights
import com.spen.placar.domain.Skill
import com.spen.placar.domain.SkillLevel
import com.spen.placar.util.formatDate

private val Up = Color(0xFF22C55E)
private val Down = Color(0xFFEF4444)

/** Mostra a evolução (melhoras/quedas) das habilidades de um jogador. */
@Composable
fun PlayerHistoryDialog(
    player: PlayerEntity,
    viewModel: PlayersViewModel,
    onDismiss: () -> Unit
) {
    val historyFlow = remember(player.id) { viewModel.historyFor(player.id) }
    val history by historyFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
        title = { Text("Evolução · ${player.name}") },
        text = {
            if (history.isEmpty()) {
                Text(
                    "Sem registros ainda. As alterações de habilidade aparecem aqui.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier
                        .heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    history.forEachIndexed { i, snap ->
                        val older = history.getOrNull(i + 1) // anterior no tempo
                        HistoryEntry(snap, older, isLatest = i == 0)
                        if (i < history.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun HistoryEntry(
    snap: PlayerHistoryEntity,
    older: PlayerHistoryEntity?,
    isLatest: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                formatDate(snap.recordedAt) + if (isLatest) "  (atual)" else "",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TotalTrend(snap.total, older?.total)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val cur = snap.weights
            val prev = older?.weights
            Skill.entries.forEachIndexed { idx, skill ->
                SkillCell(
                    short = skill.short,
                    level = SkillLevel.fromWeight(cur[idx]),
                    delta = if (prev != null) cur[idx] - prev[idx] else 0
                )
            }
        }
    }
}

@Composable
private fun TotalTrend(total: Int, oldTotal: Int?) {
    val delta = if (oldTotal != null) total - oldTotal else 0
    val (arrow, color) = when {
        delta > 0 -> "▲" to Up
        delta < 0 -> "▼" to Down
        else -> "" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Total $total", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        if (arrow.isNotEmpty()) {
            Text(
                "  $arrow${if (delta > 0) "+$delta" else delta}",
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun SkillCell(short: String, level: SkillLevel, delta: Int) {
    val letter = when (level) {
        SkillLevel.AVANCADO -> "A"
        SkillLevel.INTERMEDIARIO -> "I"
        SkillLevel.BASICO -> "B"
    }
    val arrow = when {
        delta > 0 -> "▲"
        delta < 0 -> "▼"
        else -> ""
    }
    val arrowColor = if (delta > 0) Up else Down
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(short, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(letter, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (arrow.isNotEmpty()) {
                Text(arrow, color = arrowColor, fontSize = 11.sp)
            }
        }
    }
}
