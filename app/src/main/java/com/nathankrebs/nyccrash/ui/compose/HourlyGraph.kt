package com.nathankrebs.nyccrash.ui.compose

import android.content.Context
import android.view.LayoutInflater
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.nathankrebs.nyccrash.R

/**
 * Shows a cubic-smoothed line graph for entries [hourlyEntries]
 */
@Composable
fun HourlyGraph(
    modifier: Modifier = Modifier,
    hourlyEntries: List<Int>,
) {
    val entries: List<Entry> = remember(hourlyEntries) {
        hourlyEntries.mapIndexed { index, i ->
            Entry(index.toFloat(), i.toFloat())
        }
    }

    val lineColor = MaterialTheme.colors.primary.toArgb()
    val textColor = LocalTextStyle.current.color.toArgb()
    val dataLabel = stringResource(R.string.chart_data_label)
    val lineDataSet = remember(entries) {
        LineDataSet(entries, dataLabel).apply {
            color = lineColor
            lineWidth = 10f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
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
                    this.setTouchEnabled(false)
                    this.setPinchZoom(false)
                    this.xAxis.apply {
                        valueFormatter = HourlyXAxisValueFormatter(context)
                        this.position = XAxis.XAxisPosition.BOTTOM
                        this.textColor = textColor
                    }

                    this.description = Description().apply {
                        this.text = context.getString(R.string.chart_description)
                        this.textColor = textColor
                    }
                    this.viewPortHandler.apply {
                        extraTopOffset = 20f
                    }
                    this.axisLeft.textColor = textColor
                    this.axisRight.textColor = textColor
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

@Preview
@Composable
private fun HourlyGraphPreviewLight() {
    HourlyGraph(
        modifier = Modifier
            .background(androidx.compose.ui.graphics.Color.White)
            .fillMaxWidth()
            .height(256.dp)
            .padding(16.dp),
        hourlyEntries = listOf(
            217, 102, 96, 89,
            109, 97, 124, 219,
            305, 207, 177, 214,
            235, 224, 269, 273,
            275, 241, 250, 226,
            216, 178, 147, 150
        )
    )

}

/**
 * X Axis [ValueFormatter] for use within [HourlyGraph]
 */
private class HourlyXAxisValueFormatter(private val context: Context) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return when (value.toInt()) {
            0 -> context.getString(R.string.chart_hour_0)
            1 -> context.getString(R.string.chart_hour_1)
            2 -> context.getString(R.string.chart_hour_2)
            3 -> context.getString(R.string.chart_hour_3)
            4 -> context.getString(R.string.chart_hour_4)
            5 -> context.getString(R.string.chart_hour_5)
            6 -> context.getString(R.string.chart_hour_6)
            7 -> context.getString(R.string.chart_hour_7)
            8 -> context.getString(R.string.chart_hour_8)
            9 -> context.getString(R.string.chart_hour_9)
            10 -> context.getString(R.string.chart_hour_10)
            11 -> context.getString(R.string.chart_hour_11)
            12 -> context.getString(R.string.chart_hour_12)
            13 -> context.getString(R.string.chart_hour_13)
            14 -> context.getString(R.string.chart_hour_14)
            15 -> context.getString(R.string.chart_hour_15)
            16 -> context.getString(R.string.chart_hour_16)
            17 -> context.getString(R.string.chart_hour_17)
            18 -> context.getString(R.string.chart_hour_18)
            19 -> context.getString(R.string.chart_hour_19)
            20 -> context.getString(R.string.chart_hour_20)
            21 -> context.getString(R.string.chart_hour_21)
            22 -> context.getString(R.string.chart_hour_22)
            23 -> context.getString(R.string.chart_hour_23)
            24 -> context.getString(R.string.chart_hour_24)
            else -> ""
        }
    }
}