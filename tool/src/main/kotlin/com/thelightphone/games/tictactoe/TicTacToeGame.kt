package com.thelightphone.games.tictactoe

enum class TicTacToePlayer { X, O }

private val WIN_LINES = listOf(
    listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
    listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // columns
    listOf(0, 4, 8), listOf(2, 4, 6), // diagonals
)

/** 3x3 board, framework-agnostic so it's testable outside Compose, matching the other games. */
class TicTacToeGame {

    var board: Array<TicTacToePlayer?> = arrayOfNulls(9)
        private set
    var currentPlayer: TicTacToePlayer = TicTacToePlayer.X
        private set
    var winner: TicTacToePlayer? = null
        private set
    var isDraw: Boolean = false
        private set

    /** Returns true if the move was legal and applied. */
    fun makeMove(index: Int): Boolean {
        if (winner != null || isDraw || board[index] != null) return false

        board = board.copyOf().also { it[index] = currentPlayer }
        winner = findWinner(board)
        isDraw = winner == null && board.all { it != null }

        if (winner == null && !isDraw) {
            currentPlayer = if (currentPlayer == TicTacToePlayer.X) TicTacToePlayer.O else TicTacToePlayer.X
        }
        return true
    }

    fun reset() {
        board = arrayOfNulls(9)
        currentPlayer = TicTacToePlayer.X
        winner = null
        isDraw = false
    }

    companion object {
        fun findWinner(board: Array<TicTacToePlayer?>): TicTacToePlayer? {
            for (line in WIN_LINES) {
                val (a, b, c) = line
                if (board[a] != null && board[a] == board[b] && board[b] == board[c]) return board[a]
            }
            return null
        }
    }
}

