package com.thelightphone.flights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.lightClickable
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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel containing the data and behavior for HomeScreen.
 * Use for screen-specific state and behavior
 */
class HomeScreenViewModel : LightViewModel<Unit>() {
    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    fun setApiKey(value: String) {
        _apiKey.value = value
    }
}


@InitialScreen // tells Light Phone SDK that this is the app's starting screen.
class HomeScreen(
    sealedActivity: SealedLightActivity
) : LightScreen<Unit, HomeScreenViewModel>(sealedActivity) {

    // Tell LightScreen which ViewModel belongs to this screen.
    override val viewModelClass: Class<HomeScreenViewModel>
        get() = HomeScreenViewModel::class.java

    // Create the ViewModel used by this screen.
    override fun createViewModel(): HomeScreenViewModel {
        return HomeScreenViewModel()
    }

    // Defines the UI for this screen using Jetpack Compose.
    @Composable
    override fun Content() {

        // Collect the current Light Phone theme colors.
        //
        // Keeping this reactive means the UI will automatically update
        // if the theme changes.
        val themeColors by LightThemeController.colors.collectAsState()

        val apiKeyValue by viewModel.apiKey.collectAsState()
        
        // Apply the Light Phone theme to everything inside this block.
        LightTheme(colors = themeColors) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background)
            ) {

                LightTopBar(
                    center = LightTopBarCenter.Text("Flights"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )

                LightScrollView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 1f.gridUnitsAsDp()),
                ) {

                    LightText(
                        text = "Track live flight statuses.",
                        variant = LightTextVariant.Paragraph,
                        modifier = Modifier.padding(vertical = 0.75f.gridUnitsAsDp()),                        
                    )
                    LightTextField(
                        label = "AeroDataBox RapidAPI Key:",
                        value = apiKeyValue,
                        placeholder = "API key",
                        onClick = {
                            val editorRequest = EditorRequest(
                                title = "AeroDataBox RapidAPI Key",
                                initialValue = apiKeyValue,
                                initialCaps = apiKeyValue.isBlank(),
                            )
                            navigateTo(
                                screenFactory = { TextInputEditorScreen(it, editorRequest) },
                                resultCallback = { viewModel.setApiKey(it) }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 1f.gridUnitsAsDp(),
                                vertical = 0.75f.gridUnitsAsDp()
                            ),
                    )
                    LightText(
                        text = "An AeroDataBox RapidAPI key is required to use this app. To retrieve an API key, sign up on RapidAPI and create a new key for AeroDataBox.",
                        variant = LightTextVariant.Superfine,
                        lighten = true,
                    )
                }
            }
        }
    }
}
