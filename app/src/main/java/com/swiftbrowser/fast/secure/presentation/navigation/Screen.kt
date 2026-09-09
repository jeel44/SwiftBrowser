package com.swiftbrowser.fast.secure.presentation.navigation

import android.net.Uri

/**
 * Type-safe navigation routes for the single-activity navigation graph.
 * All arguments use query-parameter syntax so Navigation Compose can parse them.
 */
sealed class Screen(val route: String) {

    data object Home : Screen("home")

    data object Browser : Screen("browser?url={url}") {
        const val ARG_URL = "url"
        // Uri.encode is required — bare URLs containing "://" and "?" break Navigation's
        // route matcher and cause the argument to arrive as an empty string.
        fun createRoute(url: String): String = "browser?url=${Uri.encode(url)}"
    }

    data object Bookmarks : Screen("bookmarks")

    data object History : Screen("history")

    data object Settings : Screen("settings")

    data object Downloads : Screen("downloads")

    data object Splash : Screen("splash")

    data object Onboarding : Screen("onboarding")

    data object Language : Screen("language")
}
