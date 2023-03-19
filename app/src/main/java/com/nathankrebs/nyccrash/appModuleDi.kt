package com.nathankrebs.nyccrash

import com.nathankrebs.nyccrash.network.CarCrashNetworkDataSource
import com.nathankrebs.nyccrash.network.CarCrashNetworkDataSourceImpl
import com.nathankrebs.nyccrash.network.NetworkingSingleton
import com.nathankrebs.nyccrash.repository.CarCrashRepository
import com.nathankrebs.nyccrash.repository.CarCrashRepositoryImpl
import io.ktor.client.HttpClient
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

    single<CarCrashRepository> {
        CarCrashRepositoryImpl(
            carCrashNetworkDataSource = get(),
        )
    }

    viewModel {
        CarCrashViewModel(
            carCrashRepository = get(),
        )
    }
}
