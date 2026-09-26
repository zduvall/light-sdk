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

import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightScrollView
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
class HistoryScreenViewModel : LightViewModel<Unit>()

class HistoryScreen(
    sealedActivity: SealedLightActivity
) : LightScreen<Unit, HistoryScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<HistoryScreenViewModel>
        get() = HistoryScreenViewModel::class.java

    override fun createViewModel(): HistoryScreenViewModel {
        return HistoryScreenViewModel()
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
                    center = LightTopBarCenter.Text("History"),           
                    modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
                )
                
                LightScrollView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 1f.gridUnitsAsDp())
                ) {
                    LightText(
                        text = "History Placeholder",
                        variant = LightTextVariant.Paragraph,
                    )
                }
                
                BottomBar(
                    active = FlightsTab.History,
                    onNavigate = { navigateTo(it) },
                )
            }
        }
    }
}