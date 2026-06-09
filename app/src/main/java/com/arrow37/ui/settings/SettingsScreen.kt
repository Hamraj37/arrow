package com.arrow37.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arrow37.BuildConfig
import com.arrow37.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GameViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val accentColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", color = onBackgroundColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = onBackgroundColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Container 1
                SettingsGroup {
                    SettingRow(
                        icon = Icons.Rounded.Language,
                        title = "Language",
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("English", color = Color.Gray, fontSize = 14.sp)
                                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    )
                    SettingDivider()
                    SettingRow(
                        icon = Icons.Rounded.Waves,
                        title = "Vibrations",
                        trailing = {
                            Switch(
                                checked = state.isVibrationEnabled,
                                onCheckedChange = { viewModel.toggleVibration() },
                                colors = SwitchDefaults.colors(checkedTrackColor = accentColor)
                            )
                        }
                    )
                    SettingDivider()
                    SettingRow(
                        icon = Icons.Rounded.VolumeUp,
                        title = "Sounds",
                        trailing = {
                            Switch(
                                checked = state.isSoundEnabled,
                                onCheckedChange = { viewModel.toggleSound() },
                                colors = SwitchDefaults.colors(checkedTrackColor = accentColor)
                            )
                        }
                    )
                    SettingDivider()
                    SettingRow(
                        icon = Icons.Rounded.DarkMode,
                        title = "Dark mode",
                        trailing = {
                            Switch(
                                checked = state.isDarkMode,
                                onCheckedChange = { viewModel.toggleDarkMode() },
                                colors = SwitchDefaults.colors(checkedTrackColor = accentColor)
                            )
                        }
                    )
                    SettingDivider()
                    SettingRow(
                        icon = Icons.Rounded.Smartphone,
                        title = "Native Refresh Rate",
                        trailing = {
                            Switch(
                                checked = state.useNativeRefreshRate,
                                onCheckedChange = { viewModel.toggleNativeRefreshRate() },
                                colors = SwitchDefaults.colors(checkedTrackColor = accentColor)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Container 5
                SettingsGroup {
                    SettingRow(
                        icon = Icons.Rounded.Description,
                        title = "Privacy"
                    )
                    SettingDivider()
                    SettingRow(
                        icon = Icons.Rounded.Info,
                        title = "Terms of Service"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = "Developed by Hamraj37",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(vertical = 4.dp),
        content = content
    )
}

@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

@Composable
fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = Color.Gray.copy(alpha = 0.2f)
    )
}
