package com.swiftbrowser.fast.secure.core.language

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    fun wrap(context: Context, code: String): Context {
        if (code == "system") return context
        val locale = if (code == "zh-TW") Locale.TRADITIONAL_CHINESE else Locale(code)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun getLanguageCode(languageName: String): String = when (languageName) {
        "English" -> "en"
        "हिन्दी" -> "hi"
        "Bahasa Indonesia" -> "id"
        "Deutsch" -> "de"
        "Português" -> "pt"
        "Русский" -> "ru"
        "繁體中文" -> "zh-TW"
        else -> "system"
    }
}
