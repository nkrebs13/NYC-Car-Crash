package com.nathankrebs.nyccrash

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * https://dev.socrata.com/foundry/data.cityofnewyork.us/h9gi-nx95
 */
@Serializable
data class CarCrashItem(
    @SerialName("crash_date") val crashDate: Float,
    @SerialName("crash_time") val crashTime: String,
    val borough: String,
    @SerialName("zip_code") val zipCode: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("number_of_persons_injured") val numberOfPersonsInjured: Int,
    @SerialName("number_of_persons_killed") val numberOfPersonsKilled: Int,
    val collisionId: Int,
)
