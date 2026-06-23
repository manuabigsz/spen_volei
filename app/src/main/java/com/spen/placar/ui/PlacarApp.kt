package com.spen.placar.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spen.placar.ui.history.HistoryScreen
import com.spen.placar.ui.scoreboard.ScoreboardScreen
import com.spen.placar.ui.scoreboard.ScoreboardViewModel
import com.spen.placar.ui.theme.SPenPlacarTheme
import com.spen.placar.util.FeedbackPlayer

/**
 * Raiz da UI: aplica o tema, conecta efeitos (som/vibração) e define a
 * navegação entre o placar e o histórico de partidas.
 */
@Composable
fun PlacarApp(
    viewModel: ScoreboardViewModel,
    spenAvailable: Boolean
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    SPenPlacarTheme(themeMode = settings.themeMode) {
        val context = LocalContext.current
        val feedbackPlayer = remember { FeedbackPlayer(context) }

        // Conecta os efeitos pontuais do ViewModel ao som/vibração.
        LaunchedEffect(Unit) {
            viewModel.effects.collect { effect ->
                feedbackPlayer.handle(
                    effect = effect,
                    soundEnabled = viewModel.settings.value.soundEnabled,
                    vibrationEnabled = viewModel.settings.value.vibrationEnabled
                )
            }
        }

        val navController = rememberNavController()

        val state by viewModel.match.collectAsStateWithLifecycle()
        val history by viewModel.history.collectAsStateWithLifecycle()
        val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
        val elapsed by viewModel.elapsedMillis.collectAsStateWithLifecycle()
        val timerRunning by viewModel.timerRunning.collectAsStateWithLifecycle()
        val spenFeedback by viewModel.spenFeedback.collectAsStateWithLifecycle()
        val spenDebug by viewModel.spenDebug.collectAsStateWithLifecycle()
        val savedMatches by viewModel.savedMatches.collectAsStateWithLifecycle()

        NavHost(navController = navController, startDestination = "scoreboard") {
            composable("scoreboard") {
                ScoreboardScreen(
                    state = state,
                    settings = settings,
                    history = history,
                    canUndo = canUndo,
                    elapsedMillis = elapsed,
                    timerRunning = timerRunning,
                    spenFeedback = spenFeedback,
                    spenAvailable = spenAvailable,
                    spenDebug = spenDebug,
                    onAddPoint = viewModel::addPoint,
                    onRemovePoint = viewModel::removePoint,
                    onUndo = viewModel::undo,
                    onReset = viewModel::resetMatch,
                    onToggleTimer = viewModel::toggleTimer,
                    onEditName = viewModel::setTeamName,
                    onShare = { shareText(context, viewModel.shareText()) },
                    onOpenHistory = { navController.navigate("history") },
                    onSpenConsumed = viewModel::consumeSpenFeedback,
                    onSetTheme = viewModel::setTheme,
                    onSetSound = viewModel::setSound,
                    onSetVibration = viewModel::setVibration,
                    onSetSpen = viewModel::setSpen
                )
            }
            composable("history") {
                HistoryScreen(
                    matches = savedMatches,
                    onBack = { navController.popBackStack() },
                    onDelete = viewModel::deleteSavedMatch,
                    onShare = { match ->
                        val text = "🏐 ${match.teamAName} ${match.setsA} x ${match.setsB} " +
                            "${match.teamBName}\nSets: ${match.scoreSummary}\n" +
                            "Vencedor: ${match.winnerName} 🏆\n— S Pen Placar"
                        shareText(context, text)
                    }
                )
            }
        }
    }
}

/** Dispara o seletor de compartilhamento do Android com o texto do resultado. */
private fun shareText(context: android.content.Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar resultado"))
}
