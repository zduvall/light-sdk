package com.thelightphone.flights

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Shared repository for flight search data.
 */
class FlightsRepository(
    private val dataStore: DataStore<Preferences>
) {
    private val latestSearchKey = stringPreferencesKey("latest_search")

    val latestSearchFlow: Flow<String> = dataStore.data.map {
        it[latestSearchKey] ?: ""
    }

    suspend fun setLatestSearch(value: String) {
        dataStore.edit { it[latestSearchKey] = value }
    }
}