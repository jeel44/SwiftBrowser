package com.swiftbrowser.fast.secure.core.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsHelper {

    fun logScreen(analytics: FirebaseAnalytics, screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logEvent(analytics: FirebaseAnalytics, eventName: String, params: Bundle? = null) {
        analytics.logEvent(eventName, params)
    }
}
