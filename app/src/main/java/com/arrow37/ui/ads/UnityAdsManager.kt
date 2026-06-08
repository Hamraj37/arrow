package com.arrow37.ui.ads

import android.app.Activity
import android.util.Log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions

object UnityAdsManager {
    private const val TAG = "UnityAdsManager"
    private const val GAME_ID = "4799059"
    private const val TEST_MODE = true
    private const val REWARDED_PLACEMENT_ID = "Rewarded_Android"

    fun initialize(activity: Activity) {
        UnityAds.initialize(activity.applicationContext, GAME_ID, TEST_MODE, object : IUnityAdsInitializationListener {
            override fun onInitializationComplete() {
                Log.d(TAG, "Unity Ads Initialization Complete")
                loadRewardedAd()
            }

            override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                Log.e(TAG, "Unity Ads Initialization Failed: $message")
            }
        })
    }

    fun loadRewardedAd() {
        UnityAds.load(REWARDED_PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                Log.d(TAG, "Rewarded Ad Loaded: $placementId")
            }

            override fun onUnityAdsFailedToLoad(placementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {
                Log.e(TAG, "Rewarded Ad Failed to Load: $message")
            }
        })
    }

    fun showRewardedAd(activity: Activity, onComplete: () -> Unit) {
        UnityAds.show(activity, REWARDED_PLACEMENT_ID, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) {
                Log.e(TAG, "Rewarded Ad Show Failure: $message")
                onComplete() // Proceed anyway
                loadRewardedAd() // Try to reload
            }

            override fun onUnityAdsShowStart(placementId: String?) {
                Log.d(TAG, "Rewarded Ad Show Start")
            }

            override fun onUnityAdsShowClick(placementId: String?) {
                Log.d(TAG, "Rewarded Ad Show Click")
            }

            override fun onUnityAdsShowComplete(placementId: String?, state: UnityAds.UnityAdsShowCompletionState?) {
                Log.d(TAG, "Rewarded Ad Show Complete: $state")
                onComplete()
                loadRewardedAd() // Load next one
            }
        })
    }
}
