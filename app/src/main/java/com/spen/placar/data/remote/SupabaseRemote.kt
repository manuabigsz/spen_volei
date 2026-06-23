package com.spen.placar.data.remote

import android.util.Log
import com.spen.placar.data.local.MatchEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.spen.placar.data.local.PlayerEntity
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.Instant

/**
 * Cliente mínimo do Supabase (PostgREST) para salvar partidas e times na nuvem.
 *
 * Usa apenas a **publishable key** (segura para o cliente) + as regras de
 * segurança (RLS) configuradas no banco. As operações são "best-effort":
 * falham silenciosamente quando offline, sem quebrar o app.
 */
class SupabaseRemote(
    private val baseUrl: String,
    private val anonKey: String
) {
    val isConfigured: Boolean = baseUrl.isNotBlank() && anonKey.isNotBlank()

    /** Salva uma partida finalizada. Retorna true em caso de sucesso. */
    suspend fun saveMatch(match: MatchEntity): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext false
        val body = JSONObject().apply {
            put("team_a", match.teamAName)
            put("team_b", match.teamBName)
            put("sets_a", match.setsA)
            put("sets_b", match.setsB)
            put("winner", match.winnerName)
            put("score_summary", match.scoreSummary)
            put("duration_millis", match.durationMillis)
            put("finished_at", Instant.ofEpochMilli(match.finishedAt).toString())
            if (match.playersA.isNotBlank()) put("players_a", namesArray(match.playersA))
            if (match.playersB.isNotBlank()) put("players_b", namesArray(match.playersB))
        }
        post("matches", body.toString())
    }

    /** Salva um sorteio de times. */
    suspend fun saveDraw(
        teams: List<List<String>>,
        bench: List<String>,
        title: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext false
        val teamsJson = JSONArray().apply {
            teams.forEach { team -> put(JSONArray(team)) }
        }
        val body = JSONObject().apply {
            put("team_count", teams.size)
            put("teams", teamsJson)
            put("bench", JSONArray(bench))
            if (title != null) put("title", title)
        }
        post("team_draws", body.toString())
    }

    /**
     * Insere ou atualiza jogadores (upsert por nome). Salvar jogadores na nuvem
     * permite reusá-los em vários dispositivos.
     */
    suspend fun savePlayers(players: List<PlayerEntity>): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured || players.isEmpty()) return@withContext false
        val arr = JSONArray()
        players.forEach { p ->
            arr.put(JSONObject().apply {
                put("name", p.name)
                put("saque", p.saque)
                put("recepcao", p.recepcao)
                put("levantamento", p.levantamento)
                put("corte", p.corte)
                put("movimentacao", p.movimentacao)
            })
        }
        // on_conflict=name + merge-duplicates → atualiza quando o nome já existe.
        post("players?on_conflict=name", arr.toString(), upsert = true)
    }

    suspend fun savePlayer(player: PlayerEntity): Boolean = savePlayers(listOf(player))

    /** Busca todos os jogadores salvos na nuvem. */
    suspend fun fetchPlayers(): List<PlayerEntity> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext emptyList()
        val body = get("players?select=name,saque,recepcao,levantamento,corte,movimentacao&order=name")
            ?: return@withContext emptyList()
        try {
            val arr = JSONArray(body)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                val name = o.optString("name").trim()
                if (name.isEmpty()) null
                else PlayerEntity(
                    name = name,
                    saque = o.optInt("saque", 1),
                    recepcao = o.optInt("recepcao", 1),
                    levantamento = o.optInt("levantamento", 1),
                    corte = o.optInt("corte", 1),
                    movimentacao = o.optInt("movimentacao", 1)
                )
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Erro ao ler jogadores da nuvem", t)
            emptyList()
        }
    }

    private fun get(pathAndQuery: String): String? {
        return try {
            val conn = (URL("$baseUrl/rest/v1/$pathAndQuery").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
                setRequestProperty("Accept", "application/json")
            }
            val code = conn.responseCode
            val text = if (code in 200..299) conn.inputStream.bufferedReader().use { it.readText() } else null
            if (text == null) {
                val err = runCatching { conn.errorStream?.bufferedReader()?.readText() }.getOrNull()
                Log.w(TAG, "GET $pathAndQuery falhou ($code): $err")
            }
            conn.disconnect()
            text
        } catch (t: Throwable) {
            Log.w(TAG, "Erro de rede em GET $pathAndQuery", t)
            null
        }
    }

    /** Remove um jogador da nuvem pelo nome. */
    suspend fun deletePlayerByName(name: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext false
        val encoded = URLEncoder.encode(name, "UTF-8")
        request("DELETE", "players?name=eq.$encoded", null)
    }

    private fun namesArray(csv: String): JSONArray =
        JSONArray(csv.split(",").map { it.trim() }.filter { it.isNotEmpty() })

    private fun post(table: String, json: String, upsert: Boolean = false): Boolean =
        request("POST", table, json, upsert)

    private fun request(method: String, pathAndQuery: String, json: String?, upsert: Boolean = false): Boolean {
        return try {
            val conn = (URL("$baseUrl/rest/v1/$pathAndQuery").openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
                setRequestProperty("Content-Type", "application/json")
                val prefer = if (upsert) "return=minimal,resolution=merge-duplicates" else "return=minimal"
                setRequestProperty("Prefer", prefer)
                if (json != null) doOutput = true
            }
            if (json != null) conn.outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            if (code !in 200..299) {
                val err = runCatching { conn.errorStream?.bufferedReader()?.readText() }.getOrNull()
                Log.w(TAG, "Falha em $method $pathAndQuery ($code): $err")
            }
            conn.disconnect()
            code in 200..299
        } catch (t: Throwable) {
            Log.w(TAG, "Erro de rede em $method $pathAndQuery", t)
            false
        }
    }

    private companion object {
        const val TAG = "SupabaseRemote"
    }
}
