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

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.clear()
}
