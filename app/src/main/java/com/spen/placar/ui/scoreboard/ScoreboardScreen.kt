package com.spen.placar.ui.scoreboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spen.placar.data.prefs.AppSettings
import com.spen.placar.data.prefs.ThemeMode
import com.spen.placar.domain.MatchState
import com.spen.placar.domain.ScoreEvent
import com.spen.placar.domain.TeamSide
import com.spen.placar.ui.scoreboard.components.Confetti
import com.spen.placar.ui.scoreboard.components.EditNameDialog
import com.spen.placar.ui.scoreboard.components.PointHistorySheet
import com.spen.placar.ui.scoreboard.components.SettingsDialog
import com.spen.placar.ui.scoreboard.components.SpenIndicatorOverlay
import com.spen.placar.ui.scoreboard.components.TeamPanel
import com.spen.placar.ui.theme.teamAColor
import com.spen.placar.ui.theme.teamBColor
import com.spen.placar.util.formatDuration

/**
 * Tela principal: placar grande, minimalista e responsivo.
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
    onAddPoint: (TeamSide) -> Unit,
    onRemovePoint: (TeamSide) -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    onToggleTimer: () -> Unit,
    onEditName: (TeamSide, String) -> Unit,
    onShare: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenPlayers: () -> Unit,
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

    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val colorA = teamAColor(dark)
    val colorB = teamBColor(dark)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.setsA} – ${state.setsB}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (state.isTieBreak) "TIE-BREAK · SET ${state.currentSet}"
                            else "SET ${state.currentSet}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onToggleTimer) {
                            Icon(
                                if (timerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Cronômetro",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            formatDuration(elapsedMillis),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "Mais opções",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        shape = RoundedCornerShape(18.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        shadowElevation = 8.dp,
                        modifier = Modifier.width(244.dp)
                    ) {
                        PrettyMenuItem("Jogadores e times", Icons.Filled.Groups) {
                            menuOpen = false; onOpenPlayers()
                        }
                        PrettyMenuItem("Histórico de pontos", Icons.AutoMirrored.Filled.List) {
                            menuOpen = false; showPointHistory = true
                        }
                        PrettyMenuItem("Histórico de partidas", Icons.Filled.EmojiEvents) {
                            menuOpen = false; onOpenHistory()
                        }
                        PrettyMenuItem("Compartilhar resultado", Icons.Filled.Share) {
                            menuOpen = false; onShare()
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                        PrettyMenuItem("Nova partida", Icons.Filled.Refresh) {
                            menuOpen = false; onReset()
                        }
                        PrettyMenuItem("Configurações", Icons.Filled.Settings) {
                            menuOpen = false; showSettings = true
                        }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                val setsPointsA = state.completedSets.map { it.pointsA }
                val setsPointsB = state.completedSets.map { it.pointsB }
                val highlightA = spenFeedback?.action == SpenAction.POINT_A
                val highlightB = spenFeedback?.action == SpenAction.POINT_B

                // Cards lado a lado: A à esquerda, B à direita
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TeamPanel(
                        name = state.teamAName,
                        points = state.pointsA,
                        sets = state.setsA,
                        setPoints = setsPointsA,
                        accent = colorA,
                        highlighted = highlightA,
                        onAddPoint = { onAddPoint(TeamSide.A) },
                        onRemovePoint = { onRemovePoint(TeamSide.A) },
                        onEditName = { editing = TeamSide.A },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    TeamPanel(
                        name = state.teamBName,
                        points = state.pointsB,
                        sets = state.setsB,
                        setPoints = setsPointsB,
                        accent = colorB,
                        highlighted = highlightB,
                        onAddPoint = { onAddPoint(TeamSide.B) },
                        onRemovePoint = { onRemovePoint(TeamSide.B) },
                        onEditName = { editing = TeamSide.B },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                // Controles inferiores
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = onUndo,
                            enabled = canUndo,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Undo,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("  Desfazer", fontWeight = FontWeight.Medium)
                        }
                        IconButton(
                            onClick = { showPointHistory = true },
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = "Histórico de pontos",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

            // Indicador visual de comando da S Pen
            SpenIndicatorOverlay(feedback = spenFeedback, onConsumed = onSpenConsumed)

            // Overlay de vitória
            if (state.finished && state.winner != null) {
                val winnerName = if (state.winner == TeamSide.A) state.teamAName else state.teamBName
                val winnerColor = if (state.winner == TeamSide.A) colorA else colorB
                WinnerOverlay(
                    winnerName = winnerName,
                    winnerColor = winnerColor,
                    confettiColors = listOf(colorA, colorB, winnerColor),
                    setsA = state.setsA,
                    setsB = state.setsB,
                    onNewMatch = onReset,
                    onShare = onShare
                )
            }
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

/** Item de menu refinado: ícone discreto, texto consistente e bom espaçamento. */
@Composable
private fun PrettyMenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text, style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
    )
}

/** Overlay elegante exibido ao final da partida. */
@Composable
private fun WinnerOverlay(
    winnerName: String,
    winnerColor: Color,
    confettiColors: List<Color>,
    setsA: Int,
    setsB: Int,
    onNewMatch: () -> Unit,
    onShare: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Confetti(colors = confettiColors, modifier = Modifier.fillMaxSize())
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 36.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = winnerColor,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = winnerName.uppercase(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = "venceu a partida",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$setsA – $setsB",
                        fontWeight = FontWeight.Light,
                        fontSize = 40.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilledTonalButton(onClick = onShare, shape = RoundedCornerShape(50)) {
                            Icon(Icons.Filled.Share, null, modifier = Modifier.size(18.dp))
                            Text("  Compartilhar")
                        }
                        FilledTonalButton(onClick = onNewMatch, shape = RoundedCornerShape(50)) {
                            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                            Text("  Nova partida")
                        }
                    }
                }
            }
        }
    }
}
