package com.spen.placar.ui.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spen.placar.data.local.PlayerConstraintEntity
import com.spen.placar.data.local.PlayerEntity
import com.spen.placar.data.local.total
import com.spen.placar.data.repository.PlayerRepository
import com.spen.placar.domain.BalancedTeams
import com.spen.placar.domain.SkillLevel
import com.spen.placar.domain.balanceTeams
import com.spen.placar.util.CsvPlayers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel do cadastro de jogadores e do sorteio de times. */
class PlayersViewModel(
    private val repository: PlayerRepository,
    private val supabaseRemote: com.spen.placar.data.remote.SupabaseRemote? = null
) : ViewModel() {

    val players: StateFlow<List<PlayerEntity>> = repository.players
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val constraints: StateFlow<List<PlayerConstraintEntity>> = repository.constraints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Resultado do último sorteio (null = nenhum gerado ainda).
    private val _teams = MutableStateFlow<BalancedTeams<PlayerEntity>?>(null)
    val teams: StateFlow<BalancedTeams<PlayerEntity>?> = _teams.asStateFlow()

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing.asStateFlow()

    init {
        // Ao abrir, baixa os jogadores da nuvem e mescla com o banco local.
        syncFromCloud()
    }

    /** Puxa os jogadores do Supabase e mescla no banco local. */
    fun syncFromCloud(onResult: (Boolean) -> Unit = {}) {
        val remote = supabaseRemote
        if (remote == null || !remote.isConfigured) {
            onResult(false)
            return
        }
        viewModelScope.launch {
            _syncing.value = true
            val list = remote.fetchPlayers()
            repository.applyRemotePlayers(list)
            _syncing.value = false
            onResult(true)
        }
    }

    /** Histórico de evolução (snapshots) de um jogador. */
    fun historyFor(playerId: Long) = repository.historyFor(playerId)

    fun upsert(player: PlayerEntity) = viewModelScope.launch {
        repository.upsert(player)
        supabaseRemote?.savePlayer(player)
    }

    fun delete(id: Long) = viewModelScope.launch {
        val name = players.value.firstOrNull { it.id == id }?.name
        repository.delete(id)
        if (name != null) supabaseRemote?.deletePlayerByName(name)
    }

    /** Remove todos os jogadores atualmente selecionados (presentes). */
    fun deleteSelected() = viewModelScope.launch {
        val selected = players.value.filter { it.present }
        repository.deleteMany(selected.map { it.id })
        selected.forEach { supabaseRemote?.deletePlayerByName(it.name) }
    }
    fun togglePresent(player: PlayerEntity) =
        viewModelScope.launch { repository.setPresent(player.id, !player.present) }
    fun setAllPresent(present: Boolean) = viewModelScope.launch { repository.setAllPresent(present) }

    /**
     * Importa jogadores de um CSV, **ignorando nomes que já existem** (sem
     * duplicar) e também duplicatas dentro do próprio arquivo. Sincroniza os
     * novos com a nuvem. Retorna (importados, ignorados).
     */
    fun importCsv(content: String, onResult: (imported: Int, skipped: Int) -> Unit) {
        viewModelScope.launch {
            val parsed = CsvPlayers.parse(content)
            val seen = players.value.map { SkillLevel.normalize(it.name) }.toMutableSet()
            val toImport = mutableListOf<com.spen.placar.data.local.PlayerEntity>()
            for (p in parsed) {
                val key = SkillLevel.normalize(p.name)
                if (key.isNotEmpty() && seen.add(key)) toImport.add(p)
            }
            if (toImport.isNotEmpty()) {
                repository.importAll(toImport)
                supabaseRemote?.savePlayers(toImport)
            }
            onResult(toImport.size, parsed.size - toImport.size)
        }
    }

    /** Sorteia times balanceados com os jogadores presentes, respeitando as
     *  restrições (pares que não podem ficar juntos). */
    fun draw(teamCount: Int) {
        val present = players.value.filter { it.present }
        if (present.size < teamCount) {
            _teams.value = null
            return
        }
        val pairs = constraints.value.map { setOf(it.aId, it.bId) }
        _teams.value = balanceTeams(
            players = present,
            teamCount = teamCount,
            weight = { it.total },
            separated = { x, y -> pairs.any { it.contains(x.id) && it.contains(y.id) } }
        )
    }

    fun clearDraw() {
        _teams.value = null
    }

    fun addConstraint(aId: Long, bId: Long) = viewModelScope.launch {
        repository.addConstraint(aId, bId)
    }

    fun removeConstraint(id: Long) = viewModelScope.launch {
        repository.removeConstraint(id)
    }

    /** Salva o sorteio atual no Supabase (best-effort). */
    fun saveDrawRemote(onResult: (Boolean) -> Unit) {
        val result = _teams.value ?: return onResult(false)
        viewModelScope.launch {
            val teams = result.teams.map { team -> team.map { it.name } }
            val bench = result.bench.map { it.name }
            val ok = supabaseRemote?.saveDraw(teams, bench) ?: false
            onResult(ok)
        }
    }

    class Factory(
        private val repository: PlayerRepository,
        private val supabaseRemote: com.spen.placar.data.remote.SupabaseRemote? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PlayersViewModel(repository, supabaseRemote) as T
    }
}
