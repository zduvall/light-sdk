package com.thelightphone.flights.model

import kotlinx.serialization.Serializable

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
    val reg: String? = null,
    val modeS: String? = null,
    val model: String? = null,
    val image: Resource? = null
)

@Serializable
data class Airline(
    val name: String,
    val iata: String? = null,
    val icao: String? = null
)

@Serializable
data class Resource(
    val url: String,
    val webUrl: String? = null,
    val author: String? = null,
    val title: String? = null,
    val description: String? = null,
    val license: String,
    val htmlAttributions: List<String>? = null
)

@Serializable
data class FlightLocation(
    val pressureAltitude: Distance,
    val altitude: Distance,
    val pressure: Pressure,
    val groundSpeed: Speed,
    val trueTrack: Azimuth,
    val vsiFpm: Int? = null,
    val reportedAtUtc: String,
    val lat: Double,
    val lon: Double
)

@Serializable
data class Distance(
    val meter: Double,
    val km: Double,
    val mile: Double,
    val nm: Double,
    val feet: Double
)

@Serializable
data class Pressure(
    val hPa: Double,
    val inHg: Double,
    val mmHg: Double
)

@Serializable
data class Speed(
    val kt: Double,
    val kmPerHour: Double,
    val miPerHour: Double,
    val meterPerSecond: Double
)

@Serializable
data class Azimuth(
    val deg: Double,
    val rad: Double
)

@Serializable
data class FlightPlan(
    val flightRules: String? = null,
    val flightType: String? = null,
    val revisionNo: Int? = null,
    val status: String? = null,
    val route: String,
    val altitude: FlightPlanDistance? = null,
    val airspeed: FlightPlanSpeed? = null,
    val lastUpdatedUtc: String
)

@Serializable
data class FlightPlanDistance(
    val requested: Distance? = null,
    val assigned: Distance? = null
)

@Serializable
data class FlightPlanSpeed(
    val requested: Speed? = null,
    val assigned: Speed? = null
)
