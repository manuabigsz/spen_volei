package com.spen.placar.util

import com.spen.placar.data.local.PlayerEntity
import com.spen.placar.domain.SkillLevel

/**
 * Importador de jogadores a partir de uma planilha CSV.
 *
 * Colunas esperadas (com ou sem cabeçalho, em qualquer ordem se houver
 * cabeçalho reconhecível): JOGADOR, SAQUE, RECEPÇÃO, LEVANTAMENTO, CORTE,
 * MOVIMENTAÇÃO. Aceita separador `,` ou `;` e níveis por extenso/abreviados
 * (Avançado / Intermediário / Básico).
 */
object CsvPlayers {

    fun parse(content: String): List<PlayerEntity> {
        val lines = content.split(Regex("\r?\n")).map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return emptyList()

        val delimiter = if (lines.first().count { it == ';' } >= lines.first().count { it == ',' }) ';' else ','

        // Detecta cabeçalho e a posição de cada coluna.
        val firstCells = splitLine(lines.first(), delimiter)
        val hasHeader = firstCells.any { SkillLevel.normalize(it).let { c ->
            c.startsWith("jogador") || c.startsWith("nome") || c.startsWith("saque")
        } }

        val mapping = if (hasHeader) headerMapping(firstCells) else defaultMapping()
        val dataLines = if (hasHeader) lines.drop(1) else lines

        return dataLines.mapNotNull { line ->
            val cells = splitLine(line, delimiter)
            val name = cells.getOrNull(mapping.name)?.trim().orEmpty()
            if (name.isEmpty()) return@mapNotNull null
            PlayerEntity(
                name = name,
                saque = SkillLevel.parse(cells.getOrNull(mapping.saque)).weight,
                recepcao = SkillLevel.parse(cells.getOrNull(mapping.recepcao)).weight,
                levantamento = SkillLevel.parse(cells.getOrNull(mapping.levantamento)).weight,
                corte = SkillLevel.parse(cells.getOrNull(mapping.corte)).weight,
                movimentacao = SkillLevel.parse(cells.getOrNull(mapping.movimentacao)).weight
            )
        }
    }

    private data class ColMap(
        val name: Int, val saque: Int, val recepcao: Int,
        val levantamento: Int, val corte: Int, val movimentacao: Int
    )

    private fun defaultMapping() = ColMap(0, 1, 2, 3, 4, 5)

    private fun headerMapping(header: List<String>): ColMap {
        fun find(vararg keys: String, default: Int): Int {
            val idx = header.indexOfFirst { cell ->
                val c = SkillLevel.normalize(cell)
                keys.any { c.startsWith(it) }
            }
            return if (idx >= 0) idx else default
        }
        return ColMap(
            name = find("jogador", "nome", default = 0),
            saque = find("saque", default = 1),
            recepcao = find("recep", default = 2),
            levantamento = find("levant", default = 3),
            corte = find("corte", default = 4),
            movimentacao = find("movim", default = 5)
        )
    }

    /** Divisão simples de CSV com suporte a campos entre aspas. */
    private fun splitLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == delimiter && !inQuotes -> { result.add(sb.toString()); sb.clear() }
                else -> sb.append(ch)
            }
        }
        result.add(sb.toString())
        return result.map { it.trim().trim('"') }
    }
}
