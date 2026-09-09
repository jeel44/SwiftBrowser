package com.swiftbrowser.fast.secure.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent

object InterstitialAdManager {

    private const val TAG = "AD_DEBUG"
    private val rc = RemoteConfigManager

    // ── Separate ad instances for cards and nav ───────────────────
    private var cardInterstitial: InterstitialAd? = null
    private var isCardLoading = false

    private var navInterstitial: InterstitialAd? = null
    private var isNavLoading = false

    // ── Separate click counters ───────────────────────────────────
    private var cardClickCount = 0
    private var navClickCount = 0

    // ─────────────────────────────────────────────────────────────
    // PRELOAD — call from HomeScreen on entry
    // ─────────────────────────────────────────────────────────────

    fun preload(context: Context) {
        loadCardInterstitial(context)
        loadNavInterstitial(context)
    }

    // ─────────────────────────────────────────────────────────────
    // CARD INTERSTITIAL
    // ─────────────────────────────────────────────────────────────

    private fun loadCardInterstitial(context: Context) {
        if (!rc.getBoolean(RemoteConfigManager.KEY_SHOW_BROWSER_INTERSTITIAL)) {
            Log.d(TAG, "CARD_INTERSTITIAL: disabled by RC flag")
            return
        }
        val adId = rc.getAdId(RemoteConfigManager.KEY_BROWSER_INTERSTITIAL_AD_ID) ?: run {
            Log.w(TAG, "CARD_INTERSTITIAL: ID empty")
            return
        }
        if (cardInterstitial != null || isCardLoading) return
        isCardLoading = true
        Log.d(TAG, "CARD_INTERSTITIAL: loading adId=$adId")
        InterstitialAd.load(context, adId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    cardInterstitial = ad
                    isCardLoading = false
                    Log.d(TAG, "CARD_INTERSTITIAL: loaded")
                    ad.setOnPaidEventListener { adValue ->
                        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.AD_IMPRESSION) {
                            param(FirebaseAnalytics.Param.AD_PLATFORM, "Google Ad Manager")
                            param(FirebaseAnalytics.Param.AD_SOURCE, "Google Ad Manager")
                            param(FirebaseAnalytics.Param.AD_FORMAT, "interstitial")
                            param(FirebaseAnalytics.Param.AD_UNIT_NAME, adId)
                            param(FirebaseAnalytics.Param.VALUE, adValue.valueMicros / 1_000_000.0)
                            param(FirebaseAnalytics.Param.CURRENCY, adValue.currencyCode)
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isCardLoading = false
                    Log.e(TAG, "CARD_INTERSTITIAL: failed — ${error.message}")
                }
            })
    }

    /**
     * Call when user taps a Quick Access card.
     * Increments counter. Shows ad on odd clicks.
     * onProceed is always called — after ad dismiss OR immediately if no ad / even click.
     */
    fun onCardClick(activity: Activity, onProceed: () -> Unit) {
        cardClickCount++
        Log.d(TAG, "CARD_INTERSTITIAL: cardClickCount=$cardClickCount")

        if (cardClickCount % 2 == 1) {
            val ad = cardInterstitial
            if (ad == null) {
                Log.d(TAG, "CARD_INTERSTITIAL: odd click but no ad ready — proceeding directly")
                loadCardInterstitial(activity.applicationContext)
                onProceed()
                return
            }
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    cardInterstitial = null
                    Log.d(TAG, "CARD_INTERSTITIAL: dismissed — reloading")
                    loadCardInterstitial(activity.applicationContext)
                    onProceed()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    cardInterstitial = null
                    Log.e(TAG, "CARD_INTERSTITIAL: show failed — proceeding")
                    loadCardInterstitial(activity.applicationContext)
                    onProceed()
                }
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "CARD_INTERSTITIAL: showing")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "CARD_INTERSTITIAL: even click — direct navigation")
            onProceed()
        }
    }

    // ─────────────────────────────────────────────────────────────
    // NAV INTERSTITIAL
    // ─────────────────────────────────────────────────────────────

    private fun loadNavInterstitial(context: Context) {
        if (!rc.getBoolean(RemoteConfigManager.KEY_SHOW_BROWSER_INTERSTITIAL)) {
            Log.d(TAG, "NAV_INTERSTITIAL: disabled by RC flag")
            return
        }
        val adId = rc.getAdId(RemoteConfigManager.KEY_BROWSER_INTERSTITIAL_AD_ID) ?: run {
            Log.w(TAG, "NAV_INTERSTITIAL: ID empty")
            return
        }
        if (navInterstitial != null || isNavLoading) return
        isNavLoading = true
        Log.d(TAG, "NAV_INTERSTITIAL: loading adId=$adId")
        InterstitialAd.load(context, adId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    navInterstitial = ad
                    isNavLoading = false
                    Log.d(TAG, "NAV_INTERSTITIAL: loaded")
                    ad.setOnPaidEventListener { adValue ->
                        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.AD_IMPRESSION) {
                            param(FirebaseAnalytics.Param.AD_PLATFORM, "Google Ad Manager")
                            param(FirebaseAnalytics.Param.AD_SOURCE, "Google Ad Manager")
                            param(FirebaseAnalytics.Param.AD_FORMAT, "interstitial")
                            param(FirebaseAnalytics.Param.AD_UNIT_NAME, adId)
                            param(FirebaseAnalytics.Param.VALUE, adValue.valueMicros / 1_000_000.0)
                            param(FirebaseAnalytics.Param.CURRENCY, adValue.currencyCode)
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isNavLoading = false
                    Log.e(TAG, "NAV_INTERSTITIAL: failed — ${error.message}")
                }
            })
    }

    /**
     * Call when user taps a bottom nav tab.
     * Increments counter. Shows ad on odd clicks.
     * onProceed is always called — after ad dismiss OR immediately if no ad / even click.
     */
    fun onNavClick(activity: Activity, onProceed: () -> Unit) {
        navClickCount++
        Log.d(TAG, "NAV_INTERSTITIAL: navClickCount=$navClickCount")

        if (navClickCount % 2 == 1) {
            val ad = navInterstitial
            if (ad == null) {
                Log.d(TAG, "NAV_INTERSTITIAL: odd click but no ad ready — proceeding directly")
                loadNavInterstitial(activity.applicationContext)
                onProceed()
                return
            }
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    navInterstitial = null
                    Log.d(TAG, "NAV_INTERSTITIAL: dismissed — reloading")
                    loadNavInterstitial(activity.applicationContext)
                    onProceed()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    navInterstitial = null
                    Log.e(TAG, "NAV_INTERSTITIAL: show failed — proceeding")
                    loadNavInterstitial(activity.applicationContext)
                    onProceed()
                }
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "NAV_INTERSTITIAL: showing")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "NAV_INTERSTITIAL: even click — direct navigation")
            onProceed()
        }
    }
}
