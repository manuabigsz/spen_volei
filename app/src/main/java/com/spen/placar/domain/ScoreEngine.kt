package com.spen.placar.domain

/**
 * Motor puro de regras do vôlei.
 *
 * Recebe um [MatchState] e devolve um novo estado, sem efeitos colaterais.
 * Isso mantém a regra de negócio testável e independente da UI/Android.
 */
object ScoreEngine {

    /**
     * Aplica +1 ponto para o lado informado, encerrando set/partida quando
     * as regras forem satisfeitas.
     *
     * Retorna o mesmo estado se a partida já estiver encerrada.
     */
    fun addPoint(state: MatchState, side: TeamSide): MatchState {
        if (state.finished) return state

        val newA = if (side == TeamSide.A) state.pointsA + 1 else state.pointsA
        val newB = if (side == TeamSide.B) state.pointsB + 1 else state.pointsB

        val intermediate = state.copy(pointsA = newA, pointsB = newB)

        val setWinner = resolveSetWinner(intermediate)
            ?: return intermediate   // set continua em andamento

        return closeSet(intermediate, setWinner)
    }

    /** Determina o vencedor do set atual, ou null se ainda não terminou. */
    private fun resolveSetWinner(state: MatchState): TeamSide? {
        val target = state.targetPoints
        val (a, b) = state.pointsA to state.pointsB
        return when {
            a >= target && a - b >= state.config.minLead -> TeamSide.A
            b >= target && b - a >= state.config.minLead -> TeamSide.B
            else -> null
        }
    }

    /** Encerra o set vencido, atualiza placar de sets e verifica fim de jogo. */
    private fun closeSet(state: MatchState, winner: TeamSide): MatchState {
        val result = SetResult(
            setNumber = state.currentSet,
            pointsA = state.pointsA,
            pointsB = state.pointsB,
            winner = winner
        )

        val setsA = state.setsA + if (winner == TeamSide.A) 1 else 0
        val setsB = state.setsB + if (winner == TeamSide.B) 1 else 0

        val matchWinner = when {
            setsA == state.config.setsToWin -> TeamSide.A
            setsB == state.config.setsToWin -> TeamSide.B
            else -> null
        }

        return state.copy(
            setsA = setsA,
            setsB = setsB,
            completedSets = state.completedSets + result,
            // Mantém o placar final visível se a partida acabou;
            // caso contrário zera para o próximo set.
            pointsA = if (matchWinner != null) state.pointsA else 0,
            pointsB = if (matchWinner != null) state.pointsB else 0,
            currentSet = if (matchWinner != null) state.currentSet else state.currentSet + 1,
            finished = matchWinner != null,
            winner = matchWinner
        )
    }

    /** Indica se o ponto recém-aplicado encerrou um set (para o histórico). */
    fun didCloseSet(before: MatchState, after: MatchState): Boolean =
        after.completedSets.size > before.completedSets.size
}
