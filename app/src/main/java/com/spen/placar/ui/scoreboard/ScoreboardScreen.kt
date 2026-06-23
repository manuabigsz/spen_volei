package com.spen.placar.ui.scoreboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spen.placar.data.prefs.AppSettings
import com.spen.placar.domain.MatchState
import com.spen.placar.domain.ScoreEvent
import com.spen.placar.domain.TeamSide
import com.spen.placar.ui.scoreboard.components.CastButton
import com.spen.placar.ui.scoreboard.components.EditNameDialog
import com.spen.placar.ui.scoreboard.components.PointHistorySheet
import com.spen.placar.ui.scoreboard.components.SettingsDialog
import com.spen.placar.ui.scoreboard.components.SpenIndicatorOverlay
import com.spen.placar.ui.scoreboard.components.TeamPanel
import com.spen.placar.ui.theme.TeamAColor
import com.spen.placar.ui.theme.TeamBColor
import com.spen.placar.data.prefs.ThemeMode
import com.spen.placar.util.formatDuration

/**
 * Tela principal: o placar grande, responsivo e otimizado para operação rápida.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreboardScreen(
    state: MatchState,
    settings: AppSettings,
    history: List<ScoreEvent>,
    canUndo: Boolean,
    elapsedMillis: Long,
    timerRunning: Boolean,
    spenFeedback: SpenFeedback?,
    spenAvailable: Boolean,
    spenDebug: List<String>,
    onAddPoint: (TeamSide) -> Unit,
    onRemovePoint: (TeamSide) -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    onToggleTimer: () -> Unit,
    onEditName: (TeamSide, String) -> Unit,
    onShare: () -> Unit,
    onOpenHistory: () -> Unit,
    onSpenConsumed: () -> Unit,
    onSetTheme: (ThemeMode) -> Unit,
    onSetSound: (Boolean) -> Unit,
    onSetVibration: (Boolean) -> Unit,
    onSetSpen: (Boolean) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showPointHistory by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<TeamSide?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${state.teamAName} ${state.setsA} x ${state.setsB} ${state.teamBName}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (state.isTieBreak) "Tie-break (Set ${state.currentSet})"
                            else "Set ${state.currentSet}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Cronômetro com play/pause
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(formatDuration(elapsedMillis), fontWeight = FontWeight.Medium)
                        IconButton(onClick = onToggleTimer) {
                            Icon(
                                if (timerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Cronômetro"
                            )
                        }
                    }
                    CastButton()
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Mais opções")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Histórico de pontos") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                            onClick = { menuOpen = false; showPointHistory = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Histórico de partidas") },
                            leadingIcon = { Icon(Icons.Filled.Refresh, null) },
                            onClick = { menuOpen = false; onOpenHistory() }
                        )
                        DropdownMenuItem(
                            text = { Text("Compartilhar resultado") },
                            leadingIcon = { Icon(Icons.Filled.Share, null) },
                            onClick = { menuOpen = false; onShare() }
                        )
                        DropdownMenuItem(
                            text = { Text("Nova partida") },
                            leadingIcon = { Icon(Icons.Filled.Refresh, null) },
                            onClick = { menuOpen = false; onReset() }
                        )
                        DropdownMenuItem(
                            text = { Text("Configurações") },
                            leadingIcon = { Icon(Icons.Filled.Settings, null) },
                            onClick = { menuOpen = false; showSettings = true }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                // Fonte do placar escalada conforme o espaço (responsivo p/ tablet).
                val scoreSize = (minOf(maxWidth, maxHeight) * 0.55f).coerceIn(96.dp, 360.dp)

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TeamPanel(
                            name = state.teamAName,
                            points = state.pointsA,
                            sets = state.setsA,
                            color = TeamAColor,
                            scoreFontSize = scoreSize,
                            onAddPoint = { onAddPoint(TeamSide.A) },
                            onRemovePoint = { onRemovePoint(TeamSide.A) },
                            onEditName = { editing = TeamSide.A },
                            modifier = Modifier.weight(1f)
                        )
                        TeamPanel(
                            name = state.teamBName,
                            points = state.pointsB,
                            sets = state.setsB,
                            color = TeamBColor,
                            scoreFontSize = scoreSize,
                            onAddPoint = { onAddPoint(TeamSide.B) },
                            onRemovePoint = { onRemovePoint(TeamSide.B) },
                            onEditName = { editing = TeamSide.B },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Barra inferior: desfazer + atalho de histórico
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = onUndo,
                            modifier = Modifier.weight(1f),
                            containerColor = if (canUndo) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            icon = { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null) },
                            text = { Text("Desfazer") }
                        )
                        IconButton(onClick = { showPointHistory = true }) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Histórico de pontos")
                        }
                    }
                }
            }

            // Banner de partida encerrada
            if (state.finished && state.winner != null) {
                val winnerName = if (state.winner == TeamSide.A) state.teamAName else state.teamBName
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "🏆 $winnerName venceu a partida!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Indicador visual de comando da S Pen
            SpenIndicatorOverlay(feedback = spenFeedback, onConsumed = onSpenConsumed)

            // Painel de diagnóstico da S Pen (toque para recolher/expandir)
            SpenDebugPanel(
                spenAvailable = spenAvailable,
                lines = spenDebug,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }

    // Diálogos / sheets
    editing?.let { side ->
        EditNameDialog(
            currentName = if (side == TeamSide.A) state.teamAName else state.teamBName,
            onDismiss = { editing = null },
            onConfirm = { onEditName(side, it); editing = null }
        )
    }

    if (showSettings) {
        SettingsDialog(
            settings = settings,
            spenAvailable = spenAvailable,
            onTheme = onSetTheme,
            onSound = onSetSound,
            onVibration = onSetVibration,
            onSpen = onSetSpen,
            onDismiss = { showSettings = false }
        )
    }

    if (showPointHistory) {
        PointHistorySheet(
            history = history,
            teamAName = state.teamAName,
            teamBName = state.teamBName,
            onDismiss = { showPointHistory = false }
        )
    }
}

/**
 * Painel flutuante de diagnóstico da S Pen. Mostra o status da conexão e as
 * últimas mensagens emitidas pelo SDK. Toque no cabeçalho para recolher.
 */
@Composable
private fun SpenDebugPanel(
    spenAvailable: Boolean,
    lines: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    val statusColor = if (spenAvailable) Color(0xFF66BB6A) else Color(0xFFEF5350)

    Box(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xCC000000))
            .widthIn(max = 320.dp)
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = "S Pen: ${if (spenAvailable) "CONECTADA ✓" else "não conectada"}  (toque)",
                color = statusColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 2.dp)
            )
            if (expanded) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 160.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    lines.forEach { line ->
                        Text(
                            text = "• $line",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}
