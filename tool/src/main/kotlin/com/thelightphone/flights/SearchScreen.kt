package com.thelightphone.flights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewModelScope

import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
import com.thelightphone.sdk.ui.LightTextVariant

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.NonCancellable
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

class SearchScreen(
    sealedActivity: SealedLightActivity
) : LightScreen<Unit, SearchScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<SearchScreenViewModel>
        get() = SearchScreenViewModel::class.java

    override fun createViewModel(): SearchScreenViewModel {
        val flightsRepository = FlightsRepository(lightContext.dataStore)
        val settingsRepository = SettingsRepository(lightContext.dataStore)

        val apiClient = AeroDataBoxClient(settingsRepository = settingsRepository)

        return SearchScreenViewModel(flightsRepository, apiClient)
    }

    @Composable
    override fun Content() {
        val latestSearchValue by viewModel.latestSearch.collectAsState()
        val searchHistoryValue by viewModel.searchHistory.collectAsState()

        TabScaffold(
            title = "Search",
            activeTab = FlightsTab.Search,
            onNavigate = { navigateTo(it) },
        ) {

            LightTextField(
                label = "Flight Code:",
                value = latestSearchValue,
                placeholder = "",
                onClick = {
                    val editorRequest = EditorRequest(
                        title = "Flight Code",
                        initialValue = latestSearchValue,
                    )
                    navigateTo(
                        screenFactory = { TextInputEditorScreen(it, editorRequest) },
                        resultCallback = viewModel::handleSearchQuery
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.75f.gridUnitsAsDp())

            )

            when {
                viewModel.isLoading || viewModel.errorMessage != null -> {
                    val displayText = if (viewModel.isLoading) "Loading..." else viewModel.errorMessage.orEmpty()

                    LightText(
                        text = displayText,
                        variant = LightTextVariant.Fine,
                        modifier = Modifier.padding(
                            start = 0.75f.gridUnitsAsDp(),
                            end = 0.75f.gridUnitsAsDp(),
                            bottom = 0.75f.gridUnitsAsDp()
                        ),
                        align = TextAlign.Justify,
                        lighten = true
                    )
                }

                else -> {
                    val flightStatus = searchHistoryValue[standardizeFlightNumber(latestSearchValue)].orEmpty()

                    flightStatus.forEach { status ->
                        FlightStatus(status)
                    }
                }
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

@Composable
fun FlightStatus(fS: FlightStatus) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LightText(
            fS.departure.airport.iata, variant = LightTextVariant.Subtitle,
        )
        LightText(
            fS.arrival.airport.iata, variant = LightTextVariant.Subtitle,
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LightText(fS.departure.airport.name, variant = LightTextVariant.Detail)
        LightText(fS.arrival.airport.name, variant = LightTextVariant.Detail)
    }
}