package com.antigravity.businessapp

import android.app.Application
import com.antigravity.businessapp.data.AppDatabase

class App : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
    }
}

