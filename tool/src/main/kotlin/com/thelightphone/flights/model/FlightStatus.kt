package com.thelightphone.flights.model

import kotlinx.serialization.Serializable

@Serializable
data class FlightStatus(
    val greatCircleDistance: Distance? = null,
    val departure: FlightEndpoint,
    val arrival: FlightEndpoint,
    val flightPlan: FlightPlan? = null,
    val lastUpdatedUtc: String,
    val number: String,
    val callSign: String? = null,
    val status: String,
    val codeshareStatus: String,
    val isCargo: Boolean,
    val aircraft: Aircraft? = null,
    val airline: Airline? = null,
    val location: FlightLocation? = null
)

@Serializable
data class FlightEndpoint(
    val airport: Airport,
    val scheduledTime: FlightTime? = null,
    val revisedTime: FlightTime? = null,
    val predictedTime: FlightTime? = null,
    val runwayTime: FlightTime? = null,
    val terminal: String? = null,
    val checkInDesk: String? = null,
    val gate: String? = null,
    val baggageBelt: String? = null,
    val runway: String? = null,
    val quality: List<String>
)

@Serializable
data class Airport(
    val icao: String? = null,
    val iata: String? = null,
    val localCode: String? = null,
    val name: String,
    val shortName: String? = null,
    val municipalityName: String? = null,
    val location: Location? = null,
    val countryCode: String? = null,
    val timeZone: String? = null
)
