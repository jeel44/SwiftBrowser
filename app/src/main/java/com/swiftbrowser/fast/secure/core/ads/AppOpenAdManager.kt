package com.swiftbrowser.fast.secure.core.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent

object AppOpenAdManager : Application.ActivityLifecycleCallbacks {

    private const val TAG = "AD_DEBUG"
    private val rc = RemoteConfigManager

    // Splash app open — shown once on cold start from SplashScreen
    private var splashAppOpenAd: AppOpenAd? = null
    private var isSplashLoading = false

    // Splash fallback interstitial — shown if splash app open fails to load or show
    private var splashFallbackInterstitial: InterstitialAd? = null
    private var isFallbackLoading = false

    // Background app open — shown when app resumes from background
    private var backgroundAppOpenAd: AppOpenAd? = null
    private var isBackgroundLoading = false

    // State tracking
    private var isShowingAd = false
    private var currentActivity: Activity? = null
    private var appInBackground = false
    private var shouldShowBackgroundAd = false

    // ─────────────────────────────────────────────────────────────
    // INIT — call from Application.onCreate()
    // ─────────────────────────────────────────────────────────────

    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                // App came to foreground — activity not yet resumed, defer ad to onActivityResumed
                if (appInBackground) {
                    appInBackground = false
                    shouldShowBackgroundAd = true
                    Log.d(TAG, "BACKGROUND_APP_OPEN: app came to foreground, shouldShow=$shouldShowBackgroundAd")
                }
            }
            override fun onStop(owner: LifecycleOwner) {
                appInBackground = true
                Log.d(TAG, "BACKGROUND_APP_OPEN: app went to background")
            }
        })
    }

    fun preloadAll(context: Context) {
        loadSplashAppOpenAd(context)
        loadSplashFallbackInterstitial(context)
        loadBackgroundAppOpenAd(context)
    }

    // ─────────────────────────────────────────────────────────────
    // SPLASH APP OPEN — cold start only, called from SplashScreen
    // ─────────────────────────────────────────────────────────────

    fun loadSplashAppOpenAd(context: Context) {
        val adId = rc.getSplashAppOpenId() ?: run {
            Log.d(TAG, "SPLASH_APP_OPEN: disabled or ID empty")
            return
        }
        if (splashAppOpenAd != null || isSplashLoading) return
        isSplashLoading = true
        Log.d(TAG, "SPLASH_APP_OPEN: loading adId=$adId")
        AppOpenAd.load(context, adId, AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    splashAppOpenAd = ad
                    isSplashLoading = false
                    Log.d(TAG, "SPLASH_APP_OPEN: loaded")
                    ad.setOnPaidEventListener { adValue ->
                        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.AD_IMPRESSION) {
                            param(FirebaseAnalytics.Param.AD_PLATFORM, "Google Ad Manager")
                            param(FirebaseAnalytics.Param.AD_SOURCE, "Google Ad Manager")
                            param(FirebaseAnalytics.Param.AD_FORMAT, "app_open")
                            param(FirebaseAnalytics.Param.AD_UNIT_NAME, adId)
                            param(FirebaseAnalytics.Param.VALUE, adValue.valueMicros / 1_000_000.0)
                            param(FirebaseAnalytics.Param.CURRENCY, adValue.currencyCode)
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isSplashLoading = false
                    Log.e(TAG, "SPLASH_APP_OPEN: failed — ${error.message}")
                }
            })
    }

    fun showSplashAppOpenAd(activity: Activity, onFinished: () -> Unit) {
        val ad = splashAppOpenAd
        if (ad == null) {
            Log.d(TAG, "SPLASH_APP_OPEN: not ready — trying fallback")
            showSplashFallback(activity, onFinished)
            return
        }
        if (isShowingAd) { onFinished(); return }
        isShowingAd = true
        ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                splashAppOpenAd = null
                isShowingAd = false
                Log.d(TAG, "SPLASH_APP_OPEN: dismissed")
                loadBackgroundAppOpenAd(activity.applicationContext)
                onFinished()
            }
            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                splashAppOpenAd = null
                isShowingAd = false
                Log.e(TAG, "SPLASH_APP_OPEN: show failed — ${error.message} — trying fallback")
                showSplashFallback(activity, onFinished)
            }
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "SPLASH_APP_OPEN: showing")
            }
        }
        ad.show(activity)
    }

    private fun loadSplashFallbackInterstitial(context: Context) {
        val adId = rc.getAdId(RemoteConfigManager.KEY_SPLASH_INTERSTITIAL_AD_ID) ?: run {
            Log.d(TAG, "SPLASH_FALLBACK: disabled or ID empty")
            return
        }
        if (splashFallbackInterstitial != null || isFallbackLoading) return
        isFallbackLoading = true
        Log.d(TAG, "SPLASH_FALLBACK: loading adId=$adId")
        InterstitialAd.load(context, adId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    splashFallbackInterstitial = ad
                    isFallbackLoading = false
                    Log.d(TAG, "SPLASH_FALLBACK: loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isFallbackLoading = false
                    Log.e(TAG, "SPLASH_FALLBACK: failed — ${error.message}")
                }
            })
    }

    private fun showSplashFallback(activity: Activity, onFinished: () -> Unit) {
        val ad = splashFallbackInterstitial
        if (ad == null) {
            Log.d(TAG, "SPLASH_FALLBACK: not ready — skip")
            onFinished()
            return
        }
        splashFallbackInterstitial = null
        ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "SPLASH_FALLBACK: dismissed")
                onFinished()
            }
            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                Log.e(TAG, "SPLASH_FALLBACK: show failed — ${error.message}")
                onFinished()
            }
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "SPLASH_FALLBACK: showing")
            }
        }
        ad.show(activity)
    }

    // ─────────────────────────────────────────────────────────────
    // BACKGROUND APP OPEN — when app resumes from background
    // ─────────────────────────────────────────────────────────────

    private fun loadBackgroundAppOpenAd(context: Context) {
        val adId = rc.getBackgroundAppOpenId() ?: run {
            Log.d(TAG, "BACKGROUND_APP_OPEN: disabled or ID empty")
            return
        }
        if (backgroundAppOpenAd != null || isBackgroundLoading) return
        isBackgroundLoading = true
        Log.d(TAG, "BACKGROUND_APP_OPEN: loading adId=$adId")
        AppOpenAd.load(context, adId, AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    backgroundAppOpenAd = ad
                    isBackgroundLoading = false
                    Log.d(TAG, "BACKGROUND_APP_OPEN: loaded")
                    ad.setOnPaidEventListener { adValue ->
                        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.AD_IMPRESSION) {
                            param(FirebaseAnalytics.Param.AD_PLATFORM, "Google Ad Manager")
                            param(FirebaseAnalytics.Param.AD_SOURCE, "Google Ad Manager")
                            param(FirebaseAnalytics.Param.AD_FORMAT, "app_open")
                            param(FirebaseAnalytics.Param.AD_UNIT_NAME, adId)
                            param(FirebaseAnalytics.Param.VALUE, adValue.valueMicros / 1_000_000.0)
                            param(FirebaseAnalytics.Param.CURRENCY, adValue.currencyCode)
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isBackgroundLoading = false
                    Log.e(TAG, "BACKGROUND_APP_OPEN: failed — ${error.message}")
                }
            })
    }

    private fun showBackgroundAppOpenAd(activity: Activity) {
        val ad = backgroundAppOpenAd
        if (ad == null) {
            Log.d(TAG, "BACKGROUND_APP_OPEN: not ready — skip")
            loadBackgroundAppOpenAd(activity.applicationContext)
            return
        }
        if (isShowingAd) return
        isShowingAd = true
        ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                backgroundAppOpenAd = null
                isShowingAd = false
                Log.d(TAG, "BACKGROUND_APP_OPEN: dismissed — reloading")
                loadBackgroundAppOpenAd(activity.applicationContext)
            }
            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                backgroundAppOpenAd = null
                isShowingAd = false
                Log.e(TAG, "BACKGROUND_APP_OPEN: show failed")
                loadBackgroundAppOpenAd(activity.applicationContext)
            }
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "BACKGROUND_APP_OPEN: showing")
            }
        }
        ad.show(activity)
    }

    // ─────────────────────────────────────────────────────────────
    // Activity lifecycle — track current activity for background resume
    // ─────────────────────────────────────────────────────────────

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        Log.d(TAG, "BACKGROUND_APP_OPEN: activity resumed, shouldShow=$shouldShowBackgroundAd, ad=${backgroundAppOpenAd != null}")
        if (shouldShowBackgroundAd) {
            shouldShowBackgroundAd = false
            showBackgroundAppOpenAd(activity)
        }
    }
    override fun onActivityPaused(activity: Activity) { if (currentActivity == activity) currentActivity = null }
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) { if (currentActivity == activity) currentActivity = null }
}
