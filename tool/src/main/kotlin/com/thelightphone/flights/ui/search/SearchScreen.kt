package com.thelightphone.flights.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

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

                    flightStatus.forEachIndexed { index, status ->
                        FlightStatus(status)
                        if (index < flightStatus.lastIndex) {
                            Spacer(modifier = Modifier.height(0.5f.gridUnitsAsDp()))
                        }
                    }
                }
            }
        }
    }
}

private val FLIGHT_DATE_FORMATTER = DateTimeFormatter.ofPattern("E, MMM d", Locale.ENGLISH)
private val FLIGHT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

fun parseOffsetDateTimeOrNull(raw: String?): OffsetDateTime? {
    if (raw.isNullOrBlank()) return null
    val sanitized = raw.trim().replace(' ', 'T')
    return runCatching {
        OffsetDateTime.parse(sanitized)
    }.recoverCatching {
        LocalDateTime.parse(sanitized).atOffset(ZoneOffset.UTC)
    }.getOrNull()
}

fun formatFlightDate(rawLocalTime: String?): String {
    val dt = parseOffsetDateTimeOrNull(rawLocalTime) ?: return ""
    return dt.format(FLIGHT_DATE_FORMATTER)
}

fun formatFlightTime24h(rawLocalTime: String?): String {
    val dt = parseOffsetDateTimeOrNull(rawLocalTime) ?: return ""
    return dt.format(FLIGHT_TIME_FORMATTER)
}

@Composable
fun FlightStatus(fS: FlightStatus) {
    // 1. IATA codes
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LightText(
            fS.departure.airport.iata ?: "---",
            variant = LightTextVariant.Subtitle,
        )
        LightText(
            fS.arrival.airport.iata ?: "---",
            variant = LightTextVariant.Subtitle,
        )
    }

    // 2. City / Municipality · Date (e.g. "New York · Sat, Oct 3")
    val depCity = fS.departure.airport.municipalityName
        ?: fS.departure.airport.shortName
        ?: fS.departure.airport.name
    val depDate = formatFlightDate(
        fS.departure.scheduledTime?.local
            ?: fS.departure.revisedTime?.local
            ?: fS.departure.runwayTime?.local
    )
    val depLocationDate = listOfNotNull(
        depCity.takeIf { it.isNotBlank() },
        depDate.takeIf { it.isNotBlank() }
    ).joinToString(" · ")

    val arrCity = fS.arrival.airport.municipalityName
        ?: fS.arrival.airport.shortName
        ?: fS.arrival.airport.name
    val arrDate = formatFlightDate(
        fS.arrival.scheduledTime?.local
            ?: fS.arrival.revisedTime?.local
            ?: fS.arrival.predictedTime?.local
            ?: fS.arrival.runwayTime?.local
    )
    val arrLocationDate = listOfNotNull(
        arrCity.takeIf { it.isNotBlank() },
        arrDate.takeIf { it.isNotBlank() }
    ).joinToString(" · ")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LightText(depLocationDate, variant = LightTextVariant.Detail)
        LightText(arrLocationDate, variant = LightTextVariant.Detail)
    }

    // 3. Movement Status Labels
    val statusLower = fS.status.lowercase()
    val isArrived = statusLower.contains("arrived") || statusLower.contains("landed") || fS.arrival.runwayTime != null
    val isDeparted =
        isArrived || statusLower.contains("departed") || statusLower.contains("enroute") || statusLower.contains("airborne") || fS.departure.runwayTime != null

    val departureStatusLabel = if (isDeparted) "Departed" else "Scheduled departure"
    val arrivalStatusLabel = when {
        isArrived -> "Arrived"
        isDeparted -> "Estimated arrival"
        else -> "Scheduled arrival"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LightText(departureStatusLabel, variant = LightTextVariant.Fine, lighten = true)
        LightText(arrivalStatusLabel, variant = LightTextVariant.Fine, lighten = true)
    }

    // 4. Active Times
    val departureActiveTime = if (isDeparted) {
        fS.departure.runwayTime?.local ?: fS.departure.revisedTime?.local ?: fS.departure.scheduledTime?.local
    } else {
        fS.departure.revisedTime?.local ?: fS.departure.scheduledTime?.local
    }

    val arrivalActiveTime = if (isArrived) {
        fS.arrival.runwayTime?.local ?: fS.arrival.revisedTime?.local ?: fS.arrival.predictedTime?.local
        ?: fS.arrival.scheduledTime?.local
    } else {
        fS.arrival.revisedTime?.local ?: fS.arrival.predictedTime?.local ?: fS.arrival.scheduledTime?.local
    }

    val depTimeFormatted = formatFlightTime24h(departureActiveTime).ifEmpty { "--:--" }
    val arrTimeFormatted = formatFlightTime24h(arrivalActiveTime).ifEmpty { "--:--" }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LightText(depTimeFormatted, variant = LightTextVariant.Copy)
        LightText(arrTimeFormatted, variant = LightTextVariant.Copy)
    }

    // 5. Crossed out scheduled time if different
    val depScheduledTimeFormatted = formatFlightTime24h(fS.departure.scheduledTime?.local)
    val arrScheduledTimeFormatted = formatFlightTime24h(fS.arrival.scheduledTime?.local)

    val showDepStrikethrough = depScheduledTimeFormatted.isNotEmpty() &&
            depTimeFormatted != "--:--" &&
            depScheduledTimeFormatted != depTimeFormatted

    val showArrStrikethrough = arrScheduledTimeFormatted.isNotEmpty() &&
            arrTimeFormatted != "--:--" &&
            arrScheduledTimeFormatted != arrTimeFormatted

    if (showDepStrikethrough || showArrStrikethrough) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LightText(
                text = if (showDepStrikethrough) depScheduledTimeFormatted else "",
                variant = LightTextVariant.Fine,
                lighten = true,
                // strikethrough = showDepStrikethrough // comment in when supported by Light SDK
            )

            LightText(
                text = if (showArrStrikethrough) arrScheduledTimeFormatted else "",
                variant = LightTextVariant.Fine,
                lighten = true,
                // strikethrough = showArrStrikethrough // comment in when supported by Light SDK
            )
        }
    }
}
