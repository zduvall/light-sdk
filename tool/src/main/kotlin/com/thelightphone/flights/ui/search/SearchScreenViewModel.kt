package com.thelightphone.flights.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.thelightphone.flights.data.AeroDataBoxClient
import com.thelightphone.flights.data.FlightsRepository
import com.thelightphone.flights.data.SearchHistory
import com.thelightphone.flights.model.ApiResult
import com.thelightphone.sdk.LightViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Flights Search Screen.
 */
class SearchScreenViewModel(
    private val flightsRepository: FlightsRepository,
    private val aeroDataBoxClient: AeroDataBoxClient
) : LightViewModel<Unit>() {

    /** Expose the latest flight number search query as UI state. */
    val latestSearch: StateFlow<String> = flightsRepository.latestSearchFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    /**
     * Persist the latest flight status search query to local disk.
     * @param value The latest search query string to persist.
     */
    fun setLatestSearch(value: String) {
        viewModelScope.launch {
            // NonCancellable ensures that the DataStore write completes
            // even if the ViewModel is destroyed by navigating away
            withContext(NonCancellable) {
                flightsRepository.setLatestSearch(value)
            }
        }
    }

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    val searchHistory: StateFlow<SearchHistory> = flightsRepository.searchHistoryFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun setSearchHistory(value: SearchHistory) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                flightsRepository.setSearchHistory(value)
            }
        }
    }

    fun handleSearchQuery(flightNumber: String) {
        setLatestSearch(flightNumber)
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val standardFlightNumber = standardizeFlightNumber(flightNumber)

                when (val result = aeroDataBoxClient.fetchFlightStatus(standardFlightNumber)) {
                    is ApiResult.Success -> {
                        val updatedHistory = searchHistory.value.toMutableMap()
                        updatedHistory[standardFlightNumber] = result.data
                        flightsRepository.setSearchHistory(updatedHistory)
                    }

                    is ApiResult.Error.MissingAuth -> {
                        errorMessage =
                            "Missing API key. Please provide your AeroDataBox RapidAPI key on the settings page."
                    }

                    is ApiResult.Error.Unauthorized -> {
                        errorMessage =
                            "Invalid API key. Please check your AeroDataBox RapidAPI key on the settings page."
                    }

                    is ApiResult.Error.RateLimited -> {
                        errorMessage = "Rate limit exceeded. Please try again later."
                    }

                    is ApiResult.Error.NotFound -> {
                        errorMessage = "No flights found matching flight number \"$flightNumber.\""
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
                        errorMessage = "An error occurred while fetching flight status. Please try again."
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }
}

/**
 * Standardizes flight number by removing whitespace & converting to lowercase.
 */
fun standardizeFlightNumber(flightNumber: String): String {
    return flightNumber.replace("\\s".toRegex(), "").lowercase()
}
