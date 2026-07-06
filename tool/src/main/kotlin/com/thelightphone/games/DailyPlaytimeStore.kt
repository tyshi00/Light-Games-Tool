package com.thelightphone.games

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Tracks cumulative playtime "today" (device-local date) for games where capping by
 * attempt count doesn't make sense - e.g. an arcade game like Snake, where a round
 * can end in a couple of seconds and the natural response is to retry immediately.
 * A time budget limits total engagement without punishing quick deaths.
 *
 * Backed by the same SDK-provided DataStore as [DailyLimitStore]; resets automatically
 * on the first usage recorded on a new calendar day.
 */
class DailyPlaytimeStore(private val dataStore: DataStore<Preferences>) {

    companion object {
        const val DEFAULT_DAILY_SECONDS = 20 * 60 // 20 minutes/day
    }

    private fun secondsKey(gameKey: String) = intPreferencesKey("${gameKey}_seconds_played")
    private fun dateKey(gameKey: String) = stringPreferencesKey("${gameKey}_seconds_date")

    private fun todayString(): String = LocalDate.now().toString()

    /** Seconds left today for [gameKey], out of [dailyBudgetSeconds]. Read-only. */
    suspend fun remainingSeconds(gameKey: String, dailyBudgetSeconds: Int = DEFAULT_DAILY_SECONDS): Int {
        val prefs = dataStore.data.first()
        val storedDate = prefs[dateKey(gameKey)]
        val usedToday = if (storedDate == todayString()) prefs[secondsKey(gameKey)] ?: 0 else 0
        return (dailyBudgetSeconds - usedToday).coerceIn(0, dailyBudgetSeconds)
    }

    /**
     * Records [elapsedSeconds] of additional usage for [gameKey] today (usage is clamped
     * so it never exceeds the budget) and returns the new remaining seconds, which will
     * be 0 once the budget is used up.
     */
    suspend fun addUsage(
        gameKey: String,
        elapsedSeconds: Int,
        dailyBudgetSeconds: Int = DEFAULT_DAILY_SECONDS,
    ): Int {
        var remaining = 0
        dataStore.edit { prefs ->
            val today = todayString()
            val storedDate = prefs[dateKey(gameKey)]
            val usedToday = if (storedDate == today) prefs[secondsKey(gameKey)] ?: 0 else 0

            val newUsed = (usedToday + elapsedSeconds).coerceAtMost(dailyBudgetSeconds)
            prefs[dateKey(gameKey)] = today
            prefs[secondsKey(gameKey)] = newUsed
            remaining = (dailyBudgetSeconds - newUsed).coerceAtLeast(0)
        }
        return remaining
    }
}
