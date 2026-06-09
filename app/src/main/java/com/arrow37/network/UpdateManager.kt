package com.arrow37.network

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

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
    private val client = OkHttpClient()

    suspend fun checkForUpdates(context: Context): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val currentVersionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0)).versionName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } ?: "1.0"

            val request = Request.Builder()
                .url(LATEST_RELEASE_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext UpdateResult.NoUpdate
                
                val bodyString = response.body?.string() ?: return@withContext UpdateResult.NoUpdate
                val latestRelease = releaseAdapter.fromJson(bodyString) ?: return@withContext UpdateResult.NoUpdate
                
                val latestVersion = latestRelease.tagName.removePrefix("v").trim()
                val currentVersion = currentVersionName.removePrefix("v").trim()

                if (isNewer(latestVersion, currentVersion)) {
                    UpdateResult.NewUpdate(
                        versionName = latestRelease.tagName,
                        downloadUrl = latestRelease.htmlUrl,
                        releaseNotes = latestRelease.body ?: ""
                    )
                } else {
                    UpdateResult.NoUpdate
                }
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Failed to check for updates", e)
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
