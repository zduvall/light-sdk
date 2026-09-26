package com.thelightphone.flights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter

/**
 * ViewModel containing the data for HomeScreen. 
 */
class HomeScreenViewModel : LightViewModel<Unit>()

@InitialScreen
class HomeScreen(
    sealedActivity: SealedLightActivity
) : LightScreen<Unit, HomeScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<HomeScreenViewModel>
        get() = HomeScreenViewModel::class.java

    override fun createViewModel(): HomeScreenViewModel {
        return HomeScreenViewModel()
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
                    center = LightTopBarCenter.Text("Home"),
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),                        
                )
                
                LightText(
                    text = "Home Placeholder",
                    variant = LightTextVariant.Paragraph,
                    modifier = Modifier.padding(horizontal = 1f.gridUnitsAsDp())
                )                
                
                BottomBar(
                    active = FlightsTab.Home,
                    onHistory = { navigateTo(screenFactory = { HistoryScreen(it) }) },
                    onSearch = { navigateTo(screenFactory = { FlightsSearchScreen(it) }) },
                    onHome = { /* Already on home screen */ },
                    onSettings = { navigateTo(screenFactory = { SettingsScreen(it) }) }
                )        
            }
        }
        
    }
}