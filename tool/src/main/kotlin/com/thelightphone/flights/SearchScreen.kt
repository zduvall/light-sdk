package com.thelightphone.flights

import androidx.compose.runtime.Composable
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant

/**
 * Flights Search Screen.
 */
class SearchScreenViewModel : LightViewModel<Unit>()

class SearchScreen(
    sealedActivity: SealedLightActivity
) : LightScreen<Unit, SearchScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<SearchScreenViewModel>
        get() = SearchScreenViewModel::class.java

    override fun createViewModel(): SearchScreenViewModel {
        return SearchScreenViewModel()
    }

    @Composable
    override fun Content() {
        TabScaffold(
            title = "Search",
            activeTab = FlightsTab.Search,
            onNavigate = { navigateTo(it) },
        ) {
            LightText(
                text = "Flights Search Placeholder",
                variant = LightTextVariant.Paragraph,
            )
        }
    }
}