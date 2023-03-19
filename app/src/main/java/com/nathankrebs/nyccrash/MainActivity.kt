package com.nathankrebs.nyccrash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.nathankrebs.nyccrash.repository.CarCrashRepository
import com.nathankrebs.nyccrash.ui.theme.NYCCrashTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val repository: CarCrashRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NYCCrashTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val greetingState = repository.carCrashes.collectAsState(initial = emptyList())
                    Text(text = greetingState.value.toString())
                }
            }
        }
    }
}
