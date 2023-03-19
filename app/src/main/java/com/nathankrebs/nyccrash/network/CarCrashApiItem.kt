package com.nathankrebs.nyccrash.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * https://dev.socrata.com/foundry/data.cityofnewyork.us/h9gi-nx95
 */
@Serializable
data class CarCrashItem(
    @SerialName("crash_date") val crashDate: String,
    @SerialName("crash_time") val crashTime: String,
    val borough: String? = null,
    @SerialName("zip_code") val zipCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("number_of_persons_injured") val numberOfPersonsInjured: Int,
    @SerialName("number_of_persons_killed") val numberOfPersonsKilled: Int,
)
