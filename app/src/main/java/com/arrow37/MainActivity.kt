package com.arrow37

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.arrow37.ui.menu.MenuScreen
import com.arrow37.ui.navigation.GameKey
import com.arrow37.ui.navigation.MenuKey
import com.arrow37.ui.navigation.SettingsKey
import com.arrow37.ui.puzzle.PuzzleScreen
import com.arrow37.ui.settings.SettingsScreen
import com.arrow37.ui.theme.ArrowTheme
import com.arrow37.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val gameViewModel: GameViewModel = viewModel()
            val state by gameViewModel.uiState.collectAsState()

            LaunchedEffect(state.useNativeRefreshRate) {
                val window = this@MainActivity.window
                if (state.useNativeRefreshRate) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val display = this@MainActivity.display
                        val maxRate = display?.supportedModes?.maxByOrNull { it.refreshRate }?.refreshRate ?: 0f
                        if (maxRate > 0f) {
                            val mode = display?.supportedModes?.find { it.refreshRate == maxRate }
                            window.attributes = window.attributes.apply {
                                preferredDisplayModeId = mode?.modeId ?: 0
                            }
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        window.attributes = window.attributes.apply {
                            preferredRefreshRate = 0f
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    window.attributes = window.attributes.apply {
                        preferredRefreshRate = 60f
                    }
                }
            }

            ArrowTheme(darkTheme = state.isDarkMode) {
                val backStack = rememberNavBackStack(MenuKey)
                
                val provider = entryProvider {
                    entry<MenuKey> {
                        MenuScreen(
                            viewModel = gameViewModel,
                            onPlayClick = { backStack.add(GameKey) },
                            onSettingsClick = { backStack.add(SettingsKey) }
                        )
                    }
                    entry<GameKey> {
                        PuzzleScreen(
                            viewModel = gameViewModel,
                            onBack = { backStack.removeLastOrNull() },
                            onSettings = { backStack.add(SettingsKey) }
                        )
                    }
                    entry<SettingsKey> {
                        SettingsScreen(
                            viewModel = gameViewModel,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                }

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = provider
                )
            }
        }
    }
}
