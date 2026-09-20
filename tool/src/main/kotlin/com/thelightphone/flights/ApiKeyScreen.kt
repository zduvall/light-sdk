package com.thelightphone.flights

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope

import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * ViewModel containing the data and behavior for ApiKeyScreen. Manages data
 * persistence asynchronously via shared [SettingsRepository].
 */
class ApiKeyScreenViewModel(
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

class ApiKeyScreen(
    sealedActivity: SealedLightActivity
) : LightScreen<Unit, ApiKeyScreenViewModel>(sealedActivity) {

    // Tell LightScreen which ViewModel belongs to this screen.
    override val viewModelClass: Class<ApiKeyScreenViewModel>
        get() = ApiKeyScreenViewModel::class.java

    // Create the ViewModel, passing the SDK-provided DataStore from lightContext.
    override fun createViewModel(): ApiKeyScreenViewModel {
        return ApiKeyScreenViewModel(lightContext.dataStore)
    }

    // Defines the UI for this screen using Jetpack Compose.
    @Composable
    override fun Content() {

        val apiKeyValue by viewModel.apiKey.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()
        
        // Apply the Light Phone theme to everything inside this block.
        LightTheme(colors = themeColors) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background)
            ) {

                LightTopBar(
                    center = LightTopBarCenter.Text("API Setup"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                LightScrollView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 1f.gridUnitsAsDp()),
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
                                initialCaps = apiKeyValue.isBlank(),
                            )
                            navigateTo(
                                screenFactory = { TextInputEditorScreen(it, editorRequest) },                             
                                resultCallback = { enteredKey ->
                                    viewModel.setApiKey(enteredKey) // save the key
                                    
                                    if (enteredKey.isBlank()) { 
                                        return@navigateTo // if no text, exit early
                                    }
                                    
                                    goBack() // if typed something, go to previous screen
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 0.75f.gridUnitsAsDp())
                    )
                    LightText(
                        text = "An API key can be retrieved by visiting https://rapidapi.com/ and creating a new key for \"AeroDataBox\".",
                        variant = LightTextVariant.Superfine,
                        lighten = true,
                    )
                }
            }
        }
    }
}