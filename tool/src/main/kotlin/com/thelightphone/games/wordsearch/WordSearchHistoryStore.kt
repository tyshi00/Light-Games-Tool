package com.thelightphone.games.wordsearch

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

/**
 * Remembers the last several dozen words used across puzzles, so a fresh puzzle can avoid
 * picking words that just showed up recently - without this, random sampling from even a
 * few-hundred-word bank can feel repetitive within just a handful of sessions.
 */
class WordSearchHistoryStore(private val dataStore: DataStore<Preferences>) {

    companion object {
        /** Roughly 15 puzzles' worth of words (at 6 words/puzzle) before anything can repeat. */
        private const val MAX_HISTORY = 90
    }

    private val recentWordsKey = stringPreferencesKey("word_search_recent_words")

    suspend fun getRecentWords(): Set<String> {
        val stored = dataStore.data.first()[recentWordsKey] ?: ""
        return stored.split(",").filter { it.isNotBlank() }.toSet()
    }

    suspend fun recordUsedWords(words: List<String>) {
        dataStore.edit { prefs ->
            val current = (prefs[recentWordsKey] ?: "").split(",").filter { it.isNotBlank() }
            val updated = (current + words).takeLast(MAX_HISTORY)
            prefs[recentWordsKey] = updated.joinToString(",")
        }
    }
}
