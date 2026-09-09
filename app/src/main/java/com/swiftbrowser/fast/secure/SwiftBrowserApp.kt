package com.swiftbrowser.fast.secure

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.swiftbrowser.fast.secure.core.ads.AppOpenAdManager
import com.swiftbrowser.fast.secure.core.ads.InstallReferrerChecker
import com.swiftbrowser.fast.secure.core.ads.RemoteConfigManager
import com.swiftbrowser.fast.secure.monetization.AdManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber
import javax.inject.Inject

/**
 * Application entry point. Hilt component generation root.
 * Initializes crash reporting, logging, and the ad SDK on startup.
 */
@HiltAndroidApp
class SwiftBrowserApp : Application() {

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var installReferrerChecker: InstallReferrerChecker

    private val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        RemoteConfigManager.init()
        AppOpenAdManager.init(this)
        initTimber()
        initFirebase()
        initAds()
        installUncaughtExceptionHandler()
        Log.d("AD_REFERRER_DEBUG", "SwiftBrowserApp.onCreate: invoking installReferrerChecker.checkIfNeeded()")
        installReferrerChecker.checkIfNeeded(appScope)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Timber.d("FCM Token: ${task.result}")
            }
        }
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initFirebase() {
        // Disable Crashlytics data collection in debug builds to avoid polluting production data
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    private fun initAds() {
        // Ads are disabled in debug builds via BuildConfig.ENABLE_ADS
        if (BuildConfig.ENABLE_ADS) {
            adManager.initialize()
        }
    }

    /**
     * Forwards uncaught exceptions to Crashlytics before the default handler terminates the process.
     * Ensures we get a crash report even for non-fatal issues that would otherwise be silent.
     */
    private fun installUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "Uncaught exception on thread ${thread.name}")
            if (BuildConfig.ENABLE_ANALYTICS) {
                Firebase.crashlytics.recordException(throwable)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
