package com.nathankrebs.nyccrash

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.headers

class CarCrashApiImpl(
    private val httpClient: HttpClient,
    private val apiKey: String,
) : CarCrashApi {

    override suspend fun getCarCrashes(): List<CarCrashItem> =
        httpClient.makeCarCrashGET {
            url {
                parameters.append("\$limit", "5000")
                parameters.append("\$order", "crash_date DESC")
            }
        }.body()

    /**
     * Makes the GET request to the URL and attaches the proper HTTP header for the app id
     */
    private suspend fun HttpClient.makeCarCrashGET(
        block: HttpRequestBuilder.() -> Unit = {}
    ) = this.get(BASE_URL) {
        headers { append("X-App-Token", apiKey) }
        this.apply(block)
    }

    companion object {
        private const val BASE_URL = "https://data.cityofnewyork.us/resource/h9gi-nx95.json"
    }
}
