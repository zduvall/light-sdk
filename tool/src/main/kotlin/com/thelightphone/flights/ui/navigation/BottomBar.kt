package com.thelightphone.flights.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thelightphone.flights.ui.history.HistoryScreen
import com.thelightphone.flights.ui.home.HomeScreen
import com.thelightphone.flights.ui.search.SearchScreen
import com.thelightphone.flights.ui.settings.SettingsScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightColors
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIconConfiguration
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightSurfaceScheme
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable

private data class TabConfig(
    val tab: FlightsTab,
    val icon: LightIconConfiguration,
    val title: String,
    val screenFactory: (SealedLightActivity) -> SimpleLightScreen<Unit>,
)

private val tabs = listOf(
    TabConfig(FlightsTab.Home, LightIcons.AIRPLANE, "Home", ::HomeScreen),
    TabConfig(FlightsTab.Search, LightIcons.SEARCH, "Search", ::SearchScreen),
    TabConfig(FlightsTab.History, LightIcons.LIST, "History", ::HistoryScreen),
    TabConfig(FlightsTab.Settings, LightIcons.SETTINGS, "Settings", ::SettingsScreen),
)

@Composable
fun BottomBar(
    active: FlightsTab,
    onNavigate: ((SealedLightActivity) -> SimpleLightScreen<Unit>) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.9f.gridUnitsAsDp(), bottom = 0.8f.gridUnitsAsDp()),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { config ->
            NavIcon(
                icon = config.icon,
                description = config.title,
                active = active == config.tab,
                onClick = {
                    if (active == config.tab) return@NavIcon
                    onNavigate(config.screenFactory)
                }
            )
        }
    }
}

@Composable
private fun NavIcon(
    icon: LightIconConfiguration,
    description: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val colors = LightThemeTokens.colors
    val scheme = LightThemeTokens.surfaceScheme
    val typography = LightThemeTokens.typography
    val tintedColors = if (active) colors else mutedColors(colors, scheme)

    LightTheme(colors = tintedColors, typography = typography, surfaceScheme = scheme) {
        LightIcon(
            icon = icon,
            size = 2f,
            contentDescription = description,
            modifier = Modifier.lightClickable(onClick = onClick),
        )
    }
}

private fun mutedColors(colors: LightColors, scheme: LightSurfaceScheme): LightColors {
    val alpha = if (scheme == LightSurfaceScheme.Dark) 0.4f else 0.38f
    return colors.copy(content = colors.content.copy(alpha = alpha))
}
