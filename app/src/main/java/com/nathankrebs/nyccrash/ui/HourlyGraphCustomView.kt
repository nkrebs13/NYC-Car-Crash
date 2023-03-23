package com.nathankrebs.nyccrash.ui

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.nathankrebs.nyccrash.R

class HourlyGraphCustomView(context: Context) : FrameLayout(context) {
    init {
        View.inflate(context, R.layout.hourly_graph, this)
    }
}
