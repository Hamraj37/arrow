package com.arrow37.ui.menu

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arrow37.data.GameState
import com.arrow37.ui.theme.ArrowTheme
import com.arrow37.ui.theme.LocalAppStrings
import com.arrow37.viewmodel.GameViewModel

@Suppress("unused")
@Composable
fun MenuScreen(
    onPlayClick: () -> Unit,
    onLevelsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: GameViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    MenuScreenContent(
        state = state,
        onPlayClick = onPlayClick,
        onLevelsClick = onLevelsClick,
        onSettingsClick = onSettingsClick,
        onCheckForUpdates = { viewModel.checkForUpdates() },
        onDismissUpdate = { viewModel.dismissUpdate() },
    )
}

@Composable
fun MenuScreenContent(
    state: GameState,
    onPlayClick: () -> Unit,
    onLevelsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onDismissUpdate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current

    LaunchedEffect(Unit) {
        android.util.Log.d("MenuScreen", "Checking for updates...")
        onCheckForUpdates()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "ARROW",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4FC3F7),
                    letterSpacing = 8.sp,
                ),
            )
            Text(
                text = "ESCAPE",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 4.sp,
                ),
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4FC3F7),
                ),
            ) {
                Text(strings.startGame, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onLevelsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4FC3F7)),
            ) {
                Text(
                    strings.levels,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4FC3F7),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4FC3F7)),
            ) {
                Icon(
                    Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = Color(0xFF4FC3F7),
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(
                    strings.settings,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4FC3F7),
                )
            }
        }
    }

    state.updateInfo?.let { info ->
        Dialog(onDismissRequest = { onDismissUpdate() }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Stylized Logo mimicking "Walla"
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ARROW",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4A69A8),
                                letterSpacing = 1.sp,
                            ),
                        )
                        // Tiny dots like in the image
                        Column(modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)) {
                            Row {
                                Box(modifier = Modifier.size(4.dp).background(Color(0xFF4FC3F7), CircleShape))
                                Spacer(modifier = Modifier.width(2.dp))
                                Box(modifier = Modifier.size(4.dp).background(Color(0xFFE57373), CircleShape))
                            }
                            Box(modifier = Modifier.padding(start = 3.dp).size(4.dp).background(Color(0xFFFFB74D), CircleShape))
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Version : ${info.versionName}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1F1F),
                        ),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // The Gray Release Notes Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .background(Color(0xFFF0F2F5), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text(
                                text = strings.recentChanges,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4F4F4F),
                                ),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = info.releaseNotes.ifEmpty { "• Bug fixes and performance improvements\n• Stability updates" },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF4F4F4F),
                                    lineHeight = 20.sp,
                                ),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Primary Action Button (Blue)
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, info.downloadUrl.toUri())
                            context.startActivity(intent)
                            onDismissUpdate()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A69A8),
                        ),
                    ) {
                        Text(
                            text = strings.updateNow,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Action Button (Text)
                    TextButton(
                        onClick = { onDismissUpdate() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = strings.updateLater,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4A69A8),
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,navigation=buttons", apiLevel = 36)
@Composable
fun MenuScreenPreview() {
    ArrowTheme {
        MenuScreenContent(
            state = GameState(),
            onPlayClick = {},
            onLevelsClick = {},
            onSettingsClick = {},
            onCheckForUpdates = {},
            onDismissUpdate = {},
        )
    }
}
