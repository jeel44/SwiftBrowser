package com.swiftbrowser.fast.secure.core.ads

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent

@Composable
fun BannerAdView(adId: String?, modifier: Modifier = Modifier) {
    if (adId.isNullOrBlank()) return
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(
                    AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, 320)
                )
                adUnitId = adId
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                loadAd(AdRequest.Builder().build())
                setOnPaidEventListener { adValue ->
                    FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.AD_IMPRESSION) {
                        param(FirebaseAnalytics.Param.AD_PLATFORM, "Google Ad Manager")
                        param(FirebaseAnalytics.Param.AD_SOURCE, "Google Ad Manager")
                        param(FirebaseAnalytics.Param.AD_FORMAT, "banner")
                        param(FirebaseAnalytics.Param.AD_UNIT_NAME, adUnitId)
                        param(FirebaseAnalytics.Param.VALUE, adValue.valueMicros / 1_000_000.0)
                        param(FirebaseAnalytics.Param.CURRENCY, adValue.currencyCode)
                    }
                }
            }
        }
    )
}
