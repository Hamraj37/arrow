package com.arrow37.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object MenuKey : NavKey

@Serializable
data object GameKey : NavKey

@Serializable
data object SettingsKey : NavKey
