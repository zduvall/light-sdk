package com.thelightphone.flights

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UsageLimits(
    val requestsRemaining: Int = 0,
    val requestsReset: Long = 0L,
    val unitsRemaining: Int = 0,
    val unitsReset: Long = 0L
)

/**
 * Shared repository for persisting app-wide settings via DataStore.
 */
class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    private val apiKeyPref = stringPreferencesKey("rapid_api_key")
    private val requestsRemaining = intPreferencesKey("requests_remaining")
    private val requestsReset = longPreferencesKey("requests_reset")
    private val unitsRemaining = intPreferencesKey("units_remaining")
    private val unitsReset = longPreferencesKey("units_reset")
    
    val apiKeyFlow: Flow<String> = dataStore.data.map { it[apiKeyPref] ?: "" }

    val usageLimitsFlow: Flow<UsageLimits?> = dataStore.data.map { prefs ->
        val remaining = prefs[requestsRemaining]
        if (remaining == null) {
            null
        } else {
            UsageLimits(
                requestsRemaining = remaining,
                requestsReset = prefs[requestsReset] ?: 0L,
                unitsRemaining = prefs[unitsRemaining] ?: 0,
                unitsReset = prefs[unitsReset] ?: 0L
            )
        }
    }

    suspend fun setApiKey(value: String) {
        dataStore.edit { it[apiKeyPref] = value }
    }

    suspend fun setUsageLimits(limits: UsageLimits) {
        dataStore.edit { prefs ->
            prefs[requestsRemaining] = limits.requestsRemaining
            prefs[requestsReset] = limits.requestsReset
            prefs[unitsRemaining] = limits.unitsRemaining
            prefs[unitsReset] = limits.unitsReset
        }
    }
}