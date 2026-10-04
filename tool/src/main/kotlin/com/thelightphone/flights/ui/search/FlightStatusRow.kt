package com.thelightphone.flights.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thelightphone.flights.model.FlightStatus
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

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

fun formatFlightDate(rawLocalTime: String?): String =
    parseOffsetDateTimeOrNull(rawLocalTime)?.format(FLIGHT_DATE_FORMATTER).orEmpty()

fun formatFlightTime24h(rawLocalTime: String?): String =
    parseOffsetDateTimeOrNull(rawLocalTime)?.format(FLIGHT_TIME_FORMATTER).orEmpty()

private fun formatLocationDate(
    municipalityName: String?,
    shortName: String?,
    name: String?,
    localTime: String?,
): String {
    val city = municipalityName ?: shortName ?: name ?: ""
    val date = formatFlightDate(localTime)

    return listOfNotNull(
        city.takeIf { it.isNotBlank() },
        date.takeIf { it.isNotBlank() },
    ).joinToString(" · ")
}

@Composable
fun FlightStatusRow(
    fS: FlightStatus,
    modifier: Modifier = Modifier,
) {
    val depLocationDate = formatLocationDate(
        municipalityName = fS.departure.airport.municipalityName,
        shortName = fS.departure.airport.shortName,
        name = fS.departure.airport.name,
        localTime = fS.departure.scheduledTime?.local
            ?: fS.departure.revisedTime?.local
            ?: fS.departure.runwayTime?.local,
    )

    val arrLocationDate = formatLocationDate(
        municipalityName = fS.arrival.airport.municipalityName,
        shortName = fS.arrival.airport.shortName,
        name = fS.arrival.airport.name,
        localTime = fS.arrival.scheduledTime?.local
            ?: fS.arrival.revisedTime?.local
            ?: fS.arrival.predictedTime?.local
            ?: fS.arrival.runwayTime?.local,
    )

    val statusLower = fS.status.lowercase()
    val isArrived =
        listOf("arrived", "landed").any(statusLower::contains) ||
                fS.arrival.runwayTime != null
    val isDeparted =
        isArrived ||
                listOf("departed", "enroute", "airborne").any(statusLower::contains) ||
                fS.departure.runwayTime != null

    val departureStatusLabel = if (isDeparted) "Departed" else "Scheduled departure"
    val arrivalStatusLabel = when {
        isArrived -> "Arrived"
        isDeparted -> "Estimated arrival"
        else -> "Scheduled arrival"
    }

    val departureActiveTime = if (isDeparted) {
        listOfNotNull(
            fS.departure.runwayTime?.local,
            fS.departure.revisedTime?.local,
            fS.departure.scheduledTime?.local,
        ).firstOrNull()
    } else {
        listOfNotNull(
            fS.departure.revisedTime?.local,
            fS.departure.scheduledTime?.local,
        ).firstOrNull()
    }

    val arrivalActiveTime = if (isArrived) {
        listOfNotNull(
            fS.arrival.runwayTime?.local,
            fS.arrival.revisedTime?.local,
            fS.arrival.predictedTime?.local,
            fS.arrival.scheduledTime?.local,
        ).firstOrNull()
    } else {
        listOfNotNull(
            fS.arrival.revisedTime?.local,
            fS.arrival.predictedTime?.local,
            fS.arrival.scheduledTime?.local,
        ).firstOrNull()
    }

    val depTimeFormatted = formatFlightTime24h(departureActiveTime).ifEmpty { "--:--" }
    val arrTimeFormatted = formatFlightTime24h(arrivalActiveTime).ifEmpty { "--:--" }

    // 1. IATA codes (e.g. "JFK" or "LAX")
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LightText(depLocationDate, variant = LightTextVariant.Detail)
            LightText(arrLocationDate, variant = LightTextVariant.Detail)
        }

        // 3. Movement Status Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LightText(
                departureStatusLabel,
                variant = LightTextVariant.Fine,
                lighten = true,
            )
            LightText(
                arrivalStatusLabel,
                variant = LightTextVariant.Fine,
                lighten = true,
            )
        }

        // 4. Active Times
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LightText(depTimeFormatted, variant = LightTextVariant.Copy)
            LightText(arrTimeFormatted, variant = LightTextVariant.Copy)
        }

        // 5. Crossed out scheduled time if different
        val depScheduledTimeFormatted =
            formatFlightTime24h(fS.departure.scheduledTime?.local)
        val arrScheduledTimeFormatted =
            formatFlightTime24h(fS.arrival.scheduledTime?.local)

        val showDepStrikethrough =
            depScheduledTimeFormatted.isNotEmpty() &&
                    depScheduledTimeFormatted != depTimeFormatted

        val showArrStrikethrough =
            arrScheduledTimeFormatted.isNotEmpty() &&
                    arrScheduledTimeFormatted != arrTimeFormatted

        if (showDepStrikethrough || showArrStrikethrough) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LightText(
                    text = depScheduledTimeFormatted.takeIf { showDepStrikethrough }.orEmpty(),
                    variant = LightTextVariant.Fine,
                    lighten = true,
                    // strikethrough = showDepStrikethrough // FIXME: comment in when supported by Light SDK
                )

                LightText(
                    text = arrScheduledTimeFormatted.takeIf { showArrStrikethrough }.orEmpty(),
                    variant = LightTextVariant.Fine,
                    lighten = true,
                    // strikethrough = showArrStrikethrough // FIXME: comment in when supported by Light SDK
                )
            }
        }
    }
}