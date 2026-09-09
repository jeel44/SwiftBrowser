package com.swiftbrowser.fast.secure.core.ads

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object RemoteConfigManager {

    private const val TAG = "RC_DEBUG"
    private val rc = Firebase.remoteConfig

    // ── Ad Unit ID keys ──────────────────────────────────────────
    const val KEY_APP_OPEN_AD_ID             = "swift_app_open_ad_id"
    const val KEY_SPLASH_INTERSTITIAL_AD_ID  = "swift_splash_interstitial_ad_id"
    const val KEY_HOME_BANNER_AD_ID          = "swift_home_banner_ad_id"
    const val KEY_HOME_NATIVE_AD_ID          = "swift_home_native_ad_id"
    const val KEY_BROWSER_INTERSTITIAL_AD_ID = "swift_browser_interstitial_ad_id"
    const val KEY_BROWSER_BANNER_AD_ID       = "swift_browser_banner_ad_id"
    const val KEY_BROWSER_REWARDED_AD_ID     = "swift_browser_rewarded_ad_id"
    const val KEY_DOWNLOADS_BANNER_AD_ID     = "swift_downloads_banner_ad_id"
    const val KEY_DOWNLOADS_REWARDED_AD_ID   = "swift_downloads_rewarded_ad_id"
    const val KEY_SETTINGS_BANNER_AD_ID      = "swift_settings_banner_ad_id"
    const val KEY_BOOKMARKS_BANNER_AD_ID     = "swift_bookmarks_banner_ad_id"
    const val KEY_HISTORY_BANNER_AD_ID       = "swift_history_banner_ad_id"

    // ── Feature flag keys ─────────────────────────────────────────
    const val KEY_SHOW_APP_OPEN_AD           = "swift_show_app_open_ad"
    const val KEY_SHOW_HOME_BANNER           = "swift_show_home_banner"
    const val KEY_SHOW_HOME_NATIVE           = "swift_show_home_native"
    const val KEY_SHOW_BROWSER_INTERSTITIAL  = "swift_show_browser_interstitial"
    const val KEY_SHOW_BROWSER_BANNER        = "swift_show_browser_banner"
    const val KEY_SHOW_BROWSER_REWARDED      = "swift_show_browser_rewarded"
    const val KEY_SHOW_DOWNLOADS_BANNER      = "swift_show_downloads_banner"
    const val KEY_SHOW_DOWNLOADS_REWARDED    = "swift_show_downloads_rewarded"
    const val KEY_SHOW_SETTINGS_BANNER       = "swift_show_settings_banner"
    const val KEY_SHOW_BOOKMARKS_BANNER      = "swift_show_bookmarks_banner"
    const val KEY_SHOW_HISTORY_BANNER        = "swift_show_history_banner"

    // ── App Open — two separate IDs ──────────────────────────────
    const val KEY_SPLASH_APP_OPEN_AD_ID        = "swift_splash_app_open_ad_id"
    const val KEY_BACKGROUND_APP_OPEN_AD_ID    = "swift_background_app_open_ad_id"
    const val KEY_SHOW_SPLASH_APP_OPEN         = "swift_show_splash_app_open"
    const val KEY_SHOW_BACKGROUND_APP_OPEN     = "swift_show_background_app_open"

    // ── Onboarding ────────────────────────────────────────────────
    const val KEY_ONBOARDING_NATIVE_AD_ID      = "swift_onboarding_native_ad_id"
    const val KEY_ONBOARDING_INTERSTITIAL_AD_ID = "swift_onboarding_interstitial_ad_id"
    const val KEY_SHOW_ONBOARDING_NATIVE       = "swift_show_onboarding_native"
    const val KEY_SHOW_ONBOARDING_INTERSTITIAL = "swift_show_onboarding_interstitial"

    // ── Language ──────────────────────────────────────────────────
    const val KEY_LANGUAGE_INTERSTITIAL_AD_ID  = "swift_language_interstitial_ad_id"
    const val KEY_SHOW_LANGUAGE_INTERSTITIAL   = "swift_show_language_interstitial"

    // ── Timing keys ───────────────────────────────────────────────
    const val KEY_BROWSER_INTERSTITIAL_COOLDOWN = "swift_browser_interstitial_cooldown_ms"

    // ── Browser interstitial session control ──────────────────────
    const val KEY_BROWSER_INTERSTITIAL_ENABLED         = "swift_browser_interstitial_enabled"
    const val KEY_BROWSER_INTERSTITIAL_MAX_PER_SESSION = "swift_browser_interstitial_max_per_session"

    // ── Safe defaults — all OFF, all IDs empty ────────────────────
    private val DEFAULTS = mapOf(
        KEY_APP_OPEN_AD_ID             to "",
        KEY_SPLASH_INTERSTITIAL_AD_ID  to "",
        KEY_HOME_BANNER_AD_ID          to "",
        KEY_HOME_NATIVE_AD_ID          to "",
        KEY_BROWSER_INTERSTITIAL_AD_ID to "",
        KEY_BROWSER_BANNER_AD_ID       to "",
        KEY_BROWSER_REWARDED_AD_ID     to "",
        KEY_DOWNLOADS_BANNER_AD_ID     to "",
        KEY_DOWNLOADS_REWARDED_AD_ID   to "",
        KEY_SETTINGS_BANNER_AD_ID      to "",
        KEY_BOOKMARKS_BANNER_AD_ID     to "",
        KEY_HISTORY_BANNER_AD_ID       to "",

        KEY_SPLASH_APP_OPEN_AD_ID        to "",
        KEY_BACKGROUND_APP_OPEN_AD_ID    to "",
        KEY_SHOW_SPLASH_APP_OPEN         to false,
        KEY_SHOW_BACKGROUND_APP_OPEN     to false,

        KEY_ONBOARDING_NATIVE_AD_ID      to "",
        KEY_ONBOARDING_INTERSTITIAL_AD_ID to "",
        KEY_SHOW_ONBOARDING_NATIVE       to false,
        KEY_SHOW_ONBOARDING_INTERSTITIAL to false,

        KEY_LANGUAGE_INTERSTITIAL_AD_ID  to "",
        KEY_SHOW_LANGUAGE_INTERSTITIAL   to false,

        KEY_SHOW_APP_OPEN_AD           to false,
        KEY_SHOW_HOME_BANNER           to false,
        KEY_SHOW_HOME_NATIVE           to false,
        KEY_SHOW_BROWSER_INTERSTITIAL  to false,
        KEY_SHOW_BROWSER_BANNER        to false,
        KEY_SHOW_BROWSER_REWARDED      to false,
        KEY_SHOW_DOWNLOADS_BANNER      to false,
        KEY_SHOW_DOWNLOADS_REWARDED    to false,
        KEY_SHOW_SETTINGS_BANNER       to false,
        KEY_SHOW_BOOKMARKS_BANNER      to false,
        KEY_SHOW_HISTORY_BANNER        to false,

        KEY_BROWSER_INTERSTITIAL_COOLDOWN to 90000L,

        KEY_BROWSER_INTERSTITIAL_ENABLED         to false,
        KEY_BROWSER_INTERSTITIAL_MAX_PER_SESSION to 4L,
    )

    var isReady = false
        private set

    fun init() {
        val settings = remoteConfigSettings { minimumFetchIntervalInSeconds = 3600 }
        rc.setConfigSettingsAsync(settings)
        rc.setDefaultsAsync(DEFAULTS)
        Log.d(TAG, "RemoteConfigManager initialized with safe defaults (all ads OFF)")
    }

    suspend fun fetchAndActivate(): Boolean = suspendCancellableCoroutine { cont ->
        rc.fetchAndActivate()
            .addOnSuccessListener { activated ->
                isReady = true
                Log.d(TAG, "RC fetch success — activated=$activated")
                logAllValues()
                cont.resume(true)
            }
            .addOnFailureListener { e ->
                isReady = true
                Log.w(TAG, "RC fetch failed — using safe defaults (all ads OFF): ${e.message}")
                cont.resume(false)
            }
    }

    fun getString(key: String): String = rc.getString(key)
    fun getBoolean(key: String): Boolean = rc.getBoolean(key)
    fun getLong(key: String): Long = rc.getLong(key)

    fun getAdId(key: String): String? = rc.getString(key).takeIf { it.isNotBlank() }

    fun getSplashAppOpenId(): String?     = if (getBoolean(KEY_SHOW_SPLASH_APP_OPEN)) getAdId(KEY_SPLASH_APP_OPEN_AD_ID) else null
    fun getBackgroundAppOpenId(): String? = if (getBoolean(KEY_SHOW_BACKGROUND_APP_OPEN)) getAdId(KEY_BACKGROUND_APP_OPEN_AD_ID) else null
    fun getOnboardingNativeId(): String?  = if (getBoolean(KEY_SHOW_ONBOARDING_NATIVE)) getAdId(KEY_ONBOARDING_NATIVE_AD_ID) else null
    fun getOnboardingInterstitialId(): String? = if (getBoolean(KEY_SHOW_ONBOARDING_INTERSTITIAL)) getAdId(KEY_ONBOARDING_INTERSTITIAL_AD_ID) else null
    fun getLanguageInterstitialId(): String?   = if (getBoolean(KEY_SHOW_LANGUAGE_INTERSTITIAL)) getAdId(KEY_LANGUAGE_INTERSTITIAL_AD_ID) else null
    fun getHomeNativeId(): String?        = if (getBoolean(KEY_SHOW_HOME_NATIVE)) getAdId(KEY_HOME_NATIVE_AD_ID) else null

    fun isBrowserInterstitialEnabled(): Boolean   = getBoolean(KEY_BROWSER_INTERSTITIAL_ENABLED)
    fun getBrowserInterstitialMaxPerSession(): Int = getLong(KEY_BROWSER_INTERSTITIAL_MAX_PER_SESSION).toInt()

    private fun logAllValues() {
        Log.d(TAG, "─── Swift Browser Remote Config ───────────────")
        DEFAULTS.keys.forEach { key ->
            Log.d(TAG, "  $key = ${rc.getValue(key).asString()}")
        }
        Log.d(TAG, "───────────────────────────────────────────────")
    }
}
