package com.nathankrebs.nyccrash

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * [SimpleDateFormat] to go to or from ISO8601 Datetime
 */
val sdfISO8601: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd'T'00:00:00", Locale.getDefault())

val sdfDisplayString: SimpleDateFormat
    get() = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
