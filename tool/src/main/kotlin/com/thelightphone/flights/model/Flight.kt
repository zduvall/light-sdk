package com.thelightphone.flights.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Flight(
    val greatCircleDistance: Distance? = null,
    val departure: FlightEndpoint,
    val arrival: FlightEndpoint,
    val flightPlan: FlightPlan? = null,
    val lastUpdatedUtc: String,
    val number: String,
    val callSign: String? = null,
    val status: FlightStatus,
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

@Serializable
enum class FlightStatus {
    @SerialName("Unknown")
    UNKNOWN,

    @SerialName("Expected")
    EXPECTED,

    @SerialName("EnRoute")
    EN_ROUTE,

    @SerialName("CheckIn")
    CHECK_IN,

    @SerialName("Boarding")
    BOARDING,

    @SerialName("GateClosed")
    GATE_CLOSED,

    @SerialName("Departed")
    DEPARTED,

    @SerialName("Delayed")
    DELAYED,

    @SerialName("Approaching")
    APPROACHING,

    @SerialName("Arrived")
    ARRIVED,

    @SerialName("Canceled")
    CANCELED,

    @SerialName("Diverted")
    DIVERTED,

    @SerialName("CanceledUncertain")
    CANCELED_UNCERTAIN
}