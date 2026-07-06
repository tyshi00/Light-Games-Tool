package com.thelightphone.games.wordsearch

import kotlin.random.Random

/** A word placed on the grid, recorded as its ordered list of (row, col) cells. */
data class PlacedWord(
    val word: String,
    val startRow: Int,
    val startCol: Int,
    val dRow: Int,
    val dCol: Int,
) {
    val cells: List<Pair<Int, Int>> =
        word.indices.map { i -> (startRow + dRow * i) to (startCol + dCol * i) }
}

class WordSearchPuzzle(
    val size: Int,
    val grid: Array<CharArray>,
    val words: List<PlacedWord>,
)

object WordSearchGenerator {

    // All 8 compass directions, so words can run in any straight line (incl. diagonals, reversed).
    private val DIRECTIONS = listOf(
        0 to 1, 1 to 0, 1 to 1, -1 to 1,
        0 to -1, -1 to 0, -1 to -1, 1 to -1,
    )

    private val WORD_BANK = listOf(
        "APPLE", "RIVER", "CLOUD", "TIGER", "PIANO", "BREAD", "CHAIR", "OCEAN",
        "MOUNTAIN", "GUITAR", "GARDEN", "WINTER", "PENCIL", "ISLAND", "CANDLE",
        "FOREST", "PLANET", "BRIDGE", "ROCKET", "DOLPHIN", "MARBLE", "SUNSET",
        "WHISTLE", "JOURNEY", "PUZZLE", "HARBOR", "COMPASS", "LANTERN", "MEADOW",
        "SPARROW", "GLACIER", "VOYAGE", "THUNDER", "BLOSSOM", "HORIZON", "CANYON",
        "COMET", "PEBBLE", "VELVET", "SHADOW", "CRYSTAL",
    )

    fun generate(size: Int = 11, wordCount: Int = 6, random: Random = Random.Default): WordSearchPuzzle {
        val chosenWords = WORD_BANK
            .filter { it.length <= size }
            .shuffled(random)
            .take(wordCount)
            .sortedByDescending { it.length } // place longer words first - they're harder to fit later

        val grid = Array(size) { CharArray(size) { ' ' } }
        val placed = mutableListOf<PlacedWord>()

        for (word in chosenWords) {
            placeWord(word, grid, size, random)?.let { placed.add(it) }
        }

        for (row in 0 until size) {
            for (col in 0 until size) {
                if (grid[row][col] == ' ') {
                    grid[row][col] = 'A' + random.nextInt(26)
                }
            }
        }

        return WordSearchPuzzle(size, grid, placed)
    }

    private fun placeWord(word: String, grid: Array<CharArray>, size: Int, random: Random): PlacedWord? {
        repeat(200) {
            val (dRow, dCol) = DIRECTIONS.random(random)
            val startRowRange = validStartRange(size, dRow, word.length)
            val startColRange = validStartRange(size, dCol, word.length)
            if (startRowRange.isEmpty() || startColRange.isEmpty()) return@repeat

            val startRow = startRowRange.random(random)
            val startCol = startColRange.random(random)

            if (fits(word, grid, startRow, startCol, dRow, dCol, size)) {
                for (i in word.indices) {
                    grid[startRow + dRow * i][startCol + dCol * i] = word[i]
                }
                return PlacedWord(word, startRow, startCol, dRow, dCol)
            }
        }
        return null
    }

    private fun validStartRange(size: Int, delta: Int, length: Int): IntRange = when {
        delta == 0 -> 0 until size
        delta > 0 -> 0 until (size - (length - 1))
        else -> (length - 1) until size
    }

    private fun fits(
        word: String,
        grid: Array<CharArray>,
        startRow: Int,
        startCol: Int,
        dRow: Int,
        dCol: Int,
        size: Int,
    ): Boolean {
        for (i in word.indices) {
            val r = startRow + dRow * i
            val c = startCol + dCol * i
            if (r !in 0 until size || c !in 0 until size) return false
            val existing = grid[r][c]
            if (existing != ' ' && existing != word[i]) return false
        }
        return true
    }
}
