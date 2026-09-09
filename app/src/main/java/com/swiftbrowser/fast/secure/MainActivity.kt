package com.swiftbrowser.fast.secure

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.swiftbrowser.fast.secure.core.language.LocaleHelper
import com.swiftbrowser.fast.secure.presentation.navigation.SwiftBrowserNavHost
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBrowserTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.HttpURLConnection
import java.net.URL

/**
 * Single Activity host. All navigation is handled inside [SwiftBrowserNavHost].
 * WebView back-press is handled inside [BrowserScreen] via BackHandler so no
 * activity-level state is needed here.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        var pendingNotificationUrl: String? = null
    }

    override fun attachBaseContext(newBase: Context) {
        val code = newBase
            .getSharedPreferences("swift_lang_prefs", Context.MODE_PRIVATE)
            .getString("selected_language", "system") ?: "system"
        android.util.Log.e("LANG_DEBUG", "attachBaseContext code: $code")
        super.attachBaseContext(LocaleHelper.wrap(newBase, code))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        trackInstall()
        setContent {
            SwiftBrowserTheme {
                SwiftBrowserNavHost(
                    navController = rememberNavController(),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        intent?.getStringExtra("notification_url")?.let { url ->
            if (url.isNotEmpty()) {
                pendingNotificationUrl = url
            }
        }
    }

    private fun trackInstall() {
        val prefs = getSharedPreferences("swift_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("install_tracked", false)) return
        Thread {
            try {
                val url = URL("https://appinstall.sixfigurefinance.com/api/track/install")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.doOutput = true
                conn.outputStream.write(
                    """{"package":"com.swiftbrowser.fast.secure"}""".toByteArray()
                )
                val code = conn.responseCode
                conn.disconnect()
                if (code == 200) {
                    prefs.edit().putBoolean("install_tracked", true).apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}