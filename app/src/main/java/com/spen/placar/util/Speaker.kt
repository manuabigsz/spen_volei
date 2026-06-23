package com.spen.placar.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Narração por voz usando o TextToSpeech do Android (voz do Google), em pt-BR.
 * Falha silenciosamente se o TTS não estiver disponível.
 */
class Speaker(context: Context) : TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            runCatching { tts.language = Locale("pt", "BR") }
            ready = true
        }
    }

    fun speak(text: String) {
        if (ready && text.isNotBlank()) {
            runCatching { tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "placar") }
        }
    }

    fun release() {
        runCatching { tts.stop(); tts.shutdown() }
    }
}
