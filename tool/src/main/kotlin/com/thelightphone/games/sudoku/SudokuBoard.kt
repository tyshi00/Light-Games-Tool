package com.thelightphone.games.sudoku

import kotlin.random.Random

enum class SudokuDifficulty(val givens: Int) {
    EASY(38),
    MEDIUM(32),
    HARD(26),
}

/**
 * A generated puzzle: [puzzle] is the 81-cell board shown to the player (0 = blank),
 * [solution] is the fully-solved 81-cell board used to validate entries.
 * Both are stored row-major: index = row * 9 + col.
 */
class SudokuBoard private constructor(
    val puzzle: IntArray,
    val solution: IntArray,
) {
    companion object {
        private const val SIZE = 9
        private const val CELLS = SIZE * SIZE

        /**
         * Generates a fresh, solvable, uniquely-determined puzzle.
         * 1. Fill a full valid grid with randomized backtracking.
         * 2. Remove cells one at a time, only keeping a removal if the puzzle
         *    still has exactly one solution (checked via a capped solution counter).
         */
        fun generate(difficulty: SudokuDifficulty, random: Random = Random.Default): SudokuBoard {
            val solved = generateSolvedGrid(random)
            val puzzle = solved.copyOf()

            val cellOrder = (0 until CELLS).shuffled(random)
            var removed = 0
            val target = CELLS - difficulty.givens

            for (index in cellOrder) {
                if (removed >= target) break

                val backup = puzzle[index]
                if (backup == 0) continue

                puzzle[index] = 0
                if (countSolutions(puzzle, limit = 2) == 1) {
                    removed++
                } else {
                    puzzle[index] = backup
                }
            }

            return SudokuBoard(puzzle, solved)
        }

        private fun generateSolvedGrid(random: Random): IntArray {
            val grid = IntArray(CELLS)
            fillGrid(grid, 0, random)
            return grid
        }

        private fun fillGrid(grid: IntArray, position: Int, random: Random): Boolean {
            if (position == CELLS) return true
            val row = position / SIZE
            val col = position % SIZE

            for (value in (1..9).shuffled(random)) {
                if (isValidPlacement(grid, row, col, value)) {
                    grid[position] = value
                    if (fillGrid(grid, position + 1, random)) return true
                    grid[position] = 0
                }
            }
            return false
        }

        /** Counts solutions up to [limit] (then stops early - we only ever need to know if it's exactly 1). */
        private fun countSolutions(grid: IntArray, limit: Int): Int {
            val working = grid.copyOf()
            var count = 0

            fun solve(position: Int): Boolean {
                if (position == CELLS) {
                    count++
                    return count >= limit
                }
                if (working[position] != 0) return solve(position + 1)

                val row = position / SIZE
                val col = position % SIZE
                for (value in 1..9) {
                    if (isValidPlacement(working, row, col, value)) {
                        working[position] = value
                        if (solve(position + 1)) return true
                        working[position] = 0
                    }
                }
                return false
            }

            solve(0)
            return count
        }

        private fun isValidPlacement(grid: IntArray, row: Int, col: Int, value: Int): Boolean {
            for (i in 0 until SIZE) {
                if (grid[row * SIZE + i] == value) return false
                if (grid[i * SIZE + col] == value) return false
            }
            val boxRow = (row / 3) * 3
            val boxCol = (col / 3) * 3
            for (r in boxRow until boxRow + 3) {
                for (c in boxCol until boxCol + 3) {
                    if (grid[r * SIZE + c] == value) return false
                }
            }
            return true
        }
    }
}
