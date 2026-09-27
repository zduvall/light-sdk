package com.thelightphone.flights

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.lightClickable
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * ViewModel containing the data and behavior for SettingsScreen. Manages data
 * persistence asynchronously via shared [SettingsRepository].
 */
class SettingsScreenViewModel(
    private val settingsRepository: SettingsRepository,
    private val aeroDataBoxClient: AeroDataBoxClient
) : LightViewModel<Unit>() {

    // Read the API key as a StateFlow, defaulting to empty string if not yet set.
    val apiKey: StateFlow<String> = settingsRepository.apiKeyFlow
        .stateIn( // convert the ordinary Flow into a StateFlow
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )
        
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    /**
     * Persist AeroDataBox API key to local disk. 
     * @param value The plaintext string token to persist.
     */
    fun setApiKey(value: String) {
        viewModelScope.launch {
            // NonCancellable ensures that the DataStore write completes
            // even if the ViewModel is destroyed by navigating away
            withContext(NonCancellable) {
                settingsRepository.setApiKey(value)
            }
        }
    }
    
    // Read the usage limit object as a StateFlow, defaulting to null if not yet set.
    val usageLimits: StateFlow<UsageLimits?> = settingsRepository.usageLimitsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /** Persist updated usage limits to local disk. 
     * @param limits The [UsageLimits] instance containing the latest usage stats.
     */
    fun setUsageLimits(limits: UsageLimits) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                settingsRepository.setUsageLimits(limits)
            }
        }
    }
    
    fun fetchAndSetUsageLimits() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                when (val result = aeroDataBoxClient.fetchUsageLimits()) {
                    is ApiResult.Success -> {
                        setUsageLimits(result.data)
                    }
                    is ApiResult.Error.MissingAuth -> {
                        errorMessage = "Missing API key. Please provide your AeroDataBox RapidAPI key."
                    }
                    is ApiResult.Error.Unauthorized -> {
                        errorMessage = "Invalid API key. Please check your AeroDataBox RapidAPI key."
                    }
                    is ApiResult.Error.RateLimited -> {
                        errorMessage = "Rate limit exceeded. Please try again later."
                    }
                    // Comment in for debugging HTTP and network errors:
                    // is ApiResult.Error.Http -> {
                    //     errorMessage = "HTTP ${result.code}: ${result.message}"
                    // }
                    // is ApiResult.Error.Network -> {
                    //     errorMessage = "Network error: ${result.throwable.localizedMessage ?: result.throwable.message ?: "Unknown error"}"
                    // }                    
                    else -> {
                        errorMessage = "An error occurred while fetching usage limits. Please try again."
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }
}

class SettingsScreen(
    sealedActivity: SealedLightActivity
) : LightScreen<Unit, SettingsScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<SettingsScreenViewModel>
        get() = SettingsScreenViewModel::class.java

    override fun createViewModel(): SettingsScreenViewModel {
        val settingsRepository = SettingsRepository(lightContext.dataStore)
        val apiClient = AeroDataBoxClient(settingsRepository = settingsRepository)

        return SettingsScreenViewModel(settingsRepository, apiClient)
    }

    @Composable
    override fun Content() {
        val apiKeyValue by viewModel.apiKey.collectAsState()
        val currentLimits by viewModel.usageLimits.collectAsState()
        
        TabScaffold(
            title = "Settings",
            activeTab = FlightsTab.Settings,
            onNavigate = { navigateTo(it) },
        ) {
            LightText(
                text = "Provide your AeroDataBox RapidAPI key to start searching for flights.",
                variant = LightTextVariant.Paragraph,
            )
            LightTextField(
                label = "API Key:",
                value = apiKeyValue,
                placeholder = "",
                onClick = {
                    val editorRequest = EditorRequest(
                        title = "AeroDataBox RapidAPI Key",
                        initialValue = apiKeyValue,
                    )
                    navigateTo(
                        screenFactory = { TextInputEditorScreen(it, editorRequest) },
                        resultCallback = viewModel::setApiKey
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.75f.gridUnitsAsDp())
            )
            LightText(
                text = """
                    To obtain an API key, create an account on RapidAPI
                    and subscribe to the "AeroDataBox" service. As of
                    September 2026, a free tier is available offering up
                    to 1,600 requests and 400 API units monthly, which
                    should be sufficient for regular personal use. For
                    the latest rate limits and pricing, visit
                    https://rapidapi.com/aedbx-aedbx/api/aerodatabox/pricing.
                """.trimIndent().replace("\n", " "),
                variant = LightTextVariant.Superfine,
                modifier = Modifier.padding(
                    start = 0.75f.gridUnitsAsDp(),
                    end = 0.75f.gridUnitsAsDp(),
                    bottom = 1f.gridUnitsAsDp()
                ),
                align = TextAlign.Justify,
                lighten = true,
            )
            
            // Button to check API key and fetch usage limits
            LightText(
                text = "Check API Key",
                variant = LightTextVariant.Copy,
                modifier = Modifier
                    .padding(bottom = 0.5f.gridUnitsAsDp())
                    .lightClickable(
                        onClick = { viewModel.fetchAndSetUsageLimits() },
                        enabled = !viewModel.isLoading
                    ),
                lighten = viewModel.isLoading,
            )
            
            // Determine status text from loading/error/usage limits
            val limits = currentLimits
            val statusText = when {
                viewModel.isLoading -> "Loading..."
                viewModel.errorMessage != null -> viewModel.errorMessage
                limits != null -> {
                    "Requests Remaining: ${limits.requestsRemaining}\n" +
                    "Requests Reset: ${limits.requestsReset}\n" +
                    "Units Remaining: ${limits.unitsRemaining}\n" +
                    "Units Reset: ${limits.unitsReset}"
                }
                else -> null
            }
            
            statusText?.let { text ->
                LightText(
                    text = text,
                    variant = LightTextVariant.Fine,
                    modifier = Modifier.padding(
                        start = 0.75f.gridUnitsAsDp(),
                        end = 0.75f.gridUnitsAsDp(),
                        bottom = 0.75f.gridUnitsAsDp()
                    ),
                    align = TextAlign.Justify,
                    lighten = true,
                )
            }
        }
    }
}