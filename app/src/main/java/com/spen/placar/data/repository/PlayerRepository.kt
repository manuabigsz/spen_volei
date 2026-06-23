package com.spen.placar.data.repository

import com.spen.placar.data.local.PlayerConstraintDao
import com.spen.placar.data.local.PlayerConstraintEntity
import com.spen.placar.data.local.PlayerDao
import com.spen.placar.data.local.PlayerEntity
import kotlinx.coroutines.flow.Flow

/** Repositório dos jogadores cadastrados e das restrições de sorteio. */
class PlayerRepository(
    private val dao: PlayerDao,
    private val constraintDao: PlayerConstraintDao
) {

    val players: Flow<List<PlayerEntity>> = dao.observeAll()
    val constraints: Flow<List<PlayerConstraintEntity>> = constraintDao.observeAll()

    suspend fun upsert(player: PlayerEntity) {
        if (player.id == 0L) dao.insert(player) else dao.update(player)
    }

    suspend fun importAll(players: List<PlayerEntity>) = dao.insertAll(players)

    suspend fun setPresent(id: Long, present: Boolean) = dao.setPresent(id, present)

    suspend fun setAllPresent(present: Boolean) = dao.setAllPresent(present)

    suspend fun delete(id: Long) {
        dao.deleteById(id)
        constraintDao.deleteForPlayer(id)
    }

    suspend fun deleteMany(ids: List<Long>) {
        if (ids.isEmpty()) return
        dao.deleteByIds(ids)
        ids.forEach { constraintDao.deleteForPlayer(it) }
    }

    suspend fun clearAll() = dao.clear()

    // --- Restrições -------------------------------------------------------
    suspend fun addConstraint(aId: Long, bId: Long) {
        if (aId != bId) constraintDao.insert(PlayerConstraintEntity(aId = aId, bId = bId))
    }

    suspend fun removeConstraint(id: Long) = constraintDao.deleteById(id)
}
