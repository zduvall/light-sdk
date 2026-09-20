package com.thelightphone.flights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope

import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel containing the data for HomeScreen. 
 * Reads the API key to route the user to the correct initial destination.
 */
class HomeScreenViewModel(
    dataStore: DataStore<Preferences>
) : LightViewModel<Unit>() {

    // Read the API key as a StateFlow, defaulting to null while DataStore asynchronously loads.
    val apiKey: StateFlow<String?> = SettingsRepository.apiKeyFlow(dataStore)
        .stateIn( // convert the ordinary Flow into a StateFlow
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // null indicates "still loading"
        )
}

@InitialScreen // tells Light Phone SDK that this is the app's starting screen.
class HomeScreen(
    sealedActivity: SealedLightActivity
) : LightScreen<Unit, HomeScreenViewModel>(sealedActivity) {

    // Tell LightScreen which ViewModel belongs to this screen.
    override val viewModelClass: Class<HomeScreenViewModel>
        get() = HomeScreenViewModel::class.java

    // Create the ViewModel, passing the SDK-provided DataStore from lightContext.
    override fun createViewModel(): HomeScreenViewModel {
        return HomeScreenViewModel(lightContext.dataStore)
    }

    // Defines the UI for this screen using Jetpack Compose.
    @Composable
    override fun Content() {

        val apiKeyValue by viewModel.apiKey.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()
        
        // Route to appropriate screen once DataStore finishes loading
        LaunchedEffect(apiKeyValue) {
            // assign key if loaded, otherwise exit LauncedEffect block
            val key = apiKeyValue ?: return@LaunchedEffect 
            
            if (key.isBlank()) {
                navigateTo(screenFactory = { ApiKeyScreen(it) })
            } else {
                navigateTo(screenFactory = { FlightsSearchScreen(it) })
            }
        }
        
        // Apply the Light Phone theme to a blank background while routing resolves
        LightTheme(colors = themeColors) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background)
            )
        }
    }
}