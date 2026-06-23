package com.spen.placar.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class TeamBalancerTest {

    private fun players(vararg totals: Int) = totals.toList()

    @Test
    fun `divide igualmente e manda resto para o rodizio`() {
        val result = balanceTeams(players(15, 14, 13, 12, 11), teamCount = 2, seed = 1) { it }
        assertEquals(2, result.teams.size)
        assertEquals(2, result.teams[0].size)
        assertEquals(2, result.teams[1].size)
        assertEquals(1, result.bench.size)   // 5 jogadores, 2 por time, 1 rodízio
    }

    @Test
    fun `times ficam balanceados em nivel`() {
        val totals = players(15, 14, 13, 12, 11, 10, 9, 8)
        val result = balanceTeams(totals, teamCount = 2, seed = 42) { it }
        val s0 = result.teams[0].sum()
        val s1 = result.teams[1].sum()
        // diferença pequena entre os times
        assertTrue("Diferença grande: $s0 vs $s1", abs(s0 - s1) <= 2)
    }

    @Test
    fun `suporta 14 jogadores em 2 times de 7`() {
        val totals = (1..14).map { 5 + it % 11 }
        val result = balanceTeams(totals, teamCount = 2, seed = 7) { it }
        assertEquals(7, result.teams[0].size)
        assertEquals(7, result.teams[1].size)
        assertTrue(result.bench.isEmpty())
    }
}
