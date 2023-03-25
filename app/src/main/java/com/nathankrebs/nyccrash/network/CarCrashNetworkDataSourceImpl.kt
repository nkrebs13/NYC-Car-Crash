package com.nathankrebs.nyccrash.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.encodeURLParameter

class CarCrashNetworkDataSourceImpl(
    private val httpClient: HttpClient,
    private val apiKey: String,
) : CarCrashNetworkDataSource {

    override suspend fun getCarCrashes(
        startDate: String,
        endDate: String
    ): List<CarCrashApiItem> =
        try {
            httpClient.get(buildUrl(startDate, endDate)) {
                headers { append("X-App-Token", apiKey) }
                url {
                    // default limit is 1000
                    // 20000 should be more than enough
                    parameters.append("\$limit", "20000")
                }
            }.body()
        } catch(e: Exception) {
            Log.e(TAG, "Error getting car crashes for dates $startDate - $endDate. Url=${buildUrl(startDate, endDate)}")
            throw(e)
        }

    /**
     * Builds out the request URL for requesting crashes between 2 specific dates
     *
     * @param startDate The ISO8601 datetime string representing the start of the date range
     * @param endDate The ISO8601 datetime string representing the end of the date range
     */
    private fun buildUrl(startDate: String, endDate: String): String {
        // crash_date between '2023-03-01T12:00:00' and '2023-03-02T14:00:00'
        val soqlParam = "crash_date between '$startDate' and '$endDate'"
        return "$BASE_URL?\$where=${soqlParam.encodeURLParameter()}"
    }

    companion object {
        private const val TAG = "CarCrashNetworkDataSource"
        private const val BASE_URL = "https://data.cityofnewyork.us/resource/h9gi-nx95.json"
    }
}
