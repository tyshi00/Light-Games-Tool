package com.thelightphone.games.snake

import kotlin.random.Random

data class Point(val x: Int, val y: Int)

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    fun isOpposite(other: Direction): Boolean = dx == -other.dx && dy == -other.dy
}

/**
 * Classic Nokia-style Snake: solid walls (hitting an edge ends the game),
 * grows by one segment per food eaten, speed increases with score.
 * Framework-agnostic so it can be unit tested and reused outside Compose.
 */
class SnakeGame(
    val width: Int = 14,
    val height: Int = 20,
    private val random: Random = Random.Default,
) {
    var snake: List<Point> = listOf(Point(width / 2, height / 2))
        private set

    var direction: Direction = Direction.RIGHT
        private set

    private var pendingDirection: Direction = direction

    var food: Point = randomEmptyCell()
        private set

    var score: Int = 0
        private set

    var isGameOver: Boolean = false
        private set

    /** Queues a direction change. Ignored if it would immediately reverse the snake. */
    fun setDirection(newDirection: Direction) {
        if (!newDirection.isOpposite(direction)) {
            pendingDirection = newDirection
        }
    }

    /** Advances the simulation by one grid step. No-op once the game is over. */
    fun tick() {
        if (isGameOver) return
        direction = pendingDirection

        val head = snake.first()
        val newHead = Point(head.x + direction.dx, head.y + direction.dy)

        val hitWall = newHead.x !in 0 until width || newHead.y !in 0 until height
        val hitSelf = snake.contains(newHead)
        if (hitWall || hitSelf) {
            isGameOver = true
            return
        }

        val ateFood = newHead == food
        snake = listOf(newHead) + if (ateFood) snake else snake.dropLast(1)

        if (ateFood) {
            score += 1
            food = randomEmptyCell()
        }
    }

    fun reset() {
        snake = listOf(Point(width / 2, height / 2))
        direction = Direction.RIGHT
        pendingDirection = direction
        score = 0
        isGameOver = false
        food = randomEmptyCell()
    }

    private fun randomEmptyCell(): Point {
        while (true) {
            val candidate = Point(random.nextInt(width), random.nextInt(height))
            if (candidate !in snake) return candidate
        }
    }
}
