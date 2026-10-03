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
    private val rapidApiKey = stringPreferencesKey("rapid_api_key")
    private val requestsRemainingKey = intPreferencesKey("requests_remaining")
    private val requestsResetKey = longPreferencesKey("requests_reset")
    private val unitsRemainingKey = intPreferencesKey("units_remaining")
    private val unitsResetKey = longPreferencesKey("units_reset")
    
    val apiKeyFlow: Flow<String> = dataStore.data.map { it[rapidApiKey] ?: "" }

    val usageLimitsFlow: Flow<UsageLimits?> = dataStore.data.map { prefs ->
        val remaining = prefs[requestsRemainingKey]
        if (remaining == null) {
            null
        } else {
            UsageLimits(
                requestsRemaining = remaining,
                requestsReset = prefs[requestsResetKey] ?: 0L,
                unitsRemaining = prefs[unitsRemainingKey] ?: 0,
                unitsReset = prefs[unitsResetKey] ?: 0L
            )
        }
    }

    suspend fun setApiKey(value: String) {
        dataStore.edit { it[rapidApiKey] = value }
    }

    suspend fun setUsageLimits(limits: UsageLimits) {
        dataStore.edit { prefs ->
            prefs[requestsRemainingKey] = limits.requestsRemaining
            prefs[requestsResetKey] = limits.requestsReset
            prefs[unitsRemainingKey] = limits.unitsRemaining
            prefs[unitsResetKey] = limits.unitsReset
        }
    }

    suspend fun clearUsageLimits() {
        dataStore.edit { prefs ->
            prefs.remove(requestsRemainingKey)
            prefs.remove(requestsResetKey)
            prefs.remove(unitsRemainingKey)
            prefs.remove(unitsResetKey)
        }
    }
}