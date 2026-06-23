package com.spen.placar.util

/** Um áudio disponível em res/raw. */
data class RawSound(val name: String, val resId: Int)

/**
 * Descobre dinamicamente os áudios colocados em `res/raw/` (via reflexão em
 * `R.raw`). Assim, qualquer arquivo que você adicionar vira uma opção no app —
 * sem precisar mexer no código.
 *
 * Nomes reservados para usos específicos:
 *  - `apito`  → botão de apito
 *  - `ace`    → ponto de ace (long-press)
 */
object SoundCatalog {

    const val APITO = "apito"
    const val ACE = "ace"
    const val SET_POINT = "set_point"
    const val MATCH_POINT = "match_point"
    const val SET_VENCIDO = "set_vencido"
    const val VITORIA = "vitoria"

    private val RESERVED = setOf(APITO, ACE, SET_POINT, MATCH_POINT, SET_VENCIDO, VITORIA)

    /** Todos os áudios em res/raw (lido por reflexão; compila sem arquivos). */
    fun all(): List<RawSound> =
        runCatching {
            val rawClass = Class.forName("com.spen.placar.R\$raw")
            rawClass.fields.mapNotNull { f ->
                val id = f.getInt(null)
                if (id != 0) RawSound(f.name, id) else null
            }.sortedBy { it.name }
        }.getOrDefault(emptyList())

    /** Áudios oferecidos como "som ao marcar ponto" (exclui os reservados). */
    fun pointPresets(): List<RawSound> =
        all().filter { it.name !in RESERVED }

    fun resId(name: String): Int =
        all().firstOrNull { it.name == name }?.resId ?: 0
}
