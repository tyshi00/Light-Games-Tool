package com.thelightphone.games

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

/**
 * Persists display preferences - currently just whether colors are inverted
 * (light background / dark text, vs. the default dark background / light text).
 * Backed by the same SDK-provided DataStore as the other stores in this tool.
 */
class SettingsStore(private val dataStore: DataStore<Preferences>) {

    private val invertColorsKey = booleanPreferencesKey("invert_colors")

    /** True if the user has chosen the inverted (light background) theme. */
    suspend fun isColorInverted(): Boolean =
        dataStore.data.first()[invertColorsKey] ?: false

    suspend fun setColorInverted(inverted: Boolean) {
        dataStore.edit { prefs -> prefs[invertColorsKey] = inverted }
    }
}
