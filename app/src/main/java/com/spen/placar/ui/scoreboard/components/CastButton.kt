package com.spen.placar.ui.scoreboard.components

import android.view.ContextThemeWrapper
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory

/**
 * Botão de Chromecast (MediaRouteButton) embutido na barra superior.
 * Permite ao usuário escolher uma TV para espelhar o placar.
 *
 * O [MediaRouteButton] exige um tema **AppCompat**; como o app usa um tema de
 * framework + Compose, envolvemos o contexto em um [ContextThemeWrapper] com
 * Theme.AppCompat. Toda a criação é protegida para nunca derrubar o app caso
 * o Google Play Services / Cast não esteja disponível no dispositivo.
 */
@Composable
fun CastButton(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            runCatching {
                val themed = ContextThemeWrapper(
                    context,
                    androidx.appcompat.R.style.Theme_AppCompat_DayNight
                )
                MediaRouteButton(themed).apply {
                    CastButtonFactory.setUpMediaRouteButton(context.applicationContext, this)
                }
            }.getOrElse {
                // Fallback invisível: o app continua funcionando sem o botão de Cast.
                View(context)
            }
        }
    )
}
