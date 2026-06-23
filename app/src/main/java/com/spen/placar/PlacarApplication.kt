package com.spen.placar

import android.app.Application
import com.spen.placar.data.local.PlacarDatabase
import com.spen.placar.data.prefs.SettingsRepository
import com.spen.placar.data.repository.MatchRepository

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

    override fun onCreate() {
        super.onCreate()
        val db = PlacarDatabase.get(this)
        matchRepository = MatchRepository(db.matchDao())
        settingsRepository = SettingsRepository(this)
    }
}
