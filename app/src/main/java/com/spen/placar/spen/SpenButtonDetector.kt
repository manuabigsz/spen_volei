package com.spen.placar.spen

import android.os.Handler
import android.os.Looper
import com.spen.placar.ui.scoreboard.SpenAction

/**
 * Detecta os padrões de uso do botão da S Pen a partir de eventos brutos
 * de pressionar/soltar, traduzindo-os em [SpenAction]:
 *
 *  - Clique simples  -> [SpenAction.POINT_A]
 *  - Clique duplo    -> [SpenAction.POINT_B]
 *  - Pressionar/segurar -> [SpenAction.UNDO]
 *
 * A lógica de temporização fica isolada aqui para manter o [SpenManager]
 * focado apenas na ligação com o SDK da Samsung.
 *
 * @param longPressMs tempo segurando o botão para acionar "desfazer".
 * @param doubleClickMs janela máxima entre dois cliques para contar como duplo.
 */
class SpenButtonDetector(
    private val longPressMs: Long = 500,
    private val doubleClickMs: Long = 280,
    private val handler: Handler = Handler(Looper.getMainLooper()),
    private val onAction: (SpenAction) -> Unit
) {
    private var longPressConsumed = false
    private var pendingSingle: Runnable? = null

    private val longPressRunnable = Runnable {
        longPressConsumed = true
        onAction(SpenAction.UNDO)
    }

    /** Chamar quando o botão for pressionado (ACTION_DOWN). */
    fun onButtonDown() {
        longPressConsumed = false
        handler.postDelayed(longPressRunnable, longPressMs)
    }

    /** Chamar quando o botão for solto (ACTION_UP). */
    fun onButtonUp() {
        handler.removeCallbacks(longPressRunnable)

        // Se já foi tratado como pressionar-e-segurar, ignora o "soltar".
        if (longPressConsumed) {
            longPressConsumed = false
            return
        }

        val previous = pendingSingle
        if (previous != null) {
            // Segundo clique dentro da janela -> clique duplo.
            handler.removeCallbacks(previous)
            pendingSingle = null
            onAction(SpenAction.POINT_B)
        } else {
            // Primeiro clique: aguarda para ver se vira clique duplo.
            val single = Runnable {
                pendingSingle = null
                onAction(SpenAction.POINT_A)
            }
            pendingSingle = single
            handler.postDelayed(single, doubleClickMs)
        }
    }

    /** Cancela quaisquer temporizadores pendentes (ex.: ao desconectar). */
    fun cancel() {
        handler.removeCallbacks(longPressRunnable)
        pendingSingle?.let { handler.removeCallbacks(it) }
        pendingSingle = null
        longPressConsumed = false
    }
}
