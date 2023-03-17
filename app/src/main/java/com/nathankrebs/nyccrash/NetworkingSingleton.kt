package com.nathankrebs.nyccrash

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Singleton containing all of the networking objects that should only be created once for the
 * networking layer of the application.
 */
object NetworkingSingleton {

    /**
     * The Json object for the application
     */
    val AppJson: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    /**
     * The OkHTTP Client for the application
     */
    val AppHttpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(AppJson)
        }
    }
}
