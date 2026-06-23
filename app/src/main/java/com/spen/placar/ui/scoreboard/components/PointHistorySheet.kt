package com.spen.placar.ui.scoreboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spen.placar.domain.ScoreEvent
import com.spen.placar.domain.TeamSide

/** Bottom sheet com o histórico de pontos da partida atual. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointHistorySheet(
    history: List<ScoreEvent>,
    teamAName: String,
    teamBName: String,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Histórico de pontos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum ponto registrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Mais recentes primeiro.
                itemsIndexed(history.reversed()) { _, event ->
                    val scorer = if (event.side == TeamSide.A) teamAName else teamBName
                    val suffix = if (event.closedSet) "  — fim do set ${event.setNumber}" else ""
                    Text(
                        text = "Set ${event.setNumber} • ${event.pointsAAfter}-${event.pointsBAfter}  ($scorer)$suffix",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (event.closedSet) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
        Box(modifier = Modifier.padding(16.dp))
    }
}
