package com.thelightphone.games

import androidx.compose.foundation.background
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
import androidx.lifecycle.viewModelScope
import com.thelightphone.games.snake.SnakeScreen
import com.thelightphone.games.sudoku.SudokuScreen
import com.thelightphone.games.wordsearch.WordSearchScreen
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightIcon
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val dailyLimitStore: DailyLimitStore,
    private val dailyPlaytimeStore: DailyPlaytimeStore,
    private val settingsStore: SettingsStore,
) : LightViewModel<Unit>() {

    data class UiState(
        val snakeRemainingSeconds: Int = DailyPlaytimeStore.DEFAULT_DAILY_SECONDS,
        val sudokuRemaining: Int = DailyLimitStore.DEFAULT_DAILY_LIMIT,
        val wordSearchRemaining: Int = DailyLimitStore.DEFAULT_DAILY_LIMIT,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private var hasAppliedSavedTheme = false

    // Refresh every time this screen becomes visible - including when a game
    // is backed out of, since LightActivity re-shows the previous screen.
    override fun onScreenShow(screen: SimpleLightScreen<Unit>) {
        super.onScreenShow(screen)
        if (!hasAppliedSavedTheme) {
            hasAppliedSavedTheme = true
            applySavedTheme()
        }
        refresh()
    }

    private fun applySavedTheme() {
        viewModelScope.launch {
            if (settingsStore.isColorInverted()) {
                LightThemeController.setLightTheme()
            } else {
                LightThemeController.setDarkTheme()
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.value = UiState(
                snakeRemainingSeconds = dailyPlaytimeStore.remainingSeconds(GameKeys.SNAKE),
                sudokuRemaining = dailyLimitStore.remainingPlays(GameKeys.SUDOKU),
                wordSearchRemaining = dailyLimitStore.remainingPlays(GameKeys.WORD_SEARCH),
            )
        }
    }
}

@InitialScreen
class HomeScreen(sealedActivity: SealedLightActivity) :
    LightScreen<Unit, HomeScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<HomeScreenViewModel>
        get() = HomeScreenViewModel::class.java

    override fun createViewModel(): HomeScreenViewModel =
        HomeScreenViewModel(
            dailyLimitStore = DailyLimitStore(lightContext.dataStore),
            dailyPlaytimeStore = DailyPlaytimeStore(lightContext.dataStore),
            settingsStore = SettingsStore(lightContext.dataStore),
        )

    @Composable
    override fun Content() {
        val state by viewModel.state.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    LightTopBar(center = LightTopBarCenter.Text("Games"))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2f.gridUnitsAsDp()),
                    ) {
                        GameMenuRow(
                            title = "Snake",
                            subtitle = describeRemainingTime(state.snakeRemainingSeconds),
                            enabled = state.snakeRemainingSeconds > 0,
                            onClick = { navigateTo(screenFactory = { activity -> SnakeScreen(activity) }) },
                        )
                        GameMenuRow(
                            title = "Sudoku",
                            subtitle = describeRemaining(state.sudokuRemaining),
                            enabled = state.sudokuRemaining > 0,
                            onClick = { navigateTo(screenFactory = { activity -> SudokuScreen(activity) }) },
                        )
                        GameMenuRow(
                            title = "Word Search",
                            subtitle = describeRemaining(state.wordSearchRemaining),
                            enabled = state.wordSearchRemaining > 0,
                            onClick = { navigateTo(screenFactory = { activity -> WordSearchScreen(activity) }) },
                        )
                    }
                }

                LightIcon(
                    icon = LightIcons.SETTINGS,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2f.gridUnitsAsDp())
                        .lightClickable {
                            navigateTo(screenFactory = { activity -> SettingsScreen(activity) })
                        },
                )
            }
        }
    }
}

private fun describeRemaining(remaining: Int): String = when (remaining) {
    0 -> "No puzzles left today - come back tomorrow"
    1 -> "1 puzzle left today"
    else -> "$remaining puzzles left today"
}

private fun describeRemainingTime(remainingSeconds: Int): String {
    if (remainingSeconds <= 0) return "No time left today - come back tomorrow"
    val minutes = remainingSeconds / 60
    return when {
        minutes >= 1 -> "$minutes min left today"
        else -> "Under a minute left today"
    }
}

@Composable
private fun GameMenuRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .lightClickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 1.25f.gridUnitsAsDp()),
    ) {
        LightText(text = title, variant = LightTextVariant.Heading, lighten = !enabled)
        LightText(
            text = subtitle,
            variant = LightTextVariant.Detail,
            lighten = true,
            modifier = Modifier.padding(top = 0.25f.gridUnitsAsDp()),
        )
    }
}
