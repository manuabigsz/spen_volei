package com.spen.placar.domain

import kotlin.random.Random

/** Resultado do sorteio: times balanceados e jogadores no rodízio (reservas). */
data class BalancedTeams<T>(
    val teams: List<List<T>>,
    val bench: List<T>
)

/**
 * Distribui os jogadores em [teamCount] times **balanceados** pelo nível total.
 *
 * Estratégia LPT (Longest Processing Time): ordena do mais forte para o mais
 * fraco e coloca cada jogador no time atualmente mais fraco que ainda tem vaga.
 * Isso aproxima ao máximo a soma de níveis entre os times.
 *
 * Os times são divididos **igualmente**: cada um recebe `floor(N / teamCount)`
 * jogadores; o que sobrar vai para o rodízio ([BalancedTeams.bench]).
 *
 * @param weight função que devolve o nível total do jogador.
 * @param separated devolve true quando dois jogadores NÃO podem ficar no mesmo
 *   time. O algoritmo respeita a restrição sempre que possível.
 * @param seed semente opcional (testes); em produção, varia a cada sorteio.
 */
fun <T> balanceTeams(
    players: List<T>,
    teamCount: Int,
    seed: Long? = null,
    weight: (T) -> Int,
    separated: (T, T) -> Boolean = { _, _ -> false }
): BalancedTeams<T> {
    require(teamCount >= 2) { "É preciso pelo menos 2 times." }
    if (players.isEmpty()) return BalancedTeams(List(teamCount) { emptyList() }, emptyList())

    val perTeam = players.size / teamCount
    if (perTeam == 0) {
        // Menos jogadores que times: cada um vira um time de 1, sem rodízio.
        val shuffled = players.shuffled(seedRandom(seed))
        return BalancedTeams(shuffled.map { listOf(it) }, emptyList())
    }

    // Embaralha antes para variar empates de mesmo nível, depois ordena desc.
    val ordered = players.shuffled(seedRandom(seed)).sortedByDescending { weight(it) }

    val teams = MutableList(teamCount) { mutableListOf<T>() }
    val sums = IntArray(teamCount)
    val bench = mutableListOf<T>()

    for (p in ordered) {
        // 1ª tentativa: time com vaga, menor soma E sem conflito de restrição.
        var best = pickTeam(teams, sums, perTeam) { team ->
            team.none { separated(p, it) }
        }
        // 2ª tentativa (fallback): ignora a restrição se não houver outra opção.
        if (best == -1) best = pickTeam(teams, sums, perTeam) { true }

        if (best == -1) bench.add(p) else {
            teams[best].add(p)
            sums[best] += weight(p)
        }
    }
    return BalancedTeams(teams, bench)
}

/** Escolhe o time com vaga, menor soma atual e que satisfaça [allowed]. */
private fun <T> pickTeam(
    teams: List<List<T>>,
    sums: IntArray,
    perTeam: Int,
    allowed: (List<T>) -> Boolean
): Int {
    var best = -1
    for (i in teams.indices) {
        if (teams[i].size < perTeam && allowed(teams[i]) && (best == -1 || sums[i] < sums[best])) {
            best = i
        }
    }
    return best
}

private fun seedRandom(seed: Long?): Random = if (seed != null) Random(seed) else Random.Default
