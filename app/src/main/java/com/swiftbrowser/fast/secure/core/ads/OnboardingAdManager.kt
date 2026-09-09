package com.swiftbrowser.fast.secure.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

object OnboardingAdManager {

    private const val TAG = "AD_DEBUG"
    private val rc = RemoteConfigManager

    private var nativeAd: NativeAd? = null
    private var isNativeLoading = false

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    // ── Native Ad for Next button ─────────────────────────────────

    fun loadNativeAd(context: Context) {
        val adId = rc.getOnboardingNativeId() ?: run {
            Log.d(TAG, "ONBOARDING_NATIVE: disabled or ID empty")
            return
        }
        if (nativeAd != null || isNativeLoading) return
        isNativeLoading = true
        Log.d(TAG, "ONBOARDING_NATIVE: loading adId=$adId")
        val adLoader = AdLoader.Builder(context, adId)
            .forNativeAd { ad ->
                nativeAd = ad
                isNativeLoading = false
                Log.d(TAG, "ONBOARDING_NATIVE: loaded")
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isNativeLoading = false
                    Log.e(TAG, "ONBOARDING_NATIVE: failed — ${error.message}")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    // Returns the loaded native ad and clears it (caller owns it now)
    fun consumeNativeAd(): NativeAd? {
        val ad = nativeAd
        nativeAd = null
        return ad
    }

    fun preloadNextNativeAd(context: Context) {
        loadNativeAd(context)
    }

    // ── Interstitial for Get Started ──────────────────────────────

    fun loadInterstitialAd(context: Context) {
        val adId = rc.getOnboardingInterstitialId() ?: run {
            Log.d(TAG, "ONBOARDING_INTERSTITIAL: disabled or ID empty")
            return
        }
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true
        Log.d(TAG, "ONBOARDING_INTERSTITIAL: loading adId=$adId")
        InterstitialAd.load(context, adId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "ONBOARDING_INTERSTITIAL: loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isInterstitialLoading = false
                    Log.e(TAG, "ONBOARDING_INTERSTITIAL: failed — ${error.message}")
                }
            })
    }

    fun showInterstitialAd(activity: Activity, onFinished: () -> Unit) {
        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "ONBOARDING_INTERSTITIAL: not ready — skip")
            onFinished()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                Log.d(TAG, "ONBOARDING_INTERSTITIAL: dismissed")
                onFinished()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                Log.e(TAG, "ONBOARDING_INTERSTITIAL: show failed")
                onFinished()
            }
        }
        ad.show(activity)
    }
}
