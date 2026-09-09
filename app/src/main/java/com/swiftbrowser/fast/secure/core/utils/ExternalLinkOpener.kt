package com.swiftbrowser.fast.secure.core.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.swiftbrowser.fast.secure.BuildConfig

private const val FB_DEBUG_TAG = "FB_BROWSER_DEBUG"

/**
 * Opens [postUrl] via standard ACTION_VIEW resolution — not scoped to any package — so Android's
 * App Links resolution can route it to the Facebook app when installed (Facebook registers itself
 * as the preferred handler for facebook.com share URLs). Falls back to Chrome Custom Tabs if
 * resolution fails or no app claims the link.
 */
fun openFacebookPost(context: Context, postUrl: String) {
    val uri = Uri.parse(postUrl)
    Log.d(FB_DEBUG_TAG, "Constructed URI: $uri")

    val intent = Intent(Intent.ACTION_VIEW, uri)

    try {
        val resolvedActivityInfo = intent.resolveActivity(context.packageManager)
        if (resolvedActivityInfo != null) {
            Log.d(
                FB_DEBUG_TAG,
                "resolveActivity() resolved to packageName=${resolvedActivityInfo.packageName}, " +
                    "className=${resolvedActivityInfo.className}",
            )
            Log.d(FB_DEBUG_TAG, "Branch taken: Facebook app (resolveActivity returned a component)")
            try {
                Log.d(FB_DEBUG_TAG, "About to call startActivity()")
                context.startActivity(intent)
                Log.d(FB_DEBUG_TAG, "startActivity() returned successfully")
            } catch (e: Exception) {
                Log.d(FB_DEBUG_TAG, "startActivity() threw ${e.javaClass.simpleName} - ${e.message}")
                throw e
            }
        } else {
            Log.d(FB_DEBUG_TAG, "resolveActivity() returned null")
            Log.d(FB_DEBUG_TAG, "Branch taken: Custom Tabs fallback (reason=resolveActivity was null)")
            context.openInCustomTabs(postUrl)
        }
    } catch (e: ActivityNotFoundException) {
        Log.d(
            FB_DEBUG_TAG,
            "resolveActivity()/startActivity() threw ActivityNotFoundException - ${e.message}",
        )
        Log.d(FB_DEBUG_TAG, "Branch taken: Custom Tabs fallback (reason=ActivityNotFoundException)")
        context.openInCustomTabs(postUrl)
    }
}

private fun Context.openInCustomTabs(url: String) {
    CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(url))
}

// ─── Debug-only fb:// URI scheme testing (Variants F–H) ────────────────────
// Temporary, additive testing harness — does not affect openFacebookPost() above.
// Triggered manually from a debug-only section on the home screen.

/**
 * Variant F: fb://faceweb/f?href=<encoded url>, package-scoped to com.facebook.katana.
 */
fun testFbVariantF(context: Context) {
    if (!BuildConfig.DEBUG) return
    val encodedHref = Uri.encode("https://shop.crictechnow.com")
    val uri = Uri.parse("fb://faceweb/f?href=$encodedHref")
    Log.d(FB_DEBUG_TAG, "[Variant F] Constructed URI: $uri")
    val intent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.facebook.katana")
    logResolveAndStart(context, "Variant F", intent)
}

/**
 * Variant G: fb://browser — no href param, just testing whether the scheme resolves.
 */
fun testFbVariantG(context: Context) {
    if (!BuildConfig.DEBUG) return
    val uri = Uri.parse("fb://browser")
    Log.d(FB_DEBUG_TAG, "[Variant G] Constructed URI: $uri")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    logResolveAndStart(context, "Variant G", intent)
}

/**
 * Variant H: fb://open — no href param, just testing whether the scheme resolves.
 */
fun testFbVariantH(context: Context) {
    if (!BuildConfig.DEBUG) return
    val uri = Uri.parse("fb://open")
    Log.d(FB_DEBUG_TAG, "[Variant H] Constructed URI: $uri")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    logResolveAndStart(context, "Variant H", intent)
}

private fun logResolveAndStart(context: Context, label: String, intent: Intent) {
    try {
        val resolvedActivityInfo = intent.resolveActivity(context.packageManager)
        if (resolvedActivityInfo != null) {
            Log.d(
                FB_DEBUG_TAG,
                "[$label] resolveActivity() resolved to packageName=${resolvedActivityInfo.packageName}, " +
                    "className=${resolvedActivityInfo.className}",
            )
        } else {
            Log.d(FB_DEBUG_TAG, "[$label] resolveActivity() returned null")
        }
        try {
            Log.d(FB_DEBUG_TAG, "[$label] About to call startActivity()")
            context.startActivity(intent)
            Log.d(FB_DEBUG_TAG, "[$label] startActivity() returned successfully")
        } catch (e: Exception) {
            Log.d(FB_DEBUG_TAG, "[$label] startActivity() threw ${e.javaClass.simpleName} - ${e.message}")
        }
    } catch (e: ActivityNotFoundException) {
        Log.d(
            FB_DEBUG_TAG,
            "[$label] resolveActivity()/startActivity() threw ActivityNotFoundException - ${e.message}",
        )
    }
}
