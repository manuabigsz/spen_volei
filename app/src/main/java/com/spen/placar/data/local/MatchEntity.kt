package com.spen.placar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Partida finalizada e persistida (histórico de partidas).
 */
@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamAName: String,
    val teamBName: String,
    val setsA: Int,
    val setsB: Int,
    val winnerName: String,
    /** Resumo dos sets, ex.: "25-20, 23-25, 25-18". */
    val scoreSummary: String,
    /** Duração da partida em milissegundos (cronômetro). */
    val durationMillis: Long,
    /** Instante de término (epoch millis). */
    val finishedAt: Long,
    /** Jogadores de cada time (nomes separados por vírgula), se houver elenco. */
    val playersA: String = "",
    val playersB: String = ""
)
