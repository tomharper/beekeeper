package com.beekeeper.app.di

import org.koin.core.module.Module

/**
 * Platform-specific Koin module. Provides [com.beekeeper.app.data.database.DatabaseDriverFactory],
 * whose constructor differs per platform (Android needs a Context, iOS needs nothing), so it
 * cannot be constructed in commonMain.
 */
expect val platformModule: Module
