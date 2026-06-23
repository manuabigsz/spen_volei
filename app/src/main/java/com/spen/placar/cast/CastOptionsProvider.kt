package com.spen.placar.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.spen.placar.R

/**
 * Configuração do Google Cast (Chromecast) para espelhar o placar na TV.
 *
 * Usa o Default Media Receiver durante o desenvolvimento. Para um receiver
 * personalizado (que renderiza o placar em tela cheia na TV), registre seu
 * App ID no Google Cast SDK Developer Console e substitua `cast_app_id`.
 */
class CastOptionsProvider : OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions {
        return CastOptions.Builder()
            .setReceiverApplicationId(context.getString(R.string.cast_app_id))
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? = null
}
