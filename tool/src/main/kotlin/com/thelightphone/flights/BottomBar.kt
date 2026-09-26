package com.thelightphone.flights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.ui.LightColors
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIconConfiguration
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightSurfaceScheme
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable

enum class FlightsTab { History, Search, Home, Settings }

@Composable
fun BottomBar(
    active: FlightsTab,
    onHistory: () -> Unit,
    onSearch: () -> Unit,
    onHome: () -> Unit,
    onSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.9f.gridUnitsAsDp(), bottom = 0.8f.gridUnitsAsDp()),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        flightsNavIcon(LightIcons.AIRPLANE, "Home", active == FlightsTab.Home, onHome)
        flightsNavIcon(LightIcons.SEARCH, "Search", active == FlightsTab.Search, onSearch)
        flightsNavIcon(LightIcons.LIST, "History", active == FlightsTab.History, onHistory)
        flightsNavIcon(LightIcons.SETTINGS, "Settings", active == FlightsTab.Settings, onSettings)
    }
}

@Composable
private fun flightsNavIcon(
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
