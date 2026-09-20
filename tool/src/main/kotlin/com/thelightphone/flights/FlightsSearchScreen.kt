package com.thelightphone.flights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp

/**
 * ViewModel containing the data and behavior for the main Flights Search Screen.
 */
class FlightsSearchScreenViewModel : LightViewModel<Unit>()

class FlightsSearchScreen(
    sealedActivity: SealedLightActivity
) : LightScreen<Unit, FlightsSearchScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<FlightsSearchScreenViewModel>
        get() = FlightsSearchScreenViewModel::class.java

    override fun createViewModel(): FlightsSearchScreenViewModel {
        return FlightsSearchScreenViewModel()
    }

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        
        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background)
            ) {
                LightTopBar(
                    center = LightTopBarCenter.Text("Flights"),
                    rightButton = LightBarButton.LightIcon(
                        icon = LightIcons.SETTINGS,
                        onClick = {
                            // navigate to api key screen
                            navigateTo(
                                screenFactory = { ApiKeyScreen(it) }
                            )
                        }
                    ),                        
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )
                
                LightText(
                    text = "Flights Search Placeholder",
                    variant = LightTextVariant.Paragraph,
                    modifier = Modifier.padding(horizontal = 1f.gridUnitsAsDp())
                )
            }
        }
    }
}