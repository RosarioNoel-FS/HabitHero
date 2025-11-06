package com.example.habithero

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class HabitHeroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}
