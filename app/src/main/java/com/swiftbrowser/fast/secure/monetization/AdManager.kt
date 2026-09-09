package com.swiftbrowser.fast.secure.monetization

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.swiftbrowser.fast.secure.core.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central ad manager. Controls AdMob initialization, ad unit ID resolution, and shared
 * [AdRequest] creation. All ad types (banner, interstitial, app-open) delegate through here
 * so ad logic is never scattered across screens.
 */
@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    var isInitialized = false
        private set

    /**
     * Initializes the AdMob SDK. Must be called once from [SwiftBrowserApp.onCreate].
     * Deferred until after the app is running to not block cold-start.
     */
    fun initialize() {
        if (isInitialized) return

        // Register test device IDs so test ads are served on physical devices during development
        val testDeviceIds = listOf(AdRequest.DEVICE_ID_EMULATOR)
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(configuration)

        MobileAds.initialize(context) { initStatus ->
            isInitialized = true
            Timber.d("AdMob initialized: ${initStatus.adapterStatusMap}")
        }
    }

    fun getBannerAdUnitId(): String = Constants.AdUnitIds.BANNER

    fun getInterstitialAdUnitId(): String = Constants.AdUnitIds.INTERSTITIAL

    fun getAppOpenAdUnitId(): String = Constants.AdUnitIds.APP_OPEN

    /** Builds a standard [AdRequest] with no extra targeting. */
    fun buildAdRequest(): AdRequest = AdRequest.Builder().build()
}
