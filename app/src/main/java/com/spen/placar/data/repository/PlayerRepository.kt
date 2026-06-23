package com.spen.placar.data.repository

import com.spen.placar.data.local.PlayerConstraintDao
import com.spen.placar.data.local.PlayerConstraintEntity
import com.spen.placar.data.local.PlayerDao
import com.spen.placar.data.local.PlayerEntity
import com.spen.placar.data.local.PlayerHistoryDao
import com.spen.placar.data.local.PlayerHistoryEntity
import com.spen.placar.data.local.total
import kotlinx.coroutines.flow.Flow

/** Repositório dos jogadores, restrições de sorteio e histórico de evolução. */
class PlayerRepository(
    private val dao: PlayerDao,
    private val constraintDao: PlayerConstraintDao,
    private val historyDao: PlayerHistoryDao,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {

    val players: Flow<List<PlayerEntity>> = dao.observeAll()
    val constraints: Flow<List<PlayerConstraintEntity>> = constraintDao.observeAll()

    fun historyFor(playerId: Long): Flow<List<PlayerHistoryEntity>> =
        historyDao.observeForPlayer(playerId)

    suspend fun upsert(player: PlayerEntity): Long {
        return if (player.id == 0L) {
            val id = dao.insert(player)
            recordSnapshot(id, player)   // marco inicial
            id
        } else {
            val old = dao.getById(player.id)
            dao.update(player)
            if (old == null || skillsChanged(old, player)) recordSnapshot(player.id, player)
            player.id
        }
    }

    suspend fun importAll(players: List<PlayerEntity>) {
        players.forEach { p ->
            val id = dao.insert(p)
            recordSnapshot(id, p)
        }
    }

    /**
     * Aplica os jogadores vindos da nuvem ao banco local: insere os que faltam
     * (por nome) e atualiza os que mudaram de nível, sem reenviar à nuvem.
     */
    suspend fun applyRemotePlayers(remote: List<PlayerEntity>) {
        remote.forEach { r ->
            val local = dao.findByName(r.name)
            if (local == null) {
                val id = dao.insert(r.copy(id = 0, present = true))
                recordSnapshot(id, r)
            } else if (skillsChanged(local, r)) {
                val updated = local.copy(
                    saque = r.saque,
                    recepcao = r.recepcao,
                    levantamento = r.levantamento,
                    corte = r.corte,
                    movimentacao = r.movimentacao
                )
                dao.update(updated)
                recordSnapshot(local.id, updated)
            }
        }
    }

    suspend fun setPresent(id: Long, present: Boolean) = dao.setPresent(id, present)
    suspend fun setAllPresent(present: Boolean) = dao.setAllPresent(present)

    suspend fun delete(id: Long) {
        dao.deleteById(id)
        constraintDao.deleteForPlayer(id)
        historyDao.deleteForPlayer(id)
    }

    suspend fun deleteMany(ids: List<Long>) {
        if (ids.isEmpty()) return
        dao.deleteByIds(ids)
        ids.forEach {
            constraintDao.deleteForPlayer(it)
            historyDao.deleteForPlayer(it)
        }
    }

    suspend fun clearAll() = dao.clear()

    // --- Restrições -------------------------------------------------------
    suspend fun addConstraint(aId: Long, bId: Long) {
        if (aId != bId) constraintDao.insert(PlayerConstraintEntity(aId = aId, bId = bId))
    }

    suspend fun removeConstraint(id: Long) = constraintDao.deleteById(id)

    // --- Histórico --------------------------------------------------------
    private suspend fun recordSnapshot(playerId: Long, p: PlayerEntity) {
        historyDao.insert(
            PlayerHistoryEntity(
                playerId = playerId,
                saque = p.saque,
                recepcao = p.recepcao,
                levantamento = p.levantamento,
                corte = p.corte,
                movimentacao = p.movimentacao,
                total = p.total,
                recordedAt = clock()
            )
        )
    }

    private fun skillsChanged(old: PlayerEntity, new: PlayerEntity): Boolean =
        old.saque != new.saque || old.recepcao != new.recepcao ||
            old.levantamento != new.levantamento || old.corte != new.corte ||
            old.movimentacao != new.movimentacao
}
