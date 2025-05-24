package com.gestiontienda.android

import android.app.Application
import com.gestiontienda.android.utils.LocaleUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GestionTiendaApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize the app with the stored language or default to English
        val storedLanguage = LocaleUtils.getStoredLanguage(this)
        LocaleUtils.setLocale(this, storedLanguage)
    }
} 
