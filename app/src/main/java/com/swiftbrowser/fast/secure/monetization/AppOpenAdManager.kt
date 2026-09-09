package com.swiftbrowser.fast.secure.monetization

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages App Open Ads. Should be connected to the app lifecycle via
 * [androidx.lifecycle.DefaultLifecycleObserver] so the ad shows on foreground transitions.
 *
 * TODO(Phase 2): Wire into [SwiftBrowserApp] via ProcessLifecycleOwner.
 */
@Singleton
class AppOpenAdManager @Inject constructor(
    private val adManager: AdManager,
) {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var loadTime: Long = 0

    /** Loads an App Open Ad. Safe to call multiple times — guards against double-loading. */
    fun loadAd(activity: Activity) {
        if (isLoadingAd || isAdAvailable()) return
        isLoadingAd = true

        AppOpenAd.load(
            activity,
            adManager.getAppOpenAdUnitId(),
            adManager.buildAdRequest(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = System.currentTimeMillis()
                    Timber.d("App open ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoadingAd = false
                    Timber.w("App open ad failed to load: ${error.message}")
                }
            },
        )
    }

    /** Shows the ad if one is available and the app is currently foregrounded. */
    fun showAdIfAvailable(activity: Activity, onComplete: () -> Unit = {}) {
        if (isShowingAd) { onComplete(); return }
        if (!isAdAvailable()) { onComplete(); loadAd(activity); return }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                loadAd(activity)
                onComplete()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd = null
                isShowingAd = false
                onComplete()
                Timber.w("App open ad failed to show: ${error.message}")
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
            }
        }

        appOpenAd?.show(activity)
    }

    /** App Open Ads expire after 4 hours. */
    private fun isAdAvailable(): Boolean {
        val fourHoursInMs = 4 * 60 * 60 * 1_000L
        return appOpenAd != null && (System.currentTimeMillis() - loadTime) < fourHoursInMs
    }
}
