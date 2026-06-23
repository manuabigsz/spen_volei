package com.spen.placar.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Acesso ao histórico de habilidades dos jogadores. */
@Dao
interface PlayerHistoryDao {

    @Insert
    suspend fun insert(entry: PlayerHistoryEntity)

    @Query("SELECT * FROM player_history WHERE playerId = :playerId ORDER BY recordedAt DESC")
    fun observeForPlayer(playerId: Long): Flow<List<PlayerHistoryEntity>>

    @Query("DELETE FROM player_history WHERE playerId = :playerId")
    suspend fun deleteForPlayer(playerId: Long)
}
