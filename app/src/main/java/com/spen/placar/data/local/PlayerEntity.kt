package com.spen.placar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spen.placar.domain.SkillLevel

/**
 * Jogador cadastrado, com as 5 habilidades essenciais (pesos 1..3).
 */
@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val saque: Int,
    val recepcao: Int,
    val levantamento: Int,
    val corte: Int,
    val movimentacao: Int,
    /** Marcado como presente para o sorteio de times. */
    val present: Boolean = true
)

/** Soma dos níveis (5..15) — usada no balanceamento. */
val PlayerEntity.total: Int
    get() = saque + recepcao + levantamento + corte + movimentacao

/** Níveis por habilidade, na ordem de [com.spen.placar.domain.Skill]. */
val PlayerEntity.levels: List<SkillLevel>
    get() = listOf(
        SkillLevel.fromWeight(saque),
        SkillLevel.fromWeight(recepcao),
        SkillLevel.fromWeight(levantamento),
        SkillLevel.fromWeight(corte),
        SkillLevel.fromWeight(movimentacao)
    )
