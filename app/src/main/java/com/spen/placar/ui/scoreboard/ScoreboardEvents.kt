package com.spen.placar.ui.scoreboard

import com.spen.placar.domain.TeamSide

/** Efeitos pontuais (one-shot) emitidos pelo ViewModel para a UI. */
sealed interface MatchEffect {
    data class PointScored(val side: TeamSide) : MatchEffect
    data object Undone : MatchEffect
    data class SetWon(val side: TeamSide, val setNumber: Int) : MatchEffect
    data class MatchWon(val side: TeamSide) : MatchEffect
}

/** Tipos de comando reconhecidos a partir da S Pen. */
enum class SpenAction { POINT_A, POINT_B, UNDO }

/**
 * Feedback visual transitório de um comando da S Pen, exibido como overlay.
 */
data class SpenFeedback(
    val action: SpenAction,
    val message: String,
    /** Token único para reacionar a animação a cada novo comando. */
    val token: Long
)
