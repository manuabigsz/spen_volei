package com.spen.placar.ui.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spen.placar.data.local.PlayerConstraintEntity
import com.spen.placar.data.local.PlayerEntity
import com.spen.placar.data.local.total
import com.spen.placar.data.repository.PlayerRepository
import com.spen.placar.domain.BalancedTeams
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
    private val repository: PlayerRepository
) : ViewModel() {

    val players: StateFlow<List<PlayerEntity>> = repository.players
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val constraints: StateFlow<List<PlayerConstraintEntity>> = repository.constraints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Resultado do último sorteio (null = nenhum gerado ainda).
    private val _teams = MutableStateFlow<BalancedTeams<PlayerEntity>?>(null)
    val teams: StateFlow<BalancedTeams<PlayerEntity>?> = _teams.asStateFlow()

    fun upsert(player: PlayerEntity) = viewModelScope.launch { repository.upsert(player) }
    fun delete(id: Long) = viewModelScope.launch { repository.delete(id) }

    /** Remove todos os jogadores atualmente selecionados (presentes). */
    fun deleteSelected() = viewModelScope.launch {
        val ids = players.value.filter { it.present }.map { it.id }
        repository.deleteMany(ids)
    }
    fun togglePresent(player: PlayerEntity) =
        viewModelScope.launch { repository.setPresent(player.id, !player.present) }
    fun setAllPresent(present: Boolean) = viewModelScope.launch { repository.setAllPresent(present) }

    /** Importa jogadores de um conteúdo CSV. Retorna quantos foram importados. */
    fun importCsv(content: String, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val parsed = CsvPlayers.parse(content)
            if (parsed.isNotEmpty()) repository.importAll(parsed)
            onResult(parsed.size)
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

    class Factory(private val repository: PlayerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PlayersViewModel(repository) as T
    }
}
