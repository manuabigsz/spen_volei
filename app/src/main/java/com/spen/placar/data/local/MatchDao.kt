package com.spen.placar.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Acesso ao histórico de partidas no Room. */
@Dao
interface MatchDao {

    @Insert
    suspend fun insert(match: MatchEntity): Long

    @Query("SELECT * FROM matches ORDER BY finishedAt DESC")
    fun observeAll(): Flow<List<MatchEntity>>

    @Query("DELETE FROM matches WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM matches")
    suspend fun clear()
}
