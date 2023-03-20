package com.nathankrebs.nyccrash

import androidx.room.Room
import com.nathankrebs.nyccrash.db.CarCrashDao
import com.nathankrebs.nyccrash.db.CarCrashDatabase
import com.nathankrebs.nyccrash.db.CarCrashLocalDataSource
import com.nathankrebs.nyccrash.db.CarCrashLocalDataSourceImpl
import com.nathankrebs.nyccrash.network.CarCrashNetworkDataSource
import com.nathankrebs.nyccrash.network.CarCrashNetworkDataSourceImpl
import com.nathankrebs.nyccrash.network.NetworkingSingleton
import com.nathankrebs.nyccrash.repository.CarCrashRepository
import com.nathankrebs.nyccrash.repository.CarCrashRepositoryImpl
import com.nathankrebs.nyccrash.ui.CarCrashViewModel
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModuleDi = module {
    single<HttpClient> {
        NetworkingSingleton.AppHttpClient
    }

    single<CarCrashNetworkDataSource> {
        CarCrashNetworkDataSourceImpl(
            httpClient = get(),
            apiKey = androidContext().getString(R.string.api_key)
        )
    }

    single<CarCrashLocalDataSource> {
        CarCrashLocalDataSourceImpl(
            database = Room.databaseBuilder(
                androidApplication().applicationContext,
                CarCrashDatabase::class.java,
                "car-crash-database"
            ).build()
        )
    }

    single<CarCrashRepository> {
        CarCrashRepositoryImpl(
            carCrashNetworkDataSource = get(),
            carCrashLocalDataSource = get(),
            ioDispatcher = Dispatchers.IO,
        )
    }

    viewModel {
        CarCrashViewModel(
            carCrashRepository = get(),
        )
    }
}
