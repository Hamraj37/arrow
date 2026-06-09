package com.arrow37.network

import android.content.Context
import android.util.Log
import com.arrow37.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class GitHubRelease(
    @Json(name = "tag_name") val tagName: String,
    @Json(name = "html_url") val htmlUrl: String,
    @Json(name = "body") val body: String? = null
)

object UpdateManager {
    private const val REPO_OWNER = "Hamraj37"
    private const val REPO_NAME = "arrow"
    private const val LATEST_RELEASE_URL = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest"
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    private val releaseAdapter = moshi.adapter(GitHubRelease::class.java)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdates(context: Context): UpdateResult = withContext(Dispatchers.IO) {
        Log.d("UpdateManager", "Starting update check...")
        try {
            val currentVersionName = BuildConfig.VERSION_NAME
            Log.d("UpdateManager", "Current App Version (BuildConfig): $currentVersionName")

            val request = Request.Builder()
                .url(LATEST_RELEASE_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "arrow-app")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w("UpdateManager", "Update check failed: ${response.code} ${response.message}")
                    return@withContext UpdateResult.NoUpdate
                }
                
                val bodyString = response.body?.string() ?: run {
                    Log.w("UpdateManager", "Response body is null")
                    return@withContext UpdateResult.NoUpdate
                }
                
                Log.d("UpdateManager", "GitHub Response: $bodyString")
                val latestRelease = try {
                    releaseAdapter.fromJson(bodyString)
                } catch (e: Exception) {
                    Log.e("UpdateManager", "Failed to parse JSON", e)
                    null
                } ?: return@withContext UpdateResult.NoUpdate
                
                val latestVersion = latestRelease.tagName.replace(Regex("^[^0-9]+"), "").trim()
                val currentVersion = currentVersionName.replace(Regex("^[^0-9]+"), "").trim()
                
                Log.d("UpdateManager", "Comparing - Latest: '$latestVersion' vs Current: '$currentVersion'")

                if (isNewer(latestVersion, currentVersion)) {
                    Log.d("UpdateManager", "New version found!")
                    UpdateResult.NewUpdate(
                        versionName = latestRelease.tagName,
                        downloadUrl = latestRelease.htmlUrl,
                        releaseNotes = latestRelease.body ?: ""
                    )
                } else {
                    Log.d("UpdateManager", "App is up to date.")
                    UpdateResult.NoUpdate
                }
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Exception during update check", e)
            UpdateResult.NoUpdate
        }
    }

    private fun isNewer(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        
        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val l = latestParts.getOrNull(i) ?: 0
            val c = currentParts.getOrNull(i) ?: 0
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}

sealed class UpdateResult {
    object NoUpdate : UpdateResult()
    data class NewUpdate(
        val versionName: String,
        val downloadUrl: String,
        val releaseNotes: String
    ) : UpdateResult()
}
