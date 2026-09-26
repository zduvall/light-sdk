package com.thelightphone.flights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.thelightphone.flights.BottomBar
import com.thelightphone.flights.FlightsTab
import com.thelightphone.flights.HistoryScreen
import com.thelightphone.flights.HomeScreen
import com.thelightphone.flights.ApiKeyScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp

/**
 * Flights Search Screen.
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
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )
                
                LightText(
                    text = "Flights Search Placeholder",
                    variant = LightTextVariant.Paragraph,
                    modifier = Modifier.padding(horizontal = 1f.gridUnitsAsDp())
                )
                
                BottomBar(
                    active = FlightsTab.Search,
                    onHistory = { navigateTo(screenFactory = { HistoryScreen(it) }) },
                    onSearch = { /* Already on search screen */ },
                    onHome = { navigateTo(screenFactory = { HomeScreen(it) }) },
                    onSettings = { navigateTo(screenFactory = { ApiKeyScreen(it) }) }
                )
            }
        }
    }
}