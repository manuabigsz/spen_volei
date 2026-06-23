package com.spen.placar.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Acesso aos jogadores cadastrados. */
@Dao
interface PlayerDao {

    @Query("SELECT * FROM players ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getById(id: Long): PlayerEntity?

    @Query("SELECT * FROM players WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(player: PlayerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(players: List<PlayerEntity>)

    @Update
    suspend fun update(player: PlayerEntity)

    @Query("UPDATE players SET present = :present WHERE id = :id")
    suspend fun setPresent(id: Long, present: Boolean)

    @Query("UPDATE players SET present = :present")
    suspend fun setAllPresent(present: Boolean)

    @Query("DELETE FROM players WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM players WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM players")
    suspend fun clear()
}
