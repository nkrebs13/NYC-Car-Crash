package com.nathankrebs.nyccrash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.nathankrebs.nyccrash.NetworkingSingleton.AppHttpClient
import com.nathankrebs.nyccrash.ui.theme.NYCCrashTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NYCCrashTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val greetingState = remember { mutableStateOf("") }
                    LaunchedEffect(key1 = null) {
                        val carCrashes = CarCrashApiImpl(
                            AppHttpClient,
                            getString(R.string.api_key)
                        ).getCarCrashes()
                        greetingState.value = carCrashes.toString()
                    }

                    Text(text = greetingState.value)
                }
            }
        }
    }
}
