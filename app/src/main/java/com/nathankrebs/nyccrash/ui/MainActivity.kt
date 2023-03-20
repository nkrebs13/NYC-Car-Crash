package com.nathankrebs.nyccrash.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.nathankrebs.nyccrash.ui.compose.AppMap
import com.nathankrebs.nyccrash.ui.theme.NYCCrashTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CarCrashViewModel by viewModel()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NYCCrashTheme {
                BottomSheetScaffold(
                    modifier = Modifier.fillMaxSize(),
                    sheetContent = {
                        val carCrashState = viewModel.uiState
                            .collectAsState(initial = CarCrashViewModel.UiState.INITIAL)
                        Text(text = carCrashState.value.toString())
                    },
                    content = {
                        AppMap(modifier = Modifier.fillMaxSize())
                    }
                )
            }
        }
    }
}
