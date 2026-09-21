package com.drcmind.cleaapp.di

import androidx.room.Room
import com.drcmind.cleaapp.data.local.datastore.AuthDataStore
import com.drcmind.cleaapp.data.local.room.CleaDatabase
import com.drcmind.cleaapp.data.remote.api.AuthApi
import com.drcmind.cleaapp.data.remote.api.HomeApiService
import com.drcmind.cleaapp.data.remote.api.MenstrualApiService
import com.drcmind.cleaapp.data.repository.AuthRepositoryImpl
import com.drcmind.cleaapp.data.repository.HomeRepositoryImpl
import com.drcmind.cleaapp.data.repository.MenstrualRepositoryImpl
import com.drcmind.cleaapp.domain.repository.AuthRepository
import com.drcmind.cleaapp.domain.repository.HomeRepository
import com.drcmind.cleaapp.domain.repository.MenstrualRepository
import com.drcmind.cleaapp.ui.auth.login.LoginViewModel
import com.drcmind.cleaapp.ui.auth.login.SignInViewModel
import com.drcmind.cleaapp.ui.auth.splash.SplashViewModel
import com.drcmind.cleaapp.ui.home.HomeViewModel
import com.drcmind.cleaapp.ui.menstrual.MenstrualViewModel
import com.drcmind.cleaapp.ui.profile.ProfileViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Data Layer - Local
    single { AuthDataStore(androidContext()) }
    single {
        Room.databaseBuilder(
            androidContext(),
            CleaDatabase::class.java,
            "clea_db"
        ).fallbackToDestructiveMigration().build()
    }
    single { get<CleaDatabase>().menstrualDao }

    // Data Layer - Remote
    single { AuthApi(get()) }
    single { MenstrualApiService(get(), get(), get()) }
    single { HomeApiService(get(), get(), get()) }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<MenstrualRepository> { MenstrualRepositoryImpl(get(), get()) }
    single<HomeRepository> { HomeRepositoryImpl(get()) }
    
    // UI Layer - ViewModels
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignInViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::MenstrualViewModel)
    viewModelOf(::HomeViewModel)
}
