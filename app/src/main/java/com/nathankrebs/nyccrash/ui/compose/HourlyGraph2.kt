package com.nathankrebs.nyccrash.ui.compose

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.nathankrebs.nyccrash.R
import com.nathankrebs.nyccrash.ui.HourlyGraphCustomView
import kotlinx.coroutines.delay


@Composable
fun HourlyGraph2(
    modifier: Modifier = Modifier,
    hourlyEntries: List<Int>
) {

    val entries: List<Entry> = remember(hourlyEntries) {
        hourlyEntries.mapIndexed { index, i ->
            Entry(index.toFloat(), i.toFloat())
        }
    }

    val lineDataSet = remember(entries) {
        LineDataSet(entries, "Hourly crashes").apply {
            color = Color.BLUE // set line color
            valueTextColor = Color.BLACK // set value text color
            lineWidth = 2f // set line width
            setDrawCircles(false) // draw circles at each data point
            setDrawValues(false) // draw values above each data point
            this.cubicIntensity = 0.8f
        }
    }

    val lineData = remember(lineDataSet) {
        LineData(lineDataSet)
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.hourly_graph, null, false)
            view.apply {
                findViewById<LineChart>(R.id.lineChartHourlyGraph).apply {
                    this.data = lineData
                    this.setTouchEnabled(true)
                    this.setPinchZoom(true)
                }
            }
        },
        update = { view ->
            view as LineChart
            view.data = lineData
            view.invalidate()
        }
    )
}