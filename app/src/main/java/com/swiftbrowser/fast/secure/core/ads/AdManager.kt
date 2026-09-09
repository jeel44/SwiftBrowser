package com.swiftbrowser.fast.secure.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

object AdManager {

    private const val TAG = "AD_DEBUG"
    private val rc = RemoteConfigManager

    // ── Cached instances ──────────────────────────────────────────
    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false

    private var splashInterstitial: InterstitialAd? = null

    private var browserInterstitial: InterstitialAd? = null
    private var isBrowserInterstitialShowing = false
    private var lastBrowserInterstitialShownAt = 0L

    private var browserRewarded: RewardedInterstitialAd? = null
    private var downloadsRewarded: RewardedInterstitialAd? = null

    // ─────────────────────────────────────────────────────────────
    // INIT — called after RC is ready
    // ─────────────────────────────────────────────────────────────

    fun init(context: Context) {
        MobileAds.initialize(context) {
            Log.d(TAG, "MobileAds SDK initialized")
            preloadAll(context)
        }
    }

    fun preloadAll(context: Context) {
        if (!rc.isReady) {
            Log.w(TAG, "RC not ready — skipping preload")
            return
        }
        loadAppOpenAd(context)
        loadSplashInterstitial(context)
        loadBrowserInterstitial(context)
        loadBrowserRewarded(context)
        loadDownloadsRewarded(context)
        Log.d(TAG, "Preload triggered for all ad formats")
    }

    // ─────────────────────────────────────────────────────────────
    // APP OPEN AD
    // ─────────────────────────────────────────────────────────────

    private fun loadAppOpenAd(context: Context) {
        if (!rc.getBoolean(rc.KEY_SHOW_APP_OPEN_AD)) {
            Log.d(TAG, "APP_OPEN: disabled by RC flag")
            return
        }
        val adId = rc.getAdId(rc.KEY_APP_OPEN_AD_ID) ?: run {
            Log.w(TAG, "APP_OPEN: ID is empty in RC — skipping load")
            return
        }
        if (appOpenAd != null || isAppOpenLoading) {
            Log.d(TAG, "APP_OPEN: already loaded or loading")
            return
        }
        isAppOpenLoading = true
        Log.d(TAG, "APP_OPEN: loading — adId=$adId")
        AppOpenAd.load(context, adId, AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAppOpenLoading = false
                    Log.d(TAG, "APP_OPEN: loaded successfully")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isAppOpenLoading = false
                    Log.e(TAG, "APP_OPEN: failed to load — ${error.code} ${error.message}")
                }
            })
    }

    fun showAppOpenAd(activity: Activity, onFinished: () -> Unit) {
        val ad = appOpenAd
        if (ad == null) {
            Log.d(TAG, "APP_OPEN: no ad ready — proceeding without ad")
            onFinished()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                Log.d(TAG, "APP_OPEN: dismissed — reloading")
                loadAppOpenAd(activity.applicationContext)
                onFinished()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd = null
                Log.e(TAG, "APP_OPEN: show failed — ${error.message}")
                onFinished()
            }
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "APP_OPEN: showing")
            }
        }
        ad.show(activity)
    }

    // ─────────────────────────────────────────────────────────────
    // SPLASH INTERSTITIAL
    // ─────────────────────────────────────────────────────────────

    private fun loadSplashInterstitial(context: Context) {
        val adId = rc.getAdId(rc.KEY_SPLASH_INTERSTITIAL_AD_ID) ?: run {
            Log.d(TAG, "SPLASH_INTERSTITIAL: ID empty — skipping")
            return
        }
        Log.d(TAG, "SPLASH_INTERSTITIAL: loading — adId=$adId")
        InterstitialAd.load(context, adId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    splashInterstitial = ad
                    Log.d(TAG, "SPLASH_INTERSTITIAL: loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "SPLASH_INTERSTITIAL: failed — ${error.message}")
                }
            })
    }

    fun showSplashInterstitial(activity: Activity, onFinished: () -> Unit) {
        val ad = splashInterstitial
        if (ad == null) {
            onFinished()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                splashInterstitial = null
                Log.d(TAG, "SPLASH_INTERSTITIAL: dismissed")
                onFinished()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                splashInterstitial = null
                onFinished()
            }
        }
        ad.show(activity)
    }

    // ─────────────────────────────────────────────────────────────
    // BROWSER INTERSTITIAL (cooldown enforced)
    // ─────────────────────────────────────────────────────────────

    private fun loadBrowserInterstitial(context: Context) {
        if (!rc.getBoolean(rc.KEY_SHOW_BROWSER_INTERSTITIAL)) {
            Log.d(TAG, "BROWSER_INTERSTITIAL: disabled by RC flag")
            return
        }
        val adId = rc.getAdId(rc.KEY_BROWSER_INTERSTITIAL_AD_ID) ?: run {
            Log.w(TAG, "BROWSER_INTERSTITIAL: ID empty — skipping")
            return
        }
        if (browserInterstitial != null) return
        Log.d(TAG, "BROWSER_INTERSTITIAL: loading — adId=$adId")
        InterstitialAd.load(context, adId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    browserInterstitial = ad
                    Log.d(TAG, "BROWSER_INTERSTITIAL: loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "BROWSER_INTERSTITIAL: failed — ${error.message}")
                }
            })
    }

    fun showBrowserInterstitial(activity: Activity, onFinished: () -> Unit) {
        if (!rc.getBoolean(rc.KEY_SHOW_BROWSER_INTERSTITIAL)) { onFinished(); return }
        if (isBrowserInterstitialShowing) { onFinished(); return }
        val cooldown = rc.getLong(rc.KEY_BROWSER_INTERSTITIAL_COOLDOWN)
        val elapsed = System.currentTimeMillis() - lastBrowserInterstitialShownAt
        if (elapsed < cooldown) {
            Log.d(TAG, "BROWSER_INTERSTITIAL: cooldown — ${(cooldown - elapsed) / 1000}s left")
            onFinished(); return
        }
        val ad = browserInterstitial
        if (ad == null) {
            Log.d(TAG, "BROWSER_INTERSTITIAL: no ad ready — preloading")
            loadBrowserInterstitial(activity.applicationContext)
            onFinished(); return
        }
        isBrowserInterstitialShowing = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                browserInterstitial = null
                isBrowserInterstitialShowing = false
                lastBrowserInterstitialShownAt = System.currentTimeMillis()
                Log.d(TAG, "BROWSER_INTERSTITIAL: dismissed — reloading")
                loadBrowserInterstitial(activity.applicationContext)
                onFinished()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                browserInterstitial = null
                isBrowserInterstitialShowing = false
                Log.e(TAG, "BROWSER_INTERSTITIAL: show failed — ${error.message}")
                onFinished()
            }
        }
        ad.show(activity)
    }

    // ─────────────────────────────────────────────────────────────
    // BROWSER REWARDED INTERSTITIAL
    // ─────────────────────────────────────────────────────────────

    private fun loadBrowserRewarded(context: Context) {
        if (!rc.getBoolean(rc.KEY_SHOW_BROWSER_REWARDED)) return
        val adId = rc.getAdId(rc.KEY_BROWSER_REWARDED_AD_ID) ?: return
        Log.d(TAG, "BROWSER_REWARDED: loading — adId=$adId")
        RewardedInterstitialAd.load(context, adId, AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    browserRewarded = ad
                    Log.d(TAG, "BROWSER_REWARDED: loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "BROWSER_REWARDED: failed — ${error.message}")
                }
            })
    }

    fun showBrowserRewarded(
        activity: Activity,
        onRewarded: () -> Unit,
        onDismissed: () -> Unit
    ) {
        val ad = browserRewarded ?: run { onDismissed(); return }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                browserRewarded = null
                loadBrowserRewarded(activity.applicationContext)
                onDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                browserRewarded = null
                onDismissed()
            }
        }
        ad.show(activity) { onRewarded() }
    }

    // ─────────────────────────────────────────────────────────────
    // DOWNLOADS REWARDED INTERSTITIAL
    // ─────────────────────────────────────────────────────────────

    private fun loadDownloadsRewarded(context: Context) {
        if (!rc.getBoolean(rc.KEY_SHOW_DOWNLOADS_REWARDED)) return
        val adId = rc.getAdId(rc.KEY_DOWNLOADS_REWARDED_AD_ID) ?: return
        Log.d(TAG, "DOWNLOADS_REWARDED: loading — adId=$adId")
        RewardedInterstitialAd.load(context, adId, AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    downloadsRewarded = ad
                    Log.d(TAG, "DOWNLOADS_REWARDED: loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "DOWNLOADS_REWARDED: failed — ${error.message}")
                }
            })
    }

    fun showDownloadsRewarded(
        activity: Activity,
        onRewarded: () -> Unit,
        onDismissed: () -> Unit
    ) {
        val ad = downloadsRewarded ?: run { onDismissed(); return }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                downloadsRewarded = null
                loadDownloadsRewarded(activity.applicationContext)
                onDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                downloadsRewarded = null
                onDismissed()
            }
        }
        ad.show(activity) { onRewarded() }
    }

    // ─────────────────────────────────────────────────────────────
    // BANNER ID GETTERS — null = don't show
    // ─────────────────────────────────────────────────────────────

    fun getHomeBannerId(): String? {
        if (!rc.getBoolean(rc.KEY_SHOW_HOME_BANNER)) return null
        return rc.getAdId(rc.KEY_HOME_BANNER_AD_ID)
    }

    fun getHomeNativeId(): String? {
        if (!rc.getBoolean(rc.KEY_SHOW_HOME_NATIVE)) return null
        return rc.getAdId(rc.KEY_HOME_NATIVE_AD_ID)
    }

    fun getBrowserBannerId(): String? {
        if (!rc.getBoolean(rc.KEY_SHOW_BROWSER_BANNER)) return null
        return rc.getAdId(rc.KEY_BROWSER_BANNER_AD_ID)
    }

    fun getDownloadsBannerId(): String? {
        if (!rc.getBoolean(rc.KEY_SHOW_DOWNLOADS_BANNER)) return null
        return rc.getAdId(rc.KEY_DOWNLOADS_BANNER_AD_ID)
    }

    fun getSettingsBannerId(): String? {
        if (!rc.getBoolean(rc.KEY_SHOW_SETTINGS_BANNER)) return null
        return rc.getAdId(rc.KEY_SETTINGS_BANNER_AD_ID)
    }

    fun getBookmarksBannerId(): String? {
        if (!rc.getBoolean(rc.KEY_SHOW_BOOKMARKS_BANNER)) return null
        return rc.getAdId(rc.KEY_BOOKMARKS_BANNER_AD_ID)
    }

    fun getHistoryBannerId(): String? {
        if (!rc.getBoolean(rc.KEY_SHOW_HISTORY_BANNER)) return null
        return rc.getAdId(rc.KEY_HISTORY_BANNER_AD_ID)
    }
}
