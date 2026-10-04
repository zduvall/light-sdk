package com.thelightphone.flights.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.thelightphone.flights.data.AeroDataBoxClient
import com.thelightphone.flights.data.SettingsRepository
import com.thelightphone.flights.model.ApiResult
import com.thelightphone.flights.model.UsageLimits
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SimpleLightScreen
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel containing the data and behavior for SettingsScreen. Manages data
 * persistence asynchronously via shared [SettingsRepository].
 */
class SettingsScreenViewModel(
    private val settingsRepository: SettingsRepository,
    private val aeroDataBoxClient: AeroDataBoxClient
) : LightViewModel<Unit>() {

    /** Expose the API key as UI state. */
    val apiKey: StateFlow<String> = settingsRepository.apiKeyFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    /**
     * Persist AeroDataBox API key to local disk.
     * @param value The plaintext string token to persist.
     */
    fun setApiKey(value: String) {
        viewModelScope.launch {
            // NonCancellable ensures that the DataStore write completes
            // even if the ViewModel is destroyed by navigating away
            withContext(NonCancellable) {
                settingsRepository.setApiKey(value)
            }
        }
    }

    // Read the usage limit object as a StateFlow, defaulting to null if not yet set.
    val usageLimits: StateFlow<UsageLimits?> = settingsRepository.usageLimitsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /** Persist updated usage limits to local disk.
     * @param limits The [UsageLimits] instance containing the latest usage stats.
     */
    fun setUsageLimits(limits: UsageLimits) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                settingsRepository.setUsageLimits(limits)
            }
        }
    }

    /** Clear persisted usage limits from local disk. */
    fun clearUsageLimits() {
        viewModelScope.launch {
            withContext(NonCancellable) {
                settingsRepository.clearUsageLimits()
            }
        }
    }

    fun fetchAndSetUsageLimits() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                when (val result = aeroDataBoxClient.fetchUsageLimits()) {
                    is ApiResult.Success -> {
                        setUsageLimits(result.data)
                    }

                    is ApiResult.Error.MissingAuth -> {
                        errorMessage = "Missing API key. Please provide your AeroDataBox RapidAPI key."
                    }

                    is ApiResult.Error.Unauthorized -> {
                        errorMessage = "Invalid API key. Please check your AeroDataBox RapidAPI key."
                    }

                    is ApiResult.Error.RateLimited -> {
                        errorMessage = "Rate limit exceeded. Please try again later."
                    }

                    // // Comment in for debugging HTTP and network errors:
                    // is ApiResult.Error.Http -> {
                    //     errorMessage = "HTTP ${result.code}: ${result.message}"
                    // }

                    // is ApiResult.Error.Network -> {
                    //     errorMessage =
                    //         "Network error: ${result.throwable.localizedMessage ?: result.throwable.message ?: "Unknown error"}"
                    // }

                    else -> {
                        errorMessage = "An error occurred while fetching usage limits. Please try again."
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }

    override fun onScreenShow(screen: SimpleLightScreen<Unit>) {
        super.onScreenShow(screen)
        // Clear previous error message & usage
        errorMessage = null
        clearUsageLimits()
    }
}
