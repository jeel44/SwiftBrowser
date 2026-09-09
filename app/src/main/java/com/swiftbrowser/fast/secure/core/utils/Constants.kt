package com.swiftbrowser.fast.secure.core.utils

import com.swiftbrowser.fast.secure.BuildConfig
import com.swiftbrowser.fast.secure.domain.model.SpeedDialSite

/**
 * Application-wide constants. All magic values live here — never scatter strings/numbers across
 * the codebase.
 */
object Constants {

    // ──────────────────────────────── Network ────────────────────────────────

    /** Base URL for any backend API calls (e.g. news feed, sync). */
    const val BASE_URL = "https://api.swiftbrowser.app/"

    // ──────────────────────────────── Browser ────────────────────────────────

    const val DEFAULT_HOME_URL = "https://google.com"
    const val SEARCH_ENGINE_GOOGLE = "https://www.google.com/search?q="
    const val SEARCH_ENGINE_BING = "https://www.bing.com/search?q="
    const val SEARCH_ENGINE_DUCKDUCKGO = "https://duckduckgo.com/?q="
    const val SEARCH_ENGINE_YAHOO = "https://search.yahoo.com/search?p="
    const val SEARCH_ENGINE_ECOSIA = "https://www.ecosia.org/search?q="

    /** Default value for [BrowserPreferences.defaultSearchEngine]. */
    const val DEFAULT_SEARCH_ENGINE_KEY = "google"

    val SEARCH_ENGINE_MAP = mapOf(
        "google" to SEARCH_ENGINE_GOOGLE,
        "bing" to SEARCH_ENGINE_BING,
        "duckduckgo" to SEARCH_ENGINE_DUCKDUCKGO,
        "yahoo" to SEARCH_ENGINE_YAHOO,
        "ecosia" to SEARCH_ENGINE_ECOSIA,
    )

    /**
     * User-agent string that identifies SwiftBrowser. Matches Chrome's format so sites render
     * correctly and don't serve degraded "mobile lite" pages.
     */
    const val DEFAULT_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 SwiftBrowser/1.0"

    // ──────────────────────────────── External Links ────────────────────────────────

    /** Target URL for the home-screen "Special Offer" CTA. */
    const val SPECIAL_OFFER_URL = "https://shop.crictechnow.com"

    /** Facebook share-post URL opened by the home-screen "Special Offer" CTA. */
    const val FACEBOOK_POST_URL = "https://www.facebook.com/permalink.php?story_fbid=pfbid036kEFV4LHdLo4SHeMJj91S9Yy3Ca1aeVtQ9Qb7ev1NY8ni8AgmeUHuNoGKsTotmCQl&id=61590492010680"

    // ──────────────────────────────── Database ────────────────────────────────

    const val DATABASE_NAME = "swiftbrowser_db"
    const val DATABASE_VERSION = 3

    const val NEWS_CACHE_EXPIRY_MS = 30 * 60 * 1000L // 30 minutes

    // ──────────────────────────────── DataStore ────────────────────────────────

    const val DATASTORE_NAME = "swift_browser_prefs"

    // ──────────────────────────────── AdMob (Test IDs) ────────────────────────────────

    object AdUnitIds {
        val APP_OPEN = if (BuildConfig.DEBUG)
            "ca-app-pub-3940256099942544/9257395921"
        else
            "/23349644043/swift_browser_app_open"

        val INTERSTITIAL = if (BuildConfig.DEBUG)
            "ca-app-pub-3940256099942544/1033173712"
        else
            "/23349644043/swift_browser_interstitial"

        val NATIVE = if (BuildConfig.DEBUG)
            "ca-app-pub-3940256099942544/2247696110"
        else
            "/23349644043/swift_browser_native"

        val BANNER = if (BuildConfig.DEBUG)
            "ca-app-pub-3940256099942544/6300978111"
        else
            "/23349644043/swift_browser_banner"
    }

    // ──────────────────────────────── Speed Dial ────────────────────────────────

    /** Default speed-dial entries shown on the home screen. */
    val DEFAULT_SPEED_DIAL_SITES = listOf(
        SpeedDialSite(1, "Google",    "https://www.google.com",    0xFFEA4335L, "G"),
        SpeedDialSite(2, "YouTube",   "https://www.youtube.com",   0xFFFF0000L, "Y"),
        SpeedDialSite(3, "Facebook",  "https://www.facebook.com",  0xFF1877F2L, "F"),
        SpeedDialSite(4, "WhatsApp",  "https://web.whatsapp.com",  0xFF25D366L, "W"),
        SpeedDialSite(5, "Instagram", "https://www.instagram.com", 0xFFE1306CL, "I"),
        SpeedDialSite(6, "Amazon",    "https://www.amazon.in",     0xFFFF9900L, "A"),
    )

    // ──────────────────────────────── FileProvider ────────────────────────────────

    const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"

    // ──────────────────────────────── Tabs ────────────────────────────────

    /** Maximum number of open tabs before the oldest is silently closed. */
    const val MAX_TABS = 20

    // ──────────────────────────────── Text Size ────────────────────────────────

    const val DEFAULT_TEXT_SIZE = 100
    const val MIN_TEXT_SIZE = 50
    const val MAX_TEXT_SIZE = 200

    // ──────────────────────────────── Timing ────────────────────────────────

    /** Minimum milliseconds between interstitial ad shows to avoid annoying the user. */
    const val INTERSTITIAL_MIN_INTERVAL_MS = 180_000L // 3 minutes

    /** Days between prompting user to set SwiftBrowser as default browser. */
    const val DEFAULT_BROWSER_PROMPT_INTERVAL_DAYS = 7L
}
