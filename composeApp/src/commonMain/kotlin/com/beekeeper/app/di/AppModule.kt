package com.beekeeper.app.di

import com.beekeeper.app.data.api.ApiClient
import com.beekeeper.app.data.database.Database
import com.beekeeper.app.data.repository.AIAdvisorRepository
import com.beekeeper.app.data.repository.ApiaryRepository
import com.beekeeper.app.data.repository.HiveRepository
import com.beekeeper.app.data.repository.FeedRepository
import com.beekeeper.app.data.repository.FollowRepository
import com.beekeeper.app.data.repository.InspectionRepository
import com.beekeeper.app.data.repository.TaskRepository
import com.beekeeper.app.ui.viewmodel.AIAdvisorViewModel
import com.beekeeper.app.ui.viewmodel.ApiaryListViewModel
import com.beekeeper.app.ui.viewmodel.CreateApiaryViewModel
import com.beekeeper.app.ui.viewmodel.FeedViewModel
import com.beekeeper.app.ui.viewmodel.InspectionsViewModel
import com.beekeeper.app.ui.viewmodel.TasksViewModel
import org.koin.dsl.module

val appModule = module {
    // Database — DatabaseDriverFactory is provided per-platform via platformModule
    single { Database(get()) }

    // API Client
    single { ApiClient() }

    // Repositories
    single { TaskRepository(get(), get()) }
    single { InspectionRepository(get(), get()) }
    single { AIAdvisorRepository(get()) }
    single { FeedRepository(get(), get()) }
    single { FollowRepository(get()) }
    single { ApiaryRepository(get(), get()) }
    single { HiveRepository(get(), get()) }

    // ViewModels
    factory { TasksViewModel(get()) }
    factory { InspectionsViewModel(get()) }
    factory { AIAdvisorViewModel(get()) }
    factory { FeedViewModel(get(), get()) }
    factory { CreateApiaryViewModel(get(), get()) }
    factory { ApiaryListViewModel(get()) }
}
