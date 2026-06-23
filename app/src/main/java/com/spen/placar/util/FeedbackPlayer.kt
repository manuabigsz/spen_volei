package com.spen.placar.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.spen.placar.domain.TeamSide
import com.spen.placar.ui.scoreboard.MatchEffect

/**
 * Toca sons e aciona a vibração em resposta aos eventos da partida,
 * respeitando as preferências do usuário.
 */
class FeedbackPlayer(context: Context) {

    private val appContext = context.applicationContext

    private val toneGenerator: ToneGenerator? = runCatching {
        ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    }.getOrNull()

    // Reproduz os áudios de res/raw (carregados uma vez).
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // nome do áudio -> id no SoundPool (pré-carregados).
    private val soundIds: Map<String, Int> = runCatching {
        SoundCatalog.all().associate { it.name to soundPool.load(appContext, it.resId, 1) }
    }.getOrDefault(emptyMap())

    private val vibrator: Vibrator? = run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /** Reage a um efeito da partida com som e/ou vibração. */
    fun handle(
        effect: MatchEffect,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        pointSoundA: String = "padrao",
        pointSoundB: String = "padrao"
    ) {
        when (effect) {
            is MatchEffect.PointScored -> {
                if (soundEnabled) {
                    val s = if (effect.side == TeamSide.A) pointSoundA else pointSoundB
                    playPointSound(s, effect.side)
                }
                if (vibrationEnabled) vibrate(30)
            }
            is MatchEffect.Ace -> {
                // Som dedicado de ace (cai no som do ponto se não houver "ace").
                if (soundEnabled) {
                    val s = if (effect.side == TeamSide.A) pointSoundA else pointSoundB
                    if (!playRaw(SoundCatalog.ACE)) playPointSound(s, effect.side)
                }
                if (vibrationEnabled) vibratePattern(longArrayOf(0, 40, 40, 80))
            }
            is MatchEffect.Undone -> {
                if (soundEnabled) tone(ToneGenerator.TONE_PROP_NACK, 120)
                if (vibrationEnabled) vibrate(20)
            }
            is MatchEffect.SetWon -> {
                if (soundEnabled && !playRaw(SoundCatalog.SET_VENCIDO)) tone(ToneGenerator.TONE_PROP_ACK, 200)
                if (vibrationEnabled) vibratePattern(longArrayOf(0, 60, 60, 60))
            }
            is MatchEffect.MatchWon -> {
                if (soundEnabled && !playRaw(SoundCatalog.VITORIA)) tone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
                if (vibrationEnabled) vibratePattern(longArrayOf(0, 100, 80, 100, 80, 200))
            }
            is MatchEffect.SetPoint -> {
                // Toca o áudio reservado de set/match point, se existir.
                if (soundEnabled) {
                    playRaw(if (effect.isMatchPoint) SoundCatalog.MATCH_POINT else SoundCatalog.SET_POINT)
                }
            }
            is MatchEffect.Announce -> { /* tratado pela narração por voz */ }
        }
    }

    /** Toca o apito (se houver um áudio chamado "apito" em res/raw). */
    fun playWhistle(): Boolean = playRaw(SoundCatalog.APITO)

    /** Toca o som de ace (botão dedicado, sem somar ponto). */
    fun playAce(): Boolean = playRaw(SoundCatalog.ACE)

    private fun playPointSound(pointSound: String, side: TeamSide) {
        when (pointSound) {
            "nenhum" -> { /* silêncio */ }
            "padrao" -> {
                // Tom distinto por equipe (grave para A, agudo para B).
                val toneType = if (side == TeamSide.A) ToneGenerator.TONE_DTMF_1 else ToneGenerator.TONE_DTMF_9
                tone(toneType, 140)
            }
            else -> if (!playRaw(pointSound)) {
                // Áudio escolhido sumiu: cai no tom padrão.
                tone(ToneGenerator.TONE_PROP_BEEP, 120)
            }
        }
    }

    /** Toca um áudio de res/raw pelo nome. Retorna false se não existir. */
    private fun playRaw(name: String): Boolean {
        val id = soundIds[name] ?: return false
        return runCatching { soundPool.play(id, 1f, 1f, 1, 0, 1f); true }.getOrDefault(false)
    }

    private fun tone(type: Int, durationMs: Int) {
        runCatching { toneGenerator?.startTone(type, durationMs) }
    }

    private fun vibrate(ms: Long) {
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") v.vibrate(ms)
        }
    }

    private fun vibratePattern(pattern: LongArray) {
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION") v.vibrate(pattern, -1)
        }
    }

    fun release() {
        runCatching { toneGenerator?.release() }
        runCatching { soundPool.release() }
    }
}
