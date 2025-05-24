package com.gestiontienda.android.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

object LocaleUtils {
    private const val LANGUAGE_PREFERENCE = "language_preference"

    fun setLocale(context: Context, languageCode: String) {
        val locale = when (languageCode) {
            "en" -> Locale("en") // English
            "es" -> Locale("es") // Spanish
            "ar" -> Locale("ar", "MA") // Arabic (Morocco)
            "zh" -> Locale("zh", "CN") // Chinese (Simplified)
            else -> Locale("en") // Default to English
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        // Save the selected language
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(LANGUAGE_PREFERENCE, languageCode)
            .apply()

        // Update night mode for RTL support
        if (languageCode == "ar") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    fun getStoredLanguage(context: Context): String {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString(LANGUAGE_PREFERENCE, "en") ?: "en"
    }

    fun getAvailableLanguages(): List<Language> {
        return listOf(
            Language("en", "English", "English"),
            Language("es", "Español", "Spanish"),
            Language("ar", "العربية", "Arabic"),
            Language("zh", "中文", "Chinese")
        )
    }
}

data class Language(
    val code: String,
    val nativeName: String,
    val englishName: String,
) 
