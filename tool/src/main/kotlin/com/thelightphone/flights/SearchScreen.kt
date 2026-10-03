package com.thelightphone.flights

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope

import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
import com.thelightphone.sdk.ui.LightTextVariant

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Flights Search Screen.
 */
class SearchScreenViewModel(
    private val flightsRepository: FlightsRepository
) : LightViewModel<Unit>() {
    
    /** Expose the latest search query as UI state. */
    val latestSearch: StateFlow<String> = flightsRepository.latestSearchFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    /**
     * Persist the latest search query to local disk. 
     * @param value The latest search query string to persist.
     */
    fun setLatestSearch(value: String) {
        viewModelScope.launch {
            // NonCancellable ensures that the DataStore write completes
            // even if the ViewModel is destroyed by navigating away
            withContext(NonCancellable) {
                flightsRepository.setLatestSearch(value)
            }
        }
    }
}

class SearchScreen(
    sealedActivity: SealedLightActivity
) : LightScreen<Unit, SearchScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<SearchScreenViewModel>
        get() = SearchScreenViewModel::class.java

    override fun createViewModel(): SearchScreenViewModel {
        val flightsRepository = FlightsRepository(lightContext.dataStore)
        return SearchScreenViewModel(flightsRepository)
    }

    @Composable
    override fun Content() {
        val latestSearchValue by viewModel.latestSearch.collectAsState()
                
        TabScaffold(
            title = "Search",
            activeTab = FlightsTab.Search,
            onNavigate = { navigateTo(it) },
        ) {

            LightTextField(
                label = "Flight Code:",
                value = latestSearchValue,
                placeholder = "",
                onClick = {
                    val editorRequest = EditorRequest(
                        title = "Flight Code",
                        initialValue = latestSearchValue,
                    )
                    navigateTo(
                        screenFactory = { TextInputEditorScreen(it, editorRequest) },
                        resultCallback = viewModel::setLatestSearch
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.75f.gridUnitsAsDp())

            )

        }
    }
}

/**
 * Standardizes flight number by removing whitespace & converting to lowercase.
 */
fun standardizeFlightNumber(flightNumber: String): String {
    return flightNumber.replace("\\s".toRegex(), "").lowercase()
}
