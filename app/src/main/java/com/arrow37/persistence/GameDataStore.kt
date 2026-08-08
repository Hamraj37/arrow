package com.arrow37.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_prefs")

class GameDataStore(context: Context) {
    private val appContext = context.applicationContext
    
    companion object {
        private val LEVEL_KEY = intPreferencesKey("current_level")
        private val SOUND_KEY = booleanPreferencesKey("sound_enabled")
        private val VIBRATION_KEY = booleanPreferencesKey("vibration_enabled")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val REFRESH_RATE_KEY = booleanPreferencesKey("refresh_rate_native")
        private val LEVEL_STARS_KEY = stringPreferencesKey("level_stars")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    private val mapType = Types.newParameterizedType(Map::class.java, Integer::class.java, Integer::class.java)
    private val mapAdapter = moshi.adapter<Map<Int, Int>>(mapType)

    val currentLevel: Flow<Int> = appContext.dataStore.data
        .map { preferences ->
            preferences[LEVEL_KEY] ?: 1
        }

    val isSoundEnabled: Flow<Boolean> = appContext.dataStore.data
        .map { preferences ->
            preferences[SOUND_KEY] ?: true
        }

    val isVibrationEnabled: Flow<Boolean> = appContext.dataStore.data
        .map { preferences ->
            preferences[VIBRATION_KEY] ?: true
        }

    val isDarkMode: Flow<Boolean> = appContext.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    val useNativeRefreshRate: Flow<Boolean> = appContext.dataStore.data
        .map { preferences ->
            preferences[REFRESH_RATE_KEY] ?: true
        }

    val levelStars: Flow<Map<Int, Int>> = appContext.dataStore.data
        .map { preferences ->
            val json = preferences[LEVEL_STARS_KEY] ?: "{}"
            try {
                mapAdapter.fromJson(json) ?: emptyMap()
            } catch (e: Exception) {
                emptyMap()
            }
        }

    val language: Flow<String> = appContext.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: "English"
        }

    suspend fun saveLevel(level: Int) {
        appContext.dataStore.edit { preferences ->
            preferences[LEVEL_KEY] = level
        }
    }

    suspend fun saveSoundEnabled(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[SOUND_KEY] = enabled
        }
    }

    suspend fun saveVibrationEnabled(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[VIBRATION_KEY] = enabled
        }
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun saveNativeRefreshRate(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[REFRESH_RATE_KEY] = enabled
        }
    }

    suspend fun saveLanguage(language: String) {
        appContext.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    suspend fun saveLevelStars(level: Int, stars: Int) {
        appContext.dataStore.edit { preferences ->
            val currentJson = preferences[LEVEL_STARS_KEY] ?: "{}"
            val currentMap = try {
                mapAdapter.fromJson(currentJson)?.toMutableMap() ?: mutableMapOf()
            } catch (e: Exception) {
                mutableMapOf()
            }

            val currentStars = currentMap[level] ?: 0
            if (stars > currentStars) {
                currentMap[level] = stars
                preferences[LEVEL_STARS_KEY] = mapAdapter.toJson(currentMap)
            }
        }
    }
}
