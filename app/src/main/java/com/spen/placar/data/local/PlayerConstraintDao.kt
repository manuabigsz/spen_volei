package com.spen.placar.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Acesso às restrições de sorteio (pares que não podem ficar juntos). */
@Dao
interface PlayerConstraintDao {

    @Query("SELECT * FROM player_constraints")
    fun observeAll(): Flow<List<PlayerConstraintEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(constraint: PlayerConstraintEntity): Long

    @Query("DELETE FROM player_constraints WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Remove restrições que envolvam um jogador específico (ao excluí-lo). */
    @Query("DELETE FROM player_constraints WHERE aId = :playerId OR bId = :playerId")
    suspend fun deleteForPlayer(playerId: Long)

    @Query("DELETE FROM player_constraints")
    suspend fun clear()
}
