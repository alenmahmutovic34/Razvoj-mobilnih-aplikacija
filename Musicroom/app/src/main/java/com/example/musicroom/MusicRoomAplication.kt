package com.example.musicroom

import android.app.Application
import android.content.Context

class MusicRoomApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Inicijalizacija DatabaseManager-a
        DatabaseManager.INSTANCE.initialize(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        // Zatvaranje konekcije sa bazom prilikom završetka aplikacije
        DatabaseManager.INSTANCE.close()
    }
}
