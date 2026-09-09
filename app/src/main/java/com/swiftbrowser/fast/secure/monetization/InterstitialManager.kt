package com.swiftbrowser.fast.secure.monetization

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.swiftbrowser.fast.secure.core.utils.Constants
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Interstitial Ads. Enforces a minimum interval between shows to avoid
 * annoying the user (see [Constants.INTERSTITIAL_MIN_INTERVAL_MS]).
 *
 * TODO(Phase 2): Call [tryShowAd] on tab navigation events in BrowserViewModel.
 */
@Singleton
class InterstitialManager @Inject constructor(
    private val adManager: AdManager,
) {

    private var interstitialAd: InterstitialAd? = null
    private var isLoadingAd = false
    private var lastShowTime = 0L

    /** Pre-loads an interstitial. Call early so the ad is ready when needed. */
    fun loadAd(activity: Activity) {
        if (isLoadingAd || interstitialAd != null) return
        isLoadingAd = true

        InterstitialAd.load(
            activity,
            adManager.getInterstitialAdUnitId(),
            adManager.buildAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoadingAd = false
                    Timber.d("Interstitial ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoadingAd = false
                    Timber.w("Interstitial ad failed to load: ${error.message}")
                }
            },
        )
    }

    /**
     * Shows the interstitial if one is loaded AND enough time has passed since the last show.
     * Reloads a new ad after showing.
     */
    fun tryShowAd(activity: Activity, onComplete: () -> Unit = {}) {
        val now = System.currentTimeMillis()
        if (now - lastShowTime < Constants.INTERSTITIAL_MIN_INTERVAL_MS) {
            onComplete()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            loadAd(activity)
            onComplete()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadAd(activity)
                onComplete()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                loadAd(activity)
                onComplete()
                Timber.w("Interstitial failed to show: ${error.message}")
            }

            override fun onAdShowedFullScreenContent() {
                lastShowTime = System.currentTimeMillis()
            }
        }

        ad.show(activity)
    }
}
