package com.spen.placar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.google.android.gms.cast.framework.CastContext
import com.spen.placar.spen.SpenManager
import com.spen.placar.ui.PlacarApp
import com.spen.placar.ui.scoreboard.ScoreboardViewModel
import androidx.compose.runtime.mutableStateOf

/**
 * Activity única que hospeda toda a UI em Compose.
 *
 * Responsável por:
 *  - instanciar o [ScoreboardViewModel] com suas dependências;
 *  - inicializar o Google Cast (Chromecast);
 *  - conectar e direcionar os comandos da S Pen ao ViewModel.
 */
class MainActivity : ComponentActivity() {

    private val app by lazy { application as PlacarApplication }

    private val viewModel: ScoreboardViewModel by viewModels {
        ScoreboardViewModel.Factory(app.matchRepository, app.settingsRepository)
    }

    private lateinit var spenManager: SpenManager
    private val spenAvailable = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializa o Cast de forma resiliente (Play Services pode faltar).
        runCatching { CastContext.getSharedInstance(this) }

        // S Pen: cada comando reconhecido é encaminhado ao ViewModel.
        spenManager = SpenManager(
            onAvailabilityChanged = { available -> spenAvailable.value = available },
            onDebug = { msg -> viewModel.addSpenDebug(msg) },
            onAction = { action -> viewModel.onSpenAction(action) }
        )
        // O SDK exige um contexto de Activity — passamos a própria Activity.
        spenManager.connect(this)

        setContent {
            PlacarApp(viewModel = viewModel, spenAvailable = spenAvailable.value)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        spenManager.disconnect()
    }
}
