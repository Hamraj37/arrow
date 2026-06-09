package com.arrow37.ui.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun UnityBanner(
    placementId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: return

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
