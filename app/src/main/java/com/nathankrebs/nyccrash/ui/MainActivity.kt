package com.nathankrebs.nyccrash.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.nathankrebs.nyccrash.ui.compose.MainScreen
import com.nathankrebs.nyccrash.ui.theme.NYCCrashTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CarCrashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NYCCrashTheme {
                val carCrashState = viewModel.uiState
                    .collectAsState(initial = CarCrashViewModel.UiState.INITIAL)

                MainScreen(
                    modifier = Modifier.fillMaxSize(),
                    crashDataStatus = carCrashState.value.status,
                    latLngs = carCrashState.value.latLngs,
                    hourlyCrashes = carCrashState.value.crashesByTime.toList(),
                    onVisibleRegionChange = {
                        viewModel.onMapVisibleRegionChange(it)
                    }
                )
            }
        }
    }
}
