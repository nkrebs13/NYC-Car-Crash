package com.nathankrebs.nyccrash.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * https://dev.socrata.com/foundry/data.cityofnewyork.us/h9gi-nx95
 */
@Serializable
data class CarCrashApiItem(
    @SerialName("crash_date") val crashDate: String,
    @SerialName("crash_time") val crashTime: String,
    val borough: String? = null,
    @SerialName("zip_code") val zipCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("on_street_name") val onStreetName: String? = null,
    @SerialName("off_street_name") val offStreetName: String? = null,
    @SerialName("cross_street_name") val crossStreetName: String? = null,
    @SerialName("number_of_persons_injured") val numberOfPersonsInjured: Int,
    @SerialName("number_of_persons_killed") val numberOfPersonsKilled: Int,
    @SerialName("number_of_pedestrians_injured") val numberOfPedestriansInjured: Int,
    @SerialName("number_of_pedestrians_killed") val numberOfPedestriansKilled: Int,
    @SerialName("number_of_cyclist_injured") val numberOfCyclistInjured: Int,
    @SerialName("number_of_cyclist_killed") val numberOfCyclistKilled: Int,
    @SerialName("number_of_motorist_injured") val numberOfMotoristInjured: Int,
    @SerialName("number_of_motorist_killed") val numberOfMotoristKilled: Int,
    @SerialName("contributing_factor_vehicle_1") val contributingFactorVehicle1: String? = null,
    @SerialName("contributing_factor_vehicle_2") val contributingFactorVehicle2: String? = null,
    @SerialName("contributing_factor_vehicle_3") val contributingFactorVehicle3: String? = null,
    @SerialName("contributing_factor_vehicle_4") val contributingFactorVehicle4: String? = null,
    @SerialName("contributing_factor_vehicle_5") val contributingFactorVehicle5: String? = null,
    @SerialName("collision_id") val collisionId: String,
    @SerialName("vehicle_type_code1") val vehicleTypeCode1: String? = null,
    @SerialName("vehicle_type_code2") val vehicleTypeCode2: String? = null,
    @SerialName("vehicle_type_code3") val vehicleTypeCode3: String? = null,
    @SerialName("vehicle_type_code4") val vehicleTypeCode4: String? = null,
    @SerialName("vehicle_type_code5") val vehicleTypeCode5: String? = null,
)
