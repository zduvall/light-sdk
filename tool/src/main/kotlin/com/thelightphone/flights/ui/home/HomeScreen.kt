package com.thelightphone.flights.ui.home

import androidx.compose.runtime.Composable
import com.thelightphone.flights.ui.navigation.FlightsTab
import com.thelightphone.flights.ui.navigation.TabScaffold
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant

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
        TabScaffold(
            title = "Home",
            activeTab = FlightsTab.Home,
            onNavigate = { navigateTo(it) },
        ) {
            LightText(
                text = "Home Placeholder",
                variant = LightTextVariant.Paragraph,
            )
        }
    }
}
