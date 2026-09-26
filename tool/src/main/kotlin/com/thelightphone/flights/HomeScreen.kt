package com.thelightphone.flights

import androidx.compose.runtime.Composable
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant

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