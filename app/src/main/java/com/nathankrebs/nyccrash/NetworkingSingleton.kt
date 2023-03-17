package com.nathankrebs.nyccrash

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.internal.platform.android.AndroidLog

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
        encodeDefaults = true
    }

    /**
     * The OkHTTP Client for the application
     */
    val AppHttpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(AppJson)
        }

        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("API", message)
                }
            }
        }
    }
}
