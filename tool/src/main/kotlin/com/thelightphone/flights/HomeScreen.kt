package com.thelightphone.flights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens

/**
 * ViewModel containing the data and behavior for HomeScreen.
 * Use for screen-specific state and behavior
 */
class HomeScreenViewModel : LightViewModel<Unit>()


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

        // Apply the Light Phone theme to everything inside this block.
        LightTheme(colors = themeColors) {

            // Column lays its children out vertically.
            Column(
                modifier = Modifier
                    // Fill the available screen.
                    .fillMaxSize()

                    // Use the background color from the Light Phone theme.
                    .background(LightThemeTokens.colors.background)

                    // Add space around the contents.
                    .padding(32.dp)
            ) {

                // App title.
                LightText(
                    text = "Flights",
                    variant = LightTextVariant.Heading,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                // Briefly explains what Flights does and how it accesses
                // flight data.
                LightText(
                    text = "Track live flight statuses.",
                    variant = LightTextVariant.Paragraph,
                    lighten = true,
                    modifier = Modifier.padding(bottom = 16.dp),                        
                )
                LightText(
                    text = "An AeroDataBox RapidAPI key is required to use this app.",
                    variant = LightTextVariant.Paragraph,
                    lighten = true,
                    modifier = Modifier.padding(bottom = 16.dp),                        
                )
                LightText(
                    text = "To retrieve an API key, sign up on RapidAPI and create a new key for AeroDataBox.",
                    variant = LightTextVariant.Superfine,
                    lighten = true,
                )
            }
        }
    }
}
