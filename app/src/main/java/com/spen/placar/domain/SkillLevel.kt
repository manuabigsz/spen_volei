package com.spen.placar.domain

import java.text.Normalizer

/**
 * Nível de uma habilidade do jogador.
 *
 * O [weight] é usado no balanceamento dos times (Básico=1 … Avançado=3).
 */
enum class SkillLevel(val weight: Int, val label: String) {
    BASICO(1, "Básico"),
    INTERMEDIARIO(2, "Intermediário"),
    AVANCADO(3, "Avançado");

    companion object {
        fun fromWeight(w: Int): SkillLevel = when {
            w >= 3 -> AVANCADO
            w == 2 -> INTERMEDIARIO
            else -> BASICO
        }

        /**
         * Interpreta o nível a partir de texto livre (planilha), tolerante a
         * acentos e abreviações: "Avançado/av/a", "Intermediário/inter/médio/m",
         * "Básico/b". Vazio → Básico.
         */
        fun parse(raw: String?): SkillLevel {
            val s = normalize(raw ?: "")
            return when {
                s.isEmpty() -> BASICO
                s.startsWith("av") || s == "a" || s == "3" -> AVANCADO
                s.startsWith("int") || s.startsWith("med") || s == "i" || s == "m" || s == "2" -> INTERMEDIARIO
                else -> BASICO
            }
        }

        /** Remove acentos e normaliza para minúsculas. */
        fun normalize(text: String): String =
            Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
                .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }
}

/** As cinco habilidades essenciais avaliadas em cada jogador. */
enum class Skill(val label: String, val short: String) {
    SAQUE("Saque", "SAQ"),
    RECEPCAO("Recepção", "REC"),
    LEVANTAMENTO("Levantamento", "LEV"),
    CORTE("Corte", "COR"),
    MOVIMENTACAO("Movimentação", "MOV")
}
