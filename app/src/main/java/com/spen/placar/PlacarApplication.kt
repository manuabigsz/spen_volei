package com.spen.placar

import android.app.Application
import com.spen.placar.data.local.PlacarDatabase
import com.spen.placar.data.prefs.SettingsRepository
import com.spen.placar.data.repository.MatchRepository
import com.spen.placar.data.repository.PlayerRepository

/**
 * Application — container simples de dependências (service locator).
 *
 * Para um app deste porte, manter as dependências aqui evita o overhead de um
 * framework de DI mantendo a testabilidade da arquitetura MVVM.
 */
class PlacarApplication : Application() {

    lateinit var matchRepository: MatchRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var playerRepository: PlayerRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = PlacarDatabase.get(this)
        matchRepository = MatchRepository(db.matchDao())
        playerRepository = PlayerRepository(db.playerDao(), db.constraintDao())
        settingsRepository = SettingsRepository(this)
    }
}
