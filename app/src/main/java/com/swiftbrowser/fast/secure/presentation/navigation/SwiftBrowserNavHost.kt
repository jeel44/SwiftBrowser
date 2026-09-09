package com.swiftbrowser.fast.secure.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.analytics.FirebaseAnalytics
import com.swiftbrowser.fast.secure.core.analytics.AnalyticsHelper
import com.swiftbrowser.fast.secure.presentation.bookmarks.BookmarksScreen
import com.swiftbrowser.fast.secure.presentation.browser.BrowserScreen
import com.swiftbrowser.fast.secure.presentation.downloads.DownloadsScreen
import com.swiftbrowser.fast.secure.presentation.history.HistoryScreen
import com.swiftbrowser.fast.secure.presentation.home.HomeScreen
import com.swiftbrowser.fast.secure.presentation.settings.SettingsScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.swiftbrowser.fast.secure.presentation.language.LanguageScreen
import com.swiftbrowser.fast.secure.presentation.language.LanguageViewModel
import com.swiftbrowser.fast.secure.presentation.onboarding.OnboardingScreen
import com.swiftbrowser.fast.secure.presentation.splash.SplashScreen

@Composable
fun SwiftBrowserNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route,
) {
    val context = LocalContext.current
    val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val screenName = when (destination.route) {
                Screen.Splash.route -> "Splash"
                Screen.Language.route -> "Language"
                Screen.Onboarding.route -> "Onboarding"
                Screen.Home.route -> "Home"
                Screen.Browser.route -> "Browser"
                Screen.Downloads.route -> "Downloads"
                Screen.Bookmarks.route -> "Bookmarks"
                Screen.History.route -> "History"
                Screen.Settings.route -> "Settings"
                else -> destination.route ?: "Unknown"
            }
            AnalyticsHelper.logScreen(firebaseAnalytics, screenName)
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {

        // ── Splash ──
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        // ── Onboarding ──
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }

        // ── Language ──
        composable(Screen.Language.route) {
            val viewModel: LanguageViewModel = hiltViewModel()
            LanguageScreen(
                browserPreferences = viewModel.browserPreferences,
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Language.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ──
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToBrowser = { url ->
                    navController.navigate(Screen.Browser.createRoute(url))
                },
                onNavigateToBookmarks = {
                    navController.navigate(Screen.Bookmarks.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToDownloads = {
                    navController.navigate(Screen.Downloads.route)
                },
            )
        }

        // ── Browser ──
        composable(
            route = Screen.Browser.route,
            arguments = listOf(
                navArgument(Screen.Browser.ARG_URL) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString(Screen.Browser.ARG_URL) ?: ""
            Log.d("BrowserDebug", "Browser composable entered — initialUrl='$url'")
            BrowserScreen(
                initialUrl = url,
                onNavigateToBookmarks = { navController.navigate(Screen.Bookmarks.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateUp = { navController.navigateUp() },
            )
        }

        // ── Bookmarks ──
        composable(Screen.Bookmarks.route) {
            BookmarksScreen(
                onBookmarkClicked = { url ->
                    navController.navigate(Screen.Browser.createRoute(url))
                },
                onNavigate = { route ->
                    if (route == Screen.Home.route) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onNavigateUp = { navController.navigateUp() },
            )
        }

        // ── History ──
        composable(Screen.History.route) {
            HistoryScreen(
                onHistoryItemClicked = { url ->
                    navController.navigate(Screen.Browser.createRoute(url))
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateUp = { navController.navigateUp() },
            )
        }

        // ── Downloads ──
        composable(Screen.Downloads.route) {
            DownloadsScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Browser.createRoute(""))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }

        // ── Settings ──
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
