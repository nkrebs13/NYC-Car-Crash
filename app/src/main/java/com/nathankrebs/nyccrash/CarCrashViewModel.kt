package com.nathankrebs.nyccrash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nathankrebs.nyccrash.model.CarCrashItem
import com.nathankrebs.nyccrash.repository.CarCrashRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CarCrashViewModel(
    private val carCrashRepository: CarCrashRepository,
) : ViewModel() {

    val uiState: StateFlow<UiState> =
        carCrashRepository.carCrashes
            .map { newCarCrashes ->
                UiState(
                    carCrashes = newCarCrashes,
                    status = UiState.UiStatus.Data,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(1_000),
                initialValue = UiState.INITIAL
            )

    data class UiState(
        val carCrashes: List<CarCrashItem>,
        val status: UiStatus,
    ) {
        enum class UiStatus {
            Loading,
            Data,
            Error
        }

        companion object {
            val INITIAL = UiState(
                carCrashes = emptyList(),
                status = UiStatus.Loading,
            )
        }
    }
}
