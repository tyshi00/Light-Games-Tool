package com.thelightphone.games.snake

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewModelScope
import com.thelightphone.games.DailyPlaytimeStore
import com.thelightphone.games.GameBudgets
import com.thelightphone.games.GameKeys
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val MIN_TICK_MS = 70L
private const val START_TICK_MS = 220L
private const val TICK_STEP_MS = 6L
private const val SWIPE_THRESHOLD_PX = 8f
private const val BUDGET_TICK_MS = 1000L

sealed class SnakeUiState {
    object CheckingBudget : SnakeUiState()
    object TimeUp : SnakeUiState()

    data class Playing(
        val snake: List<Point>,
        val food: Point,
        val score: Int,
        val isGameOver: Boolean,
        val width: Int,
        val height: Int,
        val remainingSeconds: Int,
    ) : SnakeUiState()
}

class SnakeScreenViewModel(
    private val dailyPlaytimeStore: DailyPlaytimeStore,
) : LightViewModel<Unit>() {

    private val game = SnakeGame()
    private var loopJob: Job? = null
    private var budgetJob: Job? = null
    private var hasStarted = false

    private val _state = MutableStateFlow<SnakeUiState>(SnakeUiState.CheckingBudget)
    val state: StateFlow<SnakeUiState> = _state

    override fun onScreenShow(screen: SimpleLightScreen<Unit>) {
        super.onScreenShow(screen)
        if (!hasStarted) {
            hasStarted = true
            viewModelScope.launch {
                val remaining = dailyPlaytimeStore.remainingSeconds(GameKeys.SNAKE, GameBudgets.SNAKE_SECONDS)
                if (remaining <= 0) {
                    _state.value = SnakeUiState.TimeUp
                } else {
                    _state.value = snapshot(remaining)
                    startLoop()
                    startBudgetTicker(remaining)
                }
            }
        } else {
            val current = _state.value
            if (current is SnakeUiState.Playing) {
                startLoop()
                startBudgetTicker(current.remainingSeconds)
            }
        }
    }

    override fun onScreenHide(screen: SimpleLightScreen<Unit>) {
        super.onScreenHide(screen)
        loopJob?.cancel()
        budgetJob?.cancel()
    }

    override fun onAppPause() {
        super.onAppPause()
        loopJob?.cancel()
        budgetJob?.cancel()
    }

    private fun startLoop() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            while (isActive) {
                val current = _state.value as? SnakeUiState.Playing
                if (current != null && !game.isGameOver) {
                    game.tick()
                    _state.value = snapshot(current.remainingSeconds)
                }
                val score = (_state.value as? SnakeUiState.Playing)?.score ?: 0
                val speed = (START_TICK_MS - score * TICK_STEP_MS).coerceAtLeast(MIN_TICK_MS)
                delay(speed)
            }
        }
    }

    /** Ticks once per real second, deducting from - and persisting - today's Snake budget. */
    private fun startBudgetTicker(startRemaining: Int) {
        budgetJob?.cancel()
        budgetJob = viewModelScope.launch {
            var remaining = startRemaining
            while (isActive && remaining > 0) {
                delay(BUDGET_TICK_MS)
                remaining = dailyPlaytimeStore.addUsage(GameKeys.SNAKE, elapsedSeconds = 1, dailyBudgetSeconds = GameBudgets.SNAKE_SECONDS)
                val current = _state.value as? SnakeUiState.Playing ?: continue
                _state.value = current.copy(remainingSeconds = remaining)
            }
            if (remaining <= 0) {
                loopJob?.cancel()
                _state.value = SnakeUiState.TimeUp
            }
        }
    }

    fun onSwipe(dx: Float, dy: Float) {
        if (abs(dx) > abs(dy)) {
            game.setDirection(if (dx > 0) Direction.RIGHT else Direction.LEFT)
        } else {
            game.setDirection(if (dy > 0) Direction.DOWN else Direction.UP)
        }
    }

    fun restart() {
        val current = _state.value as? SnakeUiState.Playing ?: return
        game.reset()
        _state.value = snapshot(current.remainingSeconds)
    }

    private fun snapshot(remainingSeconds: Int) = SnakeUiState.Playing(
        snake = game.snake,
        food = game.food,
        score = game.score,
        isGameOver = game.isGameOver,
        width = game.width,
        height = game.height,
        remainingSeconds = remainingSeconds,
    )
}

class SnakeScreen(sealedActivity: SealedLightActivity) :
    LightScreen<Unit, SnakeScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<SnakeScreenViewModel>
        get() = SnakeScreenViewModel::class.java

    override fun createViewModel(): SnakeScreenViewModel =
        SnakeScreenViewModel(DailyPlaytimeStore(lightContext.dataStore))

    @Composable
    override fun Content() {
        val state by viewModel.state.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                val rightLabel = when (val s = state) {
                    is SnakeUiState.Playing -> "${s.score}  ${formatClock(s.remainingSeconds)}"
                    else -> ""
                }
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack() }),
                    center = LightTopBarCenter.Text("Snake"),
                    rightButton = LightBarButton.Text(text = rightLabel, onClick = null),
                )

                when (val s = state) {
                    is SnakeUiState.CheckingBudget -> LoadingMessage()
                    is SnakeUiState.TimeUp -> TimeUpMessage()
                    is SnakeUiState.Playing -> PlayingContent(s, viewModel)
                }
            }
        }
    }
}

@Composable
private fun LoadingMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LightText(text = "Loading...", variant = LightTextVariant.Copy, lighten = true)
    }
}

@Composable
private fun TimeUpMessage() {
    val minutes = GameBudgets.SNAKE_SECONDS / 60
    Box(
        modifier = Modifier.fillMaxSize().padding(2f.gridUnitsAsDp()),
        contentAlignment = Alignment.Center,
    ) {
        Column {
            LightText(
                text = "That's $minutes minutes of Snake for today!",
                variant = LightTextVariant.Heading,
                align = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            LightText(
                text = "Come back tomorrow for more.",
                variant = LightTextVariant.Detail,
                lighten = true,
                align = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.5f.gridUnitsAsDp()),
            )
        }
    }
}

@Composable
private fun PlayingContent(state: SnakeUiState.Playing, viewModel: SnakeScreenViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(1f.gridUnitsAsDp())
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (abs(dragAmount.x) > SWIPE_THRESHOLD_PX || abs(dragAmount.y) > SWIPE_THRESHOLD_PX) {
                            viewModel.onSwipe(dragAmount.x, dragAmount.y)
                        }
                    },
                )
            },
    ) {
        SnakeBoard(state = state)

        if (state.isGameOver) {
            GameOverOverlay(score = state.score, onRestart = { viewModel.restart() })
        }
    }
}

@Composable
private fun SnakeBoard(state: SnakeUiState.Playing) {
    val colors = LightThemeTokens.colors
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cellWidth = size.width / state.width
        val cellHeight = size.height / state.height

        state.snake.forEach { segment ->
            drawRect(
                color = colors.content,
                topLeft = Offset(segment.x * cellWidth, segment.y * cellHeight),
                size = Size(cellWidth * 0.9f, cellHeight * 0.9f),
            )
        }

        drawRect(
            color = colors.contentSecondary,
            topLeft = Offset(state.food.x * cellWidth, state.food.y * cellHeight),
            size = Size(cellWidth * 0.7f, cellHeight * 0.7f),
        )
    }
}

@Composable
private fun GameOverOverlay(score: Int, onRestart: () -> Unit) {
    val colors = LightThemeTokens.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = 0.92f))
            .lightClickable(onClick = onRestart),
        contentAlignment = Alignment.Center,
    ) {
        Column {
            LightText(
                text = "Game Over",
                variant = LightTextVariant.Heading,
                align = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            LightText(
                text = "Score: $score",
                variant = LightTextVariant.Copy,
                align = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.5f.gridUnitsAsDp()),
            )
            LightText(
                text = "Tap to play again",
                variant = LightTextVariant.Detail,
                lighten = true,
                align = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 1f.gridUnitsAsDp()),
            )
        }
    }
}

private fun formatClock(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
