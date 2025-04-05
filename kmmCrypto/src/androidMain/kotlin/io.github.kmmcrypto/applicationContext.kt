package io.github.kmmcrypto

import android.content.Context
import androidx.startup.Initializer


class ApplicationContextInitializer : Initializer<Context> {
    override fun create(context: Context): Context = context.also {
        AndroidKMMCrypto.applicationContext = it.applicationContext
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

