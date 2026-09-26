package com.thelightphone.flights

import androidx.compose.runtime.Composable
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant

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
        TabScaffold(
            title = "History",
            activeTab = FlightsTab.History,
            onNavigate = { navigateTo(it) },
        ) {
            LightText(
                text = "History Placeholder",
                variant = LightTextVariant.Paragraph,
            )
        }
    }
}