package com.spen.placar.ui.players

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Trava de visualização das habilidades/pontos dos jogadores.
 *
 * Estado em memória (reinicia ao fechar o app): por padrão fica **bloqueado**;
 * só quem tem a senha consegue revelar os níveis, totais e a evolução.
 */
object SkillGate {

    private const val PASSWORD = "manu@Volei"

    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked

    /** Tenta desbloquear com a senha. Retorna true se correta. */
    fun tryUnlock(input: String): Boolean {
        val ok = input == PASSWORD
        if (ok) _unlocked.value = true
        return ok
    }

    fun lock() {
        _unlocked.value = false
    }
}
