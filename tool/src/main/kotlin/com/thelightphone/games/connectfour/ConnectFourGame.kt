package com.thelightphone.games.connectfour

enum class ConnectFourPlayer { WHITE, BLACK }

/** 7x6 grid (standard size). grid[row][col], row 0 = bottom. Framework-agnostic like the other games. */
class ConnectFourGame(
    val columns: Int = 7,
    val rows: Int = 6,
) {
    var grid: Array<Array<ConnectFourPlayer?>> = Array(rows) { arrayOfNulls(columns) }
        private set
    var currentPlayer: ConnectFourPlayer = ConnectFourPlayer.WHITE
        private set
    var winner: ConnectFourPlayer? = null
        private set
    var isDraw: Boolean = false
        private set
    var lastMoveRow: Int? = null
        private set
    var lastMoveCol: Int? = null
        private set

    fun columnFull(col: Int): Boolean = grid[rows - 1][col] != null

    /** Drops a piece into [col] (gravity fills the lowest empty row). Returns true if legal. */
    fun dropPiece(col: Int): Boolean {
        if (winner != null || isDraw) return false
        if (col !in 0 until columns || columnFull(col)) return false

        val row = (0 until rows).first { grid[it][col] == null }
        grid[row][col] = currentPlayer
        lastMoveRow = row
        lastMoveCol = col

        winner = checkWinner(grid, row, col, currentPlayer)
        isDraw = winner == null && (0 until columns).all { columnFull(it) }

        if (winner == null && !isDraw) {
            currentPlayer = if (currentPlayer == ConnectFourPlayer.WHITE) ConnectFourPlayer.BLACK else ConnectFourPlayer.WHITE
        }
        return true
    }

    fun reset() {
        grid = Array(rows) { arrayOfNulls(columns) }
        currentPlayer = ConnectFourPlayer.WHITE
        winner = null
        isDraw = false
        lastMoveRow = null
        lastMoveCol = null
    }

    companion object {
        private val DIRECTIONS = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)

        fun checkWinner(
            grid: Array<Array<ConnectFourPlayer?>>,
            row: Int,
            col: Int,
            player: ConnectFourPlayer,
        ): ConnectFourPlayer? {
            val rows = grid.size
            val cols = grid[0].size
            for ((dr, dc) in DIRECTIONS) {
                var count = 1
                var r = row + dr
                var c = col + dc
                while (r in 0 until rows && c in 0 until cols && grid[r][c] == player) {
                    count++; r += dr; c += dc
                }
                r = row - dr
                c = col - dc
                while (r in 0 until rows && c in 0 until cols && grid[r][c] == player) {
                    count++; r -= dr; c -= dc
                }
                if (count >= 4) return player
            }
            return null
        }
    }
}

