package com.spen.placar.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Opções de tema disponíveis. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Preferências do usuário (estado observável). */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val spenEnabled: Boolean = true
)

// DataStore singleton vinculado ao Context.
private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Persiste as preferências do usuário com Jetpack DataStore.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val SOUND = booleanPreferencesKey("sound_enabled")
        val VIBRATION = booleanPreferencesKey("vibration_enabled")
        val SPEN = booleanPreferencesKey("spen_enabled")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeMode = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            soundEnabled = p[Keys.SOUND] ?: true,
            vibrationEnabled = p[Keys.VIBRATION] ?: true,
            spenEnabled = p[Keys.SPEN] ?: true
        )
    }

    suspend fun setTheme(mode: ThemeMode) =
        context.dataStore.edit { it[Keys.THEME] = mode.name }

    suspend fun setSound(enabled: Boolean) =
        context.dataStore.edit { it[Keys.SOUND] = enabled }

    suspend fun setVibration(enabled: Boolean) =
        context.dataStore.edit { it[Keys.VIBRATION] = enabled }

    suspend fun setSpen(enabled: Boolean) =
        context.dataStore.edit { it[Keys.SPEN] = enabled }
}
