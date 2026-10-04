package com.thelightphone.flights.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.thelightphone.flights.data.AeroDataBoxClient
import com.thelightphone.flights.data.SettingsRepository
import com.thelightphone.flights.model.EditorRequest
import com.thelightphone.flights.model.UsageLimits
import com.thelightphone.flights.ui.editor.TextInputEditorScreen
import com.thelightphone.flights.ui.navigation.FlightsTab
import com.thelightphone.flights.ui.navigation.TabScaffold
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.lightClickable
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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

            if (apiKeyValue.isNotEmpty()) {
                ApiKeyStatus(
                    isLoading = viewModel.isLoading,
                    errorMessage = viewModel.errorMessage,
                    usageLimits = currentLimits,
                    onCheckKey = viewModel::fetchAndSetUsageLimits,
                )
            }
        }
    }
}

/**
 * Displays the "Check API Key" button and a status line showing
 * loading state, error messages, or fetched usage limits.
 */
@Composable
private fun ApiKeyStatus(
    isLoading: Boolean,
    errorMessage: String?,
    usageLimits: UsageLimits?,
    onCheckKey: () -> Unit,
) {
    // Button to check API key and fetch usage limits
    LightText(
        text = "Check API Key",
        variant = LightTextVariant.Copy,
        modifier = Modifier
            .padding(bottom = 0.5f.gridUnitsAsDp())
            .lightClickable(
                onClick = onCheckKey,
                enabled = !isLoading
            ),
        lighten = isLoading,
    )

    // Determine status text from loading/error/usage limits
    val statusText = when {
        isLoading -> "Loading..."
        errorMessage != null -> errorMessage
        usageLimits != null -> {
            val requestsResetDate = dateFromNowPlusSeconds(usageLimits.requestsReset)
            val unitsResetDate = dateFromNowPlusSeconds(usageLimits.unitsReset)
            "Requests Remaining: ${usageLimits.requestsRemaining}\n" +
                    "Requests Reset: $requestsResetDate\n" +
                    "Units Remaining: ${usageLimits.unitsRemaining}\n" +
                    "Units Reset: $unitsResetDate"
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

/** Returns [LocalDate] that is [seconds] from now (formatted as yyyy-mm-dd) */
private fun dateFromNowPlusSeconds(seconds: Long): LocalDate {
    val zone = ZoneId.systemDefault()
    return Instant.now().plusSeconds(seconds).atZone(zone).toLocalDate()
}
