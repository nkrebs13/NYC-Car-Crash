package com.nathankrebs.nyccrash.ui.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import com.nathankrebs.nyccrash.R
import com.nathankrebs.nyccrash.ui.CarCrashViewModel

@Composable
fun MainScreen(
    modifier: Modifier,
    crashDataStatus: CarCrashViewModel.UiState.UiStatus,
    latLngs: List<LatLng>,
    dateWithMostCrashes: String?,
    hourlyCrashes: List<Int>,
    onVisibleRegionChange: (VisibleRegion) -> Unit,
    onClickRetry: () -> Unit,
) {
    if(crashDataStatus == CarCrashViewModel.UiState.UiStatus.Error) {
        ErrorUi(modifier = modifier, onClickRetry = onClickRetry)
    } else {
        Box(modifier = modifier) {
            if (crashDataStatus == CarCrashViewModel.UiState.UiStatus.Loading) {
                LoadingDialog()
            }
            Column(modifier = Modifier.fillMaxSize()) {
                ExpandableHourGraph(
                    dataIsLoaded = crashDataStatus == CarCrashViewModel.UiState.UiStatus.Data,
                    hourlyCrashes = hourlyCrashes,
                    dateWithMostCrashes = dateWithMostCrashes,
                )
                AppMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    latLngs = latLngs,
                    onCameraMoved = { visibleRegion -> onVisibleRegionChange.invoke(visibleRegion) },
                )
            }
        }
    }
}

@Composable
private fun ExpandableHourGraph(
    dataIsLoaded: Boolean,
    hourlyCrashes: List<Int>,
    dateWithMostCrashes: String?,
) {
    val showGraph = remember { mutableStateOf(true) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = MaterialTheme.colors.primary
                    .copy(alpha = 0.3f)
                    .compositeOver(MaterialTheme.colors.background)
            )
            .padding(16.dp)
            .animateContentSize(
                animationSpec = TweenSpec(
                    durationMillis = 200,
                    easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
                )
            ) { initialValue, targetValue -> /* no op */ }
            .clickable { showGraph.value = !showGraph.value },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showGraph.value) {
            if (dataIsLoaded) {
                HourlyGraph(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(200.dp),
                    hourlyEntries = hourlyCrashes
                )
                if (dateWithMostCrashes != null) {
                    Text(
                        modifier = Modifier,
                        style = MaterialTheme.typography.body1,
                        text = stringResource(id = R.string.most_crashes_day, dateWithMostCrashes),
                    )
                }
            }
        } else {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = stringResource(R.string.collapsed_graph_prompt)
            )
        }
    }
}