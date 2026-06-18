package com.beekeeper.app.di

import com.beekeeper.app.data.database.DatabaseDriverFactory
import com.beekeeper.app.location.LocationProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
    single { LocationProvider() }
}
