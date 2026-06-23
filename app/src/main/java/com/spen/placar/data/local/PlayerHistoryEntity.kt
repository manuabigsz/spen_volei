package com.spen.placar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spen.placar.domain.SkillLevel

/**
 * Registro histórico das habilidades de um jogador em um instante — usado para
 * mostrar a evolução (melhoras/quedas) ao longo do tempo.
 */
@Entity(tableName = "player_history")
data class PlayerHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerId: Long,
    val saque: Int,
    val recepcao: Int,
    val levantamento: Int,
    val corte: Int,
    val movimentacao: Int,
    val total: Int,
    val recordedAt: Long
)

/** Níveis do snapshot na ordem de [com.spen.placar.domain.Skill]. */
val PlayerHistoryEntity.levels: List<SkillLevel>
    get() = listOf(
        SkillLevel.fromWeight(saque),
        SkillLevel.fromWeight(recepcao),
        SkillLevel.fromWeight(levantamento),
        SkillLevel.fromWeight(corte),
        SkillLevel.fromWeight(movimentacao)
    )

/** Pesos (1..3) na mesma ordem, para calcular variações. */
val PlayerHistoryEntity.weights: List<Int>
    get() = listOf(saque, recepcao, levantamento, corte, movimentacao)
