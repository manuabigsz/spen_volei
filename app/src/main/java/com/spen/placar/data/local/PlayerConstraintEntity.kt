package com.spen.placar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Restrição de sorteio: os jogadores [aId] e [bId] **não podem** ficar no
 * mesmo time.
 */
@Entity(tableName = "player_constraints")
data class PlayerConstraintEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val aId: Long,
    val bId: Long
)
