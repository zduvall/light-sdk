package com.thelightphone.flights

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel containing the data and behavior for SettingsScreen. Manages data
 * persistence asynchronously via shared [SettingsRepository].
 */
class SettingsScreenViewModel(
    private val dataStore: DataStore<Preferences>
) : LightViewModel<Unit>() {

    // Read the API key as a StateFlow, defaulting to empty string if not yet set.
    val apiKey: StateFlow<String> = SettingsRepository.apiKeyFlow(dataStore)
        .stateIn( // convert the ordinary Flow into a StateFlow
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    /**
     * Persist AeroDataBox API key to local disk. 
     * @param value The plaintext string token to persist.
     */
    fun setApiKey(value: String) {
        viewModelScope.launch {
            // NonCancellable ensures that the DataStore write completes
            // even if the ViewModel is destroyed by navigating away
            withContext(NonCancellable) {
                SettingsRepository.setApiKey(dataStore, value)
            }
        }
    }
}

class SettingsScreen(
    sealedActivity: SealedLightActivity
) : LightScreen<Unit, SettingsScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<SettingsScreenViewModel>
        get() = SettingsScreenViewModel::class.java

    override fun createViewModel(): SettingsScreenViewModel {
        return SettingsScreenViewModel(lightContext.dataStore)
    }

    @Composable
    override fun Content() {
        val apiKeyValue by viewModel.apiKey.collectAsState()

        TabScaffold(
            title = "Settings",
            activeTab = FlightsTab.Settings,
            onNavigate = { navigateTo(it) },
        ) {
            LightText(
                text = "Provide your AeroDataBox RapidAPI key to start searching for flights.",
                variant = LightTextVariant.Paragraph,
            )
            LightTextField(
                label = "API Key:",
                value = apiKeyValue,
                placeholder = "",
                onClick = {
                    val editorRequest = EditorRequest(
                        title = "AeroDataBox RapidAPI Key",
                        initialValue = apiKeyValue,
                    )
                    navigateTo(
                        screenFactory = { TextInputEditorScreen(it, editorRequest) },
                        resultCallback = viewModel::setApiKey
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.75f.gridUnitsAsDp())
            )
            LightText(
                text = """
                    To obtain an API key, create an account on RapidAPI
                    and subscribe to the "AeroDataBox" service. As of
                    September 2026, a free tier is available offering up
                    to 1,600 requests and 400 API units monthly, which
                    should be sufficient for regular personal use. For
                    the latest rate limits and pricing, visit
                    https://rapidapi.com/aedbx-aedbx/api/aerodatabox/pricing.
                """.trimIndent().replace("\n", " "),
                variant = LightTextVariant.Superfine,
                modifier = Modifier.padding(
                    start = 0.75f.gridUnitsAsDp(),
                    end = 0.75f.gridUnitsAsDp(),
                    bottom = 0.75f.gridUnitsAsDp()
                ),
                align = TextAlign.Justify,
                lighten = true,
            )
        }
    }
}