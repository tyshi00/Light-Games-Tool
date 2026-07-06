package com.thelightphone.games

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

/**
 * Saves a single in-progress puzzle per game as an opaque string blob, so a player who
 * backs out mid-puzzle (intentionally or by accident) can resume exactly where they left
 * off - without it costing them another one of their daily plays. Each game is responsible
 * for encoding/decoding its own state into a string; this store just persists it.
 *
 * A game should clear its entry once the puzzle is solved, so the next fresh visit
 * correctly starts (and counts) a new puzzle.
 */
class ActivePuzzleStore(private val dataStore: DataStore<Preferences>) {

    private fun key(gameKey: String) = stringPreferencesKey("${gameKey}_active_puzzle")

    suspend fun load(gameKey: String): String? = dataStore.data.first()[key(gameKey)]

    suspend fun save(gameKey: String, blob: String) {
        dataStore.edit { prefs -> prefs[key(gameKey)] = blob }
    }

    suspend fun clear(gameKey: String) {
        dataStore.edit { prefs -> prefs.remove(key(gameKey)) }
    }
}
