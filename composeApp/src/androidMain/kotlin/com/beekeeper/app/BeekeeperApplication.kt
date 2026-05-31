package com.beekeeper.app

import android.app.Application
import com.beekeeper.app.di.appModule
import com.beekeeper.app.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BeekeeperApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BeekeeperApplication)
            modules(appModule, platformModule)
        }
    }
}
