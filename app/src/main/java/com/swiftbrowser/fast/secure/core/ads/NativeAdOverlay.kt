package com.swiftbrowser.fast.secure.core.ads

import android.view.LayoutInflater
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.delay

@Composable
fun NativeAdOverlay(
    nativeAd: NativeAd?,
    onClose: () -> Unit
) {
    if (nativeAd == null) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    var showCloseButton by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(2000L)
        showCloseButton = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF0000000))
            .zIndex(10f)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(16.dp)),
            factory = { ctx ->
                val nativeAdView = NativeAdView(ctx)
                val adView = LayoutInflater.from(ctx)
                    .inflate(
                        ctx.resources.getIdentifier(
                            "native_ad_onboarding", "layout", ctx.packageName
                        ),
                        nativeAdView,
                        false
                    )
                nativeAdView.addView(adView)

                nativeAdView.headlineView = adView.findViewById(
                    ctx.resources.getIdentifier("ad_headline", "id", ctx.packageName)
                )
                nativeAdView.bodyView = adView.findViewById(
                    ctx.resources.getIdentifier("ad_body", "id", ctx.packageName)
                )
                nativeAdView.iconView = adView.findViewById(
                    ctx.resources.getIdentifier("ad_icon", "id", ctx.packageName)
                )
                nativeAdView.callToActionView = adView.findViewById(
                    ctx.resources.getIdentifier("ad_call_to_action", "id", ctx.packageName)
                )
                nativeAdView.mediaView = adView.findViewById(
                    ctx.resources.getIdentifier("ad_media", "id", ctx.packageName)
                )

                (nativeAdView.headlineView as? android.widget.TextView)?.text = nativeAd.headline
                (nativeAdView.bodyView as? android.widget.TextView)?.text = nativeAd.body
                (nativeAdView.callToActionView as? android.widget.Button)?.text = nativeAd.callToAction
                nativeAd.icon?.let { icon ->
                    (nativeAdView.iconView as? android.widget.ImageView)?.setImageDrawable(icon.drawable)
                }
                nativeAd.mediaContent?.let { media ->
                    nativeAdView.mediaView?.mediaContent = media
                }

                nativeAdView.setNativeAd(nativeAd)
                nativeAdView
            }
        )

        Box(
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.TopStart)
                .background(Color(0xFF6C47D9), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text("Ad", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        AnimatedVisibility(
            visible = showCloseButton,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            TextButton(
                onClick = {
                    nativeAd.destroy()
                    onClose()
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF333333), CircleShape)
            ) {
                Text("✕", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}
