package com.thelightphone.flights

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Shared repository for persisting app-wide settings via DataStore.
 */
object SettingsRepository {

    private val API_KEY_PREF = stringPreferencesKey("rapid_api_key")

    /**
     * Provides a cold, reactive data stream of the AeroDataBox API key.
     *
     * This stream will emit the current stored value immediately upon collection 
     * and will automatically push new values whenever the underlying storage changes.
     *
     * @param dataStore The [DataStore] instance where the preferences are managed.
     * @return A [Flow] emitting the active API key, or an empty string if no key is set.
     */
    fun apiKeyFlow(dataStore: DataStore<Preferences>): Flow<String> =
        dataStore.data.map { it[API_KEY_PREF] ?: "" }

    /**
     * Persists a new AeroDataBox API key safely to the local disk.
     *
     * This is a suspending function that performs asynchronous disk I/O operations 
     * without blocking the calling thread. It must be invoked from a coroutine scope.
     *
     * @param dataStore The [DataStore] instance where the preferences are managed.
     * @param value The plaintext string token to persist.
     */
    suspend fun setApiKey(dataStore: DataStore<Preferences>, value: String) {
        dataStore.edit { it[API_KEY_PREF] = value }
    }
}
