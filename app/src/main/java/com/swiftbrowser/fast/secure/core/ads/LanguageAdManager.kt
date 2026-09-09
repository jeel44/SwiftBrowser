package com.swiftbrowser.fast.secure.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object LanguageAdManager {

    private const val TAG = "AD_DEBUG"
    private val rc = RemoteConfigManager

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    // Call this on Language screen entry — preload before user taps
    fun preload(context: Context) {
        val adId = rc.getLanguageInterstitialId() ?: run {
            Log.d(TAG, "LANGUAGE_INTERSTITIAL: disabled or ID empty")
            return
        }
        if (interstitialAd != null || isLoading) return
        isLoading = true
        Log.d(TAG, "LANGUAGE_INTERSTITIAL: preloading adId=$adId")
        InterstitialAd.load(context, adId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    Log.d(TAG, "LANGUAGE_INTERSTITIAL: preloaded and ready")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    Log.e(TAG, "LANGUAGE_INTERSTITIAL: preload failed — ${error.message}")
                }
            })
    }

    // Call when user taps a language — show ad then navigate to Home
    fun showOnLanguageSelect(activity: Activity, onFinished: () -> Unit) {
        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "LANGUAGE_INTERSTITIAL: not ready — navigate directly")
            onFinished()
            preload(activity.applicationContext)
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                Log.d(TAG, "LANGUAGE_INTERSTITIAL: dismissed — navigating to Home")
                preload(activity.applicationContext)
                onFinished()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                Log.e(TAG, "LANGUAGE_INTERSTITIAL: show failed — navigating directly")
                onFinished()
            }
        }
        ad.show(activity)
    }
}
