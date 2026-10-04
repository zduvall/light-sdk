package com.thelightphone.flights.model

data class EditorRequest(
    val title: String,
    val initialValue: String,
    val initialCaps: Boolean = false,
)
