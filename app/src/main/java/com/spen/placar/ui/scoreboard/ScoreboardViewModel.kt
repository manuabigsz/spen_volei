package com.spen.placar.ui.scoreboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spen.placar.data.local.MatchEntity
import com.spen.placar.data.prefs.AppSettings
import com.spen.placar.data.prefs.SettingsRepository
import com.spen.placar.data.prefs.ThemeMode
import com.spen.placar.data.repository.MatchRepository
import com.spen.placar.domain.MatchState
import com.spen.placar.domain.ScoreEngine
import com.spen.placar.domain.ScoreEvent
import com.spen.placar.domain.TeamSide
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * ViewModel central do placar (MVVM).
 *
 * Concentra todo o estado da partida em andamento, a pilha de "desfazer",
 * o histórico de pontos, o cronômetro e a persistência ao final da partida.
 */
class ScoreboardViewModel(
    private val matchRepository: MatchRepository,
    private val settingsRepository: SettingsRepository,
    private val supabaseRemote: com.spen.placar.data.remote.SupabaseRemote? = null,
    private val now: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    // --- Estado da partida -------------------------------------------------
    private val _match = MutableStateFlow(MatchState())
    val match: StateFlow<MatchState> = _match.asStateFlow()

    // Pilha de snapshots para desfazer (cada ponto empilha o estado anterior).
    private val undoStack = ArrayDeque<MatchState>()

    // Elenco de cada time (quando a partida vem de um sorteio de jogadores).
    private var rostersA: List<String> = emptyList()
    private var rostersB: List<String> = emptyList()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    // --- Histórico de pontos do jogo atual --------------------------------
    private val _history = MutableStateFlow<List<ScoreEvent>>(emptyList())
    val history: StateFlow<List<ScoreEvent>> = _history.asStateFlow()

    // --- Cronômetro --------------------------------------------------------
    private val _elapsedMillis = MutableStateFlow(0L)
    val elapsedMillis: StateFlow<Long> = _elapsedMillis.asStateFlow()

    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()
    private var timerBaseElapsed = 0L
    private var timerStartedAt = 0L

    // --- Feedback da S Pen -------------------------------------------------
    private val _spenFeedback = MutableStateFlow<SpenFeedback?>(null)
    val spenFeedback: StateFlow<SpenFeedback?> = _spenFeedback.asStateFlow()

    // --- Log de diagnóstico da S Pen (exibido na tela) --------------------
    private val _spenDebug = MutableStateFlow<List<String>>(listOf("aguardando S Pen…"))
    val spenDebug: StateFlow<List<String>> = _spenDebug.asStateFlow()

    /** Acrescenta uma linha ao log de debug da S Pen (mantém as últimas 12). */
    fun addSpenDebug(message: String) {
        _spenDebug.value = (_spenDebug.value + message).takeLast(12)
    }

    // --- Efeitos (som/vibração) -------------------------------------------
    private val _effects = MutableSharedFlow<MatchEffect>(extraBufferCapacity = 8)
    val effects = _effects.asSharedFlow()

    // --- Preferências ------------------------------------------------------
    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    // --- Histórico persistido ---------------------------------------------
    val savedMatches = matchRepository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _syncingMatches = MutableStateFlow(false)
    val syncingMatches: StateFlow<Boolean> = _syncingMatches.asStateFlow()

    /** Baixa as partidas da nuvem e mescla no histórico local. */
    fun syncMatchesFromCloud() {
        val remote = supabaseRemote
        if (remote == null || !remote.isConfigured) return
        viewModelScope.launch {
            _syncingMatches.value = true
            val list = remote.fetchMatches()
            matchRepository.applyRemoteMatches(list)
            _syncingMatches.value = false
        }
    }

    init {
        startTimerTicker()
    }

    // =======================================================================
    //  Ações de pontuação
    // =======================================================================

    fun addPoint(side: TeamSide, ace: Boolean = false) {
        val current = _match.value
        if (current.finished) return

        // Inicia o cronômetro automaticamente no primeiro ponto.
        if (_elapsedMillis.value == 0L && !_timerRunning.value) startTimer()

        undoStack.addLast(current)
        val updated = ScoreEngine.addPoint(current, side)
        _match.value = updated
        _canUndo.value = true

        val closedSet = ScoreEngine.didCloseSet(current, updated)
        appendHistory(side, updated, closedSet)

        // Efeitos
        viewModelScope.launch {
            _effects.emit(if (ace) MatchEffect.Ace(side) else MatchEffect.PointScored(side))
        }
        if (closedSet) {
            val lastSet = updated.completedSets.last()
            viewModelScope.launch {
                _effects.emit(MatchEffect.SetWon(lastSet.winner, lastSet.setNumber))
            }
        }
        if (updated.finished && updated.winner != null) {
            stopTimer()
            viewModelScope.launch { _effects.emit(MatchEffect.MatchWon(updated.winner)) }
            val wName = if (updated.winner == TeamSide.A) updated.teamAName else updated.teamBName
            announce("Fim de jogo! $wName venceu!")
            persistFinishedMatch(updated)
        } else {
            val sp = updated.setPointSide
            if (sp != null && current.setPointSide != sp) {
                // Set point / match point ao entrar nesse estado.
                val name = if (sp == TeamSide.A) updated.teamAName else updated.teamBName
                announce(if (updated.matchPoint) "$name está quase ganhando!" else "Set point para $name")
                viewModelScope.launch { _effects.emit(MatchEffect.SetPoint(updated.matchPoint)) }
            } else {
                // A cada 5 pontos do time que marcou, anuncia o placar completo.
                val justScored = if (side == TeamSide.A) updated.pointsA else updated.pointsB
                if (justScored > 0 && justScored % 5 == 0) {
                    announce(
                        "${updated.pointsA} pontos para ${updated.teamAName}, " +
                            "e ${updated.pointsB} para ${updated.teamBName}"
                    )
                }
            }
        }
    }

    private fun announce(text: String) {
        viewModelScope.launch { _effects.emit(MatchEffect.Announce(text)) }
    }

    fun removePoint(side: TeamSide) {
        val current = _match.value
        val a = if (side == TeamSide.A) (current.pointsA - 1).coerceAtLeast(0) else current.pointsA
        val b = if (side == TeamSide.B) (current.pointsB - 1).coerceAtLeast(0) else current.pointsB
        if (a == current.pointsA && b == current.pointsB) return

        undoStack.addLast(current)
        _match.value = current.copy(pointsA = a, pointsB = b)
        _canUndo.value = true
    }

    /** Desfaz a última ação (ponto adicionado/removido). */
    fun undo() {
        val previous = undoStack.removeLastOrNull() ?: return
        _match.value = previous
        _canUndo.value = undoStack.isNotEmpty()

        // Mantém o histórico coerente com o estado restaurado.
        _history.value = _history.value.dropLast(1)

        viewModelScope.launch { _effects.emit(MatchEffect.Undone) }
    }

    /** Inicia uma nova partida, preservando os nomes dos times. */
    fun resetMatch() {
        val names = _match.value
        _match.value = MatchState(teamAName = names.teamAName, teamBName = names.teamBName)
        undoStack.clear()
        _canUndo.value = false
        _history.value = emptyList()
        resetTimer()
    }

    /** Inicia uma partida com times vindos do sorteio (nomes + elencos). */
    fun applyTeams(nameA: String, playersA: List<String>, nameB: String, playersB: List<String>) {
        rostersA = playersA
        rostersB = playersB
        _match.value = MatchState(teamAName = nameA, teamBName = nameB)
        undoStack.clear()
        _canUndo.value = false
        _history.value = emptyList()
        resetTimer()
    }

    fun setTeamName(side: TeamSide, name: String) {
        _match.value = when (side) {
            TeamSide.A -> _match.value.copy(teamAName = name.ifBlank { "Time A" })
            TeamSide.B -> _match.value.copy(teamBName = name.ifBlank { "Time B" })
        }
    }

    private fun appendHistory(side: TeamSide, state: MatchState, closedSet: Boolean) {
        // Após fechar um set os pontos são zerados; usa o set encerrado para
        // registrar o placar correto no histórico.
        val (pa, pb) = if (closedSet) {
            val s = state.completedSets.last()
            s.pointsA to s.pointsB
        } else state.pointsA to state.pointsB

        val setNumber = if (closedSet) state.completedSets.last().setNumber else state.currentSet
        _history.value = _history.value + ScoreEvent(
            side = side,
            setNumber = setNumber,
            pointsAAfter = pa,
            pointsBAfter = pb,
            timestamp = now(),
            closedSet = closedSet
        )
    }

    // =======================================================================
    //  Comandos da S Pen
    // =======================================================================

    /** Trata um comando vindo do controle da S Pen e emite feedback visual. */
    fun onSpenAction(action: SpenAction) {
        if (!settings.value.spenEnabled) {
            addSpenDebug("ação $action ignorada (S Pen desligada nas config)")
            return
        }
        addSpenDebug("AÇÃO: $action")
        when (action) {
            SpenAction.POINT_A -> addPoint(TeamSide.A)
            SpenAction.POINT_B -> addPoint(TeamSide.B)
            SpenAction.UNDO -> undo()
        }
        val msg = when (action) {
            SpenAction.POINT_A -> "+1 ${_match.value.teamAName}"
            SpenAction.POINT_B -> "+1 ${_match.value.teamBName}"
            SpenAction.UNDO -> "Desfeito"
        }
        _spenFeedback.value = SpenFeedback(action, msg, now())
    }

    fun consumeSpenFeedback() {
        _spenFeedback.value = null
    }

    // =======================================================================
    //  Cronômetro
    // =======================================================================

    fun toggleTimer() = if (_timerRunning.value) stopTimer() else startTimer()

    private fun startTimer() {
        if (_timerRunning.value) return
        timerBaseElapsed = _elapsedMillis.value
        timerStartedAt = now()
        _timerRunning.value = true
    }

    private fun stopTimer() {
        if (!_timerRunning.value) return
        _elapsedMillis.value = timerBaseElapsed + (now() - timerStartedAt)
        _timerRunning.value = false
    }

    fun resetTimer() {
        _timerRunning.value = false
        _elapsedMillis.value = 0L
        timerBaseElapsed = 0L
    }

    private fun startTimerTicker() {
        viewModelScope.launch {
            while (isActive) {
                if (_timerRunning.value) {
                    _elapsedMillis.value = timerBaseElapsed + (now() - timerStartedAt)
                }
                delay(250)
            }
        }
    }

    // =======================================================================
    //  Preferências
    // =======================================================================

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { settingsRepository.setTheme(mode) }
    fun setSound(on: Boolean) = viewModelScope.launch { settingsRepository.setSound(on) }
    fun setVibration(on: Boolean) = viewModelScope.launch { settingsRepository.setVibration(on) }
    fun setSpen(on: Boolean) = viewModelScope.launch { settingsRepository.setSpen(on) }
    fun setVoice(on: Boolean) = viewModelScope.launch { settingsRepository.setVoice(on) }
    fun setPointSound(side: TeamSide, value: String) = viewModelScope.launch {
        if (side == TeamSide.A) settingsRepository.setPointSoundA(value)
        else settingsRepository.setPointSoundB(value)
    }

    // =======================================================================
    //  Persistência
    // =======================================================================

    private fun persistFinishedMatch(state: MatchState) {
        val winnerName = when (state.winner) {
            TeamSide.A -> state.teamAName
            TeamSide.B -> state.teamBName
            null -> return
        }
        val summary = state.completedSets.joinToString(", ") { "${it.pointsA}-${it.pointsB}" }
        val entity = MatchEntity(
            teamAName = state.teamAName,
            teamBName = state.teamBName,
            setsA = state.setsA,
            setsB = state.setsB,
            winnerName = winnerName,
            scoreSummary = summary,
            durationMillis = _elapsedMillis.value,
            finishedAt = now(),
            playersA = rostersA.joinToString(", "),
            playersB = rostersB.joinToString(", ")
        )
        viewModelScope.launch {
            matchRepository.save(entity)
            // Sincroniza na nuvem (best-effort; ignora falhas offline).
            supabaseRemote?.saveMatch(entity)
        }
    }

    fun deleteSavedMatch(id: Long) = viewModelScope.launch { matchRepository.delete(id) }

    /** Texto pronto para compartilhamento do resultado atual. */
    fun shareText(): String {
        val s = _match.value
        val sb = StringBuilder()
        sb.append("🏐 ${s.teamAName} ${s.setsA} x ${s.setsB} ${s.teamBName}\n")
        if (s.completedSets.isNotEmpty()) {
            sb.append("Sets: ")
            sb.append(s.completedSets.joinToString(", ") { "${it.pointsA}-${it.pointsB}" })
            sb.append("\n")
        }
        if (!s.finished) {
            sb.append("Set ${s.currentSet}: ${s.pointsA}-${s.pointsB}\n")
        }
        s.winner?.let {
            val name = if (it == TeamSide.A) s.teamAName else s.teamBName
            sb.append("Vencedor: $name 🏆\n")
        }
        sb.append("Liga das Nações Femininas 2029")
        return sb.toString()
    }

    /**
     * Factory para instanciar o ViewModel com suas dependências (sem Hilt).
     */
    class Factory(
        private val matchRepository: MatchRepository,
        private val settingsRepository: SettingsRepository,
        private val supabaseRemote: com.spen.placar.data.remote.SupabaseRemote? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScoreboardViewModel(matchRepository, settingsRepository, supabaseRemote) as T
        }
    }
}
