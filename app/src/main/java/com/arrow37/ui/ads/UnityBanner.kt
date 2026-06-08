package com.arrow37.ui.ads

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

@Composable
fun UnityBanner(
    placementId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    AndroidView(
        factory = { ctx ->
            BannerView(activity, placementId, UnityBannerSize(320, 50)).apply {
                load()
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        update = { view ->
            // view.load() // Loading every update might be too much, usually once is enough
        }
    )
}
