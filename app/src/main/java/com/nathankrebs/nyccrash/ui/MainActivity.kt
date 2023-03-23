package com.nathankrebs.nyccrash.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nathankrebs.nyccrash.ui.compose.AppMap
import com.nathankrebs.nyccrash.ui.compose.HourlyGraph
import com.nathankrebs.nyccrash.ui.compose.HourlyGraph2
import com.nathankrebs.nyccrash.ui.theme.NYCCrashTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private val viewModel: CarCrashViewModel by viewModel()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NYCCrashTheme {
                val carCrashState = viewModel.uiState
                    .collectAsState(initial = CarCrashViewModel.UiState.INITIAL)

                val scaffoldState = rememberBottomSheetScaffoldState(
                    bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
                )

                BottomSheetScaffold(
                    modifier = Modifier.fillMaxSize(),
                    scaffoldState = scaffoldState,
                    sheetContent = {
                        Column(
                            modifier = Modifier
//                                .heightIn(max = 64.dp)
                                .verticalScroll(state = rememberScrollState()),
                        ) {
                            val yAxisCount = 8
                            val yStep: Int = remember(carCrashState.value.crashesByTime) {
                                val maxCrashes = carCrashState.value.crashesByTime.max()
                                val chartMax = maxCrashes * 1.1
                                (chartMax / yAxisCount).roundToInt()
                            }
                            val yValues: List<String> = remember(yStep) {
                                IntRange(0, yAxisCount - 1).map { (it * yStep).toString() }
                            }
                            HourlyGraph(
                                modifier = Modifier
                                    .height(256.dp)
                                    .padding(16.dp),
                                points = carCrashState.value.crashesByTime.toList(),
                                // todo - move these to res
                                xValues = listOf("midnight", "6am", "noon", "6pm"),
                                yValues = yValues,
                                paddingSpace = 32.dp,
                                verticalStep = yStep
                            )
                            HourlyGraph2(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(256.dp)
                                    .padding(16.dp),
                                hourlyEntries = carCrashState.value.crashesByTime.toList()
                            )

                            Text(text = carCrashState.value.toString())
                        }
                    },
                    content = {
                        AppMap(
                            modifier = Modifier.fillMaxSize(),
                            latLngs = carCrashState.value.latLngs,
                            onCameraMoved = { visibleRegion ->
                                viewModel.onMapVisibleRegionChange(visibleRegion)
                            },
                        )
                    }
                )
            }
        }
    }
}
