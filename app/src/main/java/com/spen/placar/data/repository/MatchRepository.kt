package com.spen.placar.data.repository

import com.spen.placar.data.local.MatchDao
import com.spen.placar.data.local.MatchEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositório do histórico de partidas — única porta de entrada para os
 * dados persistidos, isolando o restante do app do Room (MVVM).
 */
class MatchRepository(private val dao: MatchDao) {

    val history: Flow<List<MatchEntity>> = dao.observeAll()

    suspend fun save(match: MatchEntity): Long = dao.insert(match)

    /**
     * Mescla partidas vindas da nuvem no banco local, evitando duplicatas
     * (usa o instante de término como chave — único na prática).
     */
    suspend fun applyRemoteMatches(remote: List<MatchEntity>) {
        val existing = dao.allFinishedAt().toSet()
        remote.filter { it.finishedAt !in existing }
            .forEach { dao.insert(it.copy(id = 0)) }
    }

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.clear()
}
