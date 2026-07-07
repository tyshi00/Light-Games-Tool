package com.thelightphone.games

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

/**
 * Persists whether each game shows up on the home screen. Defaults to visible -
 * a game only disappears once the user explicitly hides it from Settings.
 */
class GameVisibilityStore(private val dataStore: DataStore<Preferences>) {

    private fun key(gameKey: String) = booleanPreferencesKey("${gameKey}_visible")

    suspend fun isVisible(gameKey: String): Boolean =
        dataStore.data.first()[key(gameKey)] ?: true

    suspend fun setVisible(gameKey: String, visible: Boolean) {
        dataStore.edit { prefs -> prefs[key(gameKey)] = visible }
    }
}
