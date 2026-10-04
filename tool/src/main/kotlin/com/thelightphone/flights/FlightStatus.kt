package com.thelightphone.flights

import kotlinx.serialization.Serializable

@Serializable
data class FlightStatus(
    val greatCircleDistance: GreatCircleDistance? = null,
    val departure: FlightEndpoint,
    val arrival: FlightEndpoint,
    val lastUpdatedUtc: String,
    val number: String,
    val callSign: String? = null,
    val status: String,
    val codeshareStatus: String? = null,
    val isCargo: Boolean? = null,
    val aircraft: Aircraft? = null,
    val airline: Airline? = null
)

@Serializable
data class GreatCircleDistance(
    val meter: Double,
    val km: Double,
    val mile: Double,
    val nm: Double,
    val feet: Double
)

@Serializable
data class FlightEndpoint(
    val airport: Airport,
    val scheduledTime: FlightTime? = null,
    val revisedTime: FlightTime? = null,
    val predictedTime: FlightTime? = null,
    val runwayTime: FlightTime? = null,
    val terminal: String? = null,
    val runway: String? = null,
    val quality: List<String> = emptyList()
)

@Serializable
data class Airport(
    val icao: String? = null,
    val iata: String? = null,
    val name: String,
    val shortName: String? = null,
    val municipalityName: String? = null,
    val location: Location? = null,
    val countryCode: String? = null,
    val timeZone: String? = null
)

@Serializable
data class Location(
    val lat: Double,
    val lon: Double
)

@Serializable
data class FlightTime(
    val utc: String,
    val local: String
)

@Serializable
data class Aircraft(
    val model: String? = null
)

@Serializable
data class Airline(
    val name: String? = null,
    val iata: String? = null,
    val icao: String? = null
)
