package com.thelightphone.games

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Tracks how many times a given game has been started "today" (device-local date)
 * and enforces a per-day cap. Backed by the DataStore instance the SDK already
 * provides via [com.thelightphone.sdk.SealedLightContext.dataStore], so no extra
 * permissions or storage setup are required.
 *
 * The counter resets automatically the first time a play is attempted on a new
 * calendar day - there's no background job needed.
 */
class DailyLimitStore(private val dataStore: DataStore<Preferences>) {

    companion object {
        const val DEFAULT_DAILY_LIMIT = 3
    }

    private fun countKey(gameKey: String) = intPreferencesKey("${gameKey}_play_count")
    private fun dateKey(gameKey: String) = stringPreferencesKey("${gameKey}_play_date")

    private fun todayString(): String = LocalDate.now().toString() // e.g. "2026-07-05"

    /** How many plays are left today for [gameKey], out of [limit]. Read-only, does not consume a play. */
    suspend fun remainingPlays(gameKey: String, limit: Int = DEFAULT_DAILY_LIMIT): Int {
        val prefs = dataStore.data.first()
        val storedDate = prefs[dateKey(gameKey)]
        val usedToday = if (storedDate == todayString()) prefs[countKey(gameKey)] ?: 0 else 0
        return (limit - usedToday).coerceIn(0, limit)
    }

    /**
     * Attempts to consume one play for [gameKey]. Returns `true` and records the play if
     * today's limit hasn't been reached yet. Returns `false` (and leaves state untouched)
     * if the user has already used all of today's plays.
     */
    suspend fun tryConsumePlay(gameKey: String, limit: Int = DEFAULT_DAILY_LIMIT): Boolean {
        var allowed = false
        dataStore.edit { prefs ->
            val today = todayString()
            val storedDate = prefs[dateKey(gameKey)]
            val usedToday = if (storedDate == today) prefs[countKey(gameKey)] ?: 0 else 0

            if (usedToday < limit) {
                prefs[dateKey(gameKey)] = today
                prefs[countKey(gameKey)] = usedToday + 1
                allowed = true
            }
        }
        return allowed
    }
}
