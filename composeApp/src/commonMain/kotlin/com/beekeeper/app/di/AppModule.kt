package com.beekeeper.app.di

import com.beekeeper.app.data.api.ApiClient
import com.beekeeper.app.data.database.Database
import com.beekeeper.app.data.database.DatabaseDriverFactory
import com.beekeeper.app.data.repository.AIAdvisorRepository
import com.beekeeper.app.data.repository.InspectionRepository
import com.beekeeper.app.data.repository.TaskRepository
import com.beekeeper.app.ui.viewmodel.AIAdvisorViewModel
import com.beekeeper.app.ui.viewmodel.InspectionsViewModel
import com.beekeeper.app.ui.viewmodel.TasksViewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single { DatabaseDriverFactory(get()) }
    single { Database(get()) }

    // API Client
    single { ApiClient() }

    // Repositories
    single { TaskRepository(get(), get()) }
    single { InspectionRepository(get(), get()) }
    single { AIAdvisorRepository(get()) }

    // ViewModels
    factory { TasksViewModel(get()) }
    factory { InspectionsViewModel(get()) }
    factory { AIAdvisorViewModel(get()) }
}
