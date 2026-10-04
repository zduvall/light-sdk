package com.thelightphone.flights.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.thelightphone.flights.data.AeroDataBoxClient
import com.thelightphone.flights.data.FlightsRepository
import com.thelightphone.flights.data.SettingsRepository
import com.thelightphone.flights.model.EditorRequest
import com.thelightphone.flights.model.FlightStatus
import com.thelightphone.flights.ui.editor.TextInputEditorScreen
import com.thelightphone.flights.ui.navigation.FlightsTab
import com.thelightphone.flights.ui.navigation.TabScaffold
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp

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

@Composable
fun FlightStatus(fS: FlightStatus) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LightText(
            fS.departure.airport.iata ?: "Unknown",
            variant = LightTextVariant.Subtitle,
        )
        LightText(
            fS.arrival.airport.iata ?: "Unknown",
            variant = LightTextVariant.Subtitle,
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
