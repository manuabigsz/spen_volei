package com.spen.placar.spen

import android.content.Context
import android.util.Log
import com.spen.placar.ui.scoreboard.SpenAction
import java.lang.reflect.Proxy

/**
 * Faz a ponte com o **Samsung S Pen Remote SDK** para receber os eventos do
 * botão da S Pen (Galaxy S24 Ultra e similares) e convertê-los em comandos.
 *
 * ### Por que reflexão?
 * O Pen Remote SDK é distribuído como um AAR proprietário da Samsung
 * (`com.samsung.android.sdk.penremote`). Para que o projeto **compile e rode
 * em qualquer dispositivo** mesmo sem esse AAR, a integração é feita por
 * reflexão: se o SDK estiver presente em tempo de execução, o controle por
 * S Pen é ativado; caso contrário, [isAvailable] permanece `false` e o app
 * funciona normalmente pelos botões na tela.
 *
 * Para habilitar de fato no dispositivo, adicione o AAR do SDK em `app/libs/`
 * (veja o README). As chamadas abaixo espelham a API oficial:
 *
 * ```
 * SpenRemote.getInstance()
 *   .connect(context, ConnectionResultCallback { manager -> ... })
 * manager.registerSpenEventListener({ event -> ButtonEvent(event).action }, buttonUnit)
 * ```
 */
class SpenManager(
    private val onAvailabilityChanged: (Boolean) -> Unit = {},
    private val onDebug: (String) -> Unit = {},
    onAction: (SpenAction) -> Unit
) {
    private val detector = SpenButtonDetector(onAction = onAction)

    // O SDK da Samsung EXIGE um contexto de Activity em connect()/disconnect().
    private var activityContext: Context? = null

    var isAvailable: Boolean = false
        private set(value) {
            field = value
            onAvailabilityChanged(value)
        }

    private var spenRemote: Any? = null
    private var unitManager: Any? = null

    /**
     * Tenta conectar ao SDK da S Pen. Seguro de chamar sempre.
     * @param activity DEVE ser um contexto de Activity (exigência do SDK).
     */
    fun connect(activity: Context) {
        activityContext = activity
        try {
            onDebug("connect() iniciado")
            val remoteCls = Class.forName(PKG + "SpenRemote")
            val remote = remoteCls.getMethod("getInstance").invoke(null) ?: run {
                onDebug("getInstance() retornou null")
                return
            }
            onDebug("SDK encontrado (getInstance OK)")

            val featureButton = remoteCls.getField("FEATURE_TYPE_BUTTON").getInt(null)
            val enabled = remoteCls
                .getMethod("isFeatureEnabled", Int::class.javaPrimitiveType)
                .invoke(remote, featureButton) as? Boolean ?: false
            onDebug("isFeatureEnabled(BUTTON)=$enabled")
            if (!enabled) {
                Log.i(TAG, "Botão da S Pen indisponível neste dispositivo.")
                onDebug("BOTÃO indisponível: pareie a S Pen por Bluetooth")
                return
            }

            val callbackCls = Class.forName(PKG + "SpenRemote\$ConnectionResultCallback")
            val callback = Proxy.newProxyInstance(
                callbackCls.classLoader,
                arrayOf(callbackCls)
            ) { _, method, args ->
                when (method.name) {
                    "onSuccess" -> onConnected(args?.getOrNull(0))
                    "onFailure" -> {
                        Log.w(TAG, "Falha ao conectar à S Pen: ${args?.getOrNull(0)}")
                        onDebug("onFailure: ${args?.getOrNull(0)}")
                    }
                }
                null
            }

            remoteCls
                .getMethod("connect", Context::class.java, callbackCls)
                .invoke(remote, activity, callback)
            spenRemote = remote
            onDebug("connect() chamado — aguardando callback…")
        } catch (_: ClassNotFoundException) {
            // SDK não incluído — comportamento esperado em dispositivos não-Samsung.
            onDebug("SDK AUSENTE (ClassNotFoundException)")
            isAvailable = false
        } catch (t: Throwable) {
            Log.w(TAG, "Não foi possível iniciar a S Pen.", t)
            reportError("connect", t)
            isAvailable = false
        }
    }

    /** Desembrulha InvocationTargetException e reporta a causa real no painel. */
    private fun reportError(stage: String, t: Throwable) {
        val real = (t as? java.lang.reflect.InvocationTargetException)?.targetException
            ?: t.cause ?: t
        onDebug("ERRO $stage: ${real.javaClass.name}: ${real.message ?: "(sem msg)"}")
        real.stackTrace.firstOrNull()?.let { onDebug("  em ${it.className}.${it.methodName}:${it.lineNumber}") }
    }

    private fun onConnected(manager: Any?) {
        onDebug("onSuccess — registrando listener…")
        manager ?: return
        try {
            val unitCls = Class.forName(PKG + "SpenUnit")
            val managerCls = Class.forName(PKG + "SpenUnitManager")
            val listenerCls = Class.forName(PKG + "SpenEventListener")
            val eventCls = Class.forName(PKG + "SpenEvent")
            val buttonEventCls = Class.forName(PKG + "ButtonEvent")

            val typeButton = unitCls.getField("TYPE_BUTTON").getInt(null)
            val actionDown = buttonEventCls.getField("ACTION_DOWN").getInt(null)

            val buttonUnit = managerCls
                .getMethod("getUnit", Int::class.javaPrimitiveType)
                .invoke(manager, typeButton)

            val listener = Proxy.newProxyInstance(
                listenerCls.classLoader,
                arrayOf(listenerCls)
            ) { _, method, args ->
                if (method.name == "onEvent" && args != null) {
                    val event = args[0]
                    val buttonEvent = buttonEventCls.getConstructor(eventCls).newInstance(event)
                    val action = buttonEventCls.getMethod("getAction").invoke(buttonEvent) as Int
                    if (action == actionDown) {
                        onDebug("botão DOWN")
                        detector.onButtonDown()
                    } else {
                        onDebug("botão UP")
                        detector.onButtonUp()
                    }
                }
                null
            }

            managerCls
                .getMethod("registerSpenEventListener", listenerCls, unitCls)
                .invoke(manager, listener, buttonUnit)

            unitManager = manager
            isAvailable = true
            Log.i(TAG, "S Pen conectada — controle remoto ativo.")
            onDebug("CONECTADA ✓ listener do botão registrado")
        } catch (t: Throwable) {
            Log.w(TAG, "Erro ao registrar eventos da S Pen.", t)
            reportError("onConnected", t)
            isAvailable = false
        }
    }

    /** Libera os recursos do SDK. Chamar no onDestroy da Activity. */
    fun disconnect() {
        detector.cancel()
        try {
            val ctx = activityContext
            spenRemote?.let { remote ->
                if (ctx != null) {
                    remote.javaClass
                        .getMethod("disconnect", Context::class.java)
                        .invoke(remote, ctx)
                }
            }
        } catch (_: Throwable) {
            // ignora — desconexão é best-effort
        } finally {
            spenRemote = null
            unitManager = null
            activityContext = null
            isAvailable = false
        }
    }

    private companion object {
        const val TAG = "SpenManager"
        const val PKG = "com.samsung.android.sdk.penremote."
    }
}
