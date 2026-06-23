package com.spen.placar.util

import android.content.Context
import android.media.AudioManager
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
    fun handle(effect: MatchEffect, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        when (effect) {
            is MatchEffect.PointScored -> {
                // Som distinto para cada equipe (tom grave para A, agudo para B).
                if (soundEnabled) {
                    val toneType = if (effect.side == TeamSide.A) {
                        ToneGenerator.TONE_DTMF_1   // mais grave
                    } else {
                        ToneGenerator.TONE_DTMF_9   // mais agudo
                    }
                    tone(toneType, 140)
                }
                if (vibrationEnabled) vibrate(30)
            }
            is MatchEffect.Undone -> {
                if (soundEnabled) tone(ToneGenerator.TONE_PROP_NACK, 120)
                if (vibrationEnabled) vibrate(20)
            }
            is MatchEffect.SetWon -> {
                if (soundEnabled) tone(ToneGenerator.TONE_PROP_ACK, 200)
                if (vibrationEnabled) vibratePattern(longArrayOf(0, 60, 60, 60))
            }
            is MatchEffect.MatchWon -> {
                if (soundEnabled) tone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
                if (vibrationEnabled) vibratePattern(longArrayOf(0, 100, 80, 100, 80, 200))
            }
        }
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
    }
}
