package com.thelightphone.flights.model

data class UsageLimits(
    val requestsRemaining: Int = 0,
    val requestsReset: Long = 0L,
    val unitsRemaining: Int = 0,
    val unitsReset: Long = 0L
)
