package com.spen.placar.domain

/**
 * Modelos de domínio do placar de vôlei.
 *
 * Toda a lógica de pontuação é mantida pura (sem dependências de Android),
 * facilitando testes unitários e mantendo a arquitetura MVVM limpa.
 */

/** Identifica cada equipe. */
enum class TeamSide { A, B }

/**
 * Configuração das regras da partida.
 *
 * Padrão oficial (CBV/FIVB): melhor de 5 sets, 25 pontos por set com
 * diferença mínima de 2; set decisivo (tie-break) até 15 pontos.
 */
data class MatchConfig(
    val pointsPerSet: Int = 25,
    val tieBreakPoints: Int = 15,
    val minLead: Int = 2,
    val setsToWin: Int = 3,   // vence quem fechar 3 sets primeiro
    val maxSets: Int = 5
)

/** Resultado de um set já encerrado (usado no histórico). */
data class SetResult(
    val setNumber: Int,
    val pointsA: Int,
    val pointsB: Int,
    val winner: TeamSide
)

/**
 * Estado completo e imutável de uma partida.
 *
 * Cada alteração produz uma nova instância — isso permite manter uma pilha
 * de snapshots para implementar "desfazer" de forma trivial e segura.
 */
data class MatchState(
    val config: MatchConfig = MatchConfig(),
    val teamAName: String = "Time A",
    val teamBName: String = "Time B",
    val pointsA: Int = 0,            // pontos no set em andamento
    val pointsB: Int = 0,
    val setsA: Int = 0,             // sets vencidos no total
    val setsB: Int = 0,
    val currentSet: Int = 1,
    val finished: Boolean = false,
    val winner: TeamSide? = null,
    val completedSets: List<SetResult> = emptyList()
) {
    /** É o set decisivo (tie-break)? Ocorre quando ambos têm setsToWin-1 sets. */
    val isTieBreak: Boolean
        get() = setsA == config.setsToWin - 1 && setsB == config.setsToWin - 1

    /** Alvo de pontos do set atual (15 no tie-break, 25 nos demais). */
    val targetPoints: Int
        get() = if (isTieBreak) config.tieBreakPoints else config.pointsPerSet
}

/**
 * Entrada do histórico de pontos exibido na tela e persistido.
 */
data class ScoreEvent(
    val side: TeamSide,
    val setNumber: Int,
    val pointsAAfter: Int,
    val pointsBAfter: Int,
    val timestamp: Long,
    /** true quando este ponto encerrou um set. */
    val closedSet: Boolean = false
)
