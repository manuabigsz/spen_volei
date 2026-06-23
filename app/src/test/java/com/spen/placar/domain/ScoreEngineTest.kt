package com.spen.placar.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testes das regras de pontuação do vôlei. */
class ScoreEngineTest {

    private fun scoreTo(state: MatchState, side: TeamSide, times: Int): MatchState {
        var s = state
        repeat(times) { s = ScoreEngine.addPoint(s, side) }
        return s
    }

    @Test
    fun `set normal exige 25 pontos`() {
        var s = MatchState()
        s = scoreTo(s, TeamSide.A, 24)
        s = scoreTo(s, TeamSide.B, 20)
        assertEquals(0, s.setsA)        // ainda não fechou
        s = ScoreEngine.addPoint(s, TeamSide.A)  // 25-20
        assertEquals(1, s.setsA)
        assertEquals(0, s.pointsA)      // zerou para o próximo set
        assertEquals(2, s.currentSet)
    }

    @Test
    fun `set exige diferenca minima de 2 pontos`() {
        var s = MatchState()
        s = scoreTo(s, TeamSide.A, 24)
        s = scoreTo(s, TeamSide.B, 24)  // 24-24
        s = ScoreEngine.addPoint(s, TeamSide.A) // 25-24, não fecha
        assertEquals(0, s.setsA)
        s = ScoreEngine.addPoint(s, TeamSide.A) // 26-24, fecha
        assertEquals(1, s.setsA)
    }

    @Test
    fun `tie-break vai ate 15`() {
        var s = MatchState()
        // leva ambos a 2 sets cada
        s = scoreTo(s, TeamSide.A, 25); s = resetIfNeeded(s)
        s = scoreTo(s, TeamSide.A, 25); s = resetIfNeeded(s)
        s = scoreTo(s, TeamSide.B, 25); s = resetIfNeeded(s)
        s = scoreTo(s, TeamSide.B, 25); s = resetIfNeeded(s)
        assertTrue(s.isTieBreak)
        assertEquals(15, s.targetPoints)
        s = scoreTo(s, TeamSide.A, 15)
        assertTrue(s.finished)
        assertEquals(TeamSide.A, s.winner)
    }

    @Test
    fun `partida termina em melhor de 5`() {
        var s = MatchState()
        repeat(3) { s = scoreTo(s, TeamSide.A, 25) }
        assertTrue(s.finished)
        assertEquals(TeamSide.A, s.winner)
        assertEquals(3, s.setsA)
        // pontos após fim não alteram
        val after = ScoreEngine.addPoint(s, TeamSide.B)
        assertEquals(s, after)
    }

    @Test
    fun `set nao fecha antes do alvo`() {
        var s = MatchState()
        s = scoreTo(s, TeamSide.A, 10)
        assertFalse(s.finished)
        assertNull(s.winner)
        assertEquals(0, s.setsA)
    }

    // Helper: como addPoint já zera o set ao fechar, este helper é só
    // semântico para deixar o teste de tie-break legível.
    private fun resetIfNeeded(s: MatchState) = s
}
