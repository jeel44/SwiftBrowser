package com.swiftbrowser.fast.secure.core.utils

import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ──────────────────────────────── String Extensions ────────────────────────────────

/**
 * Returns a fully-qualified URL. Bare search terms become a search-engine query; incomplete
 * URLs (missing scheme) are prefixed with https://.
 */
fun String.toSearchOrUrl(searchEngineUrl: String = Constants.SEARCH_ENGINE_GOOGLE): String {
    val trimmed = this.trim()
    return when {
        trimmed.isBlank() -> Constants.DEFAULT_HOME_URL
        URLUtil.isValidUrl(trimmed) -> trimmed
        trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
        else -> "$searchEngineUrl${Uri.encode(trimmed)}"
    }
}

/** Returns just the host/domain portion of a URL for display in the address bar. */
fun String.toDisplayUrl(): String {
    return try {
        Uri.parse(this).host?.removePrefix("www.") ?: this
    } catch (e: Exception) {
        this
    }
}

/** Returns `true` if this string is a well-formed URL with http or https scheme. */
fun String.isUrl(): Boolean =
    URLUtil.isValidUrl(this) && (startsWith("http://") || startsWith("https://"))

// ──────────────────────────────── Long (Timestamp) Extensions ────────────────────────────────

private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
private val fullFormatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())

fun Long.toFormattedDate(): String = dateFormatter.format(Date(this))
fun Long.toFormattedTime(): String = timeFormatter.format(Date(this))
fun Long.toFormattedDateTime(): String = fullFormatter.format(Date(this))

fun Long.isToday(): Boolean {
    val now = System.currentTimeMillis()
    val dayMs = 24 * 60 * 60 * 1000L
    return (now - this) < dayMs
}

fun Long.isYesterday(): Boolean {
    val now = System.currentTimeMillis()
    val dayMs = 24 * 60 * 60 * 1000L
    return (now - this) in dayMs..(2 * dayMs)
}

// ──────────────────────────────── Context Extensions ────────────────────────────────

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// ──────────────────────────────── Compose Extensions ────────────────────────────────

/** Converts Dp to pixels — useful when WebView APIs require pixel values. */
@Composable
fun Dp.toPx(): Float {
    val density = LocalDensity.current
    return with(density) { this@toPx.toPx() }
}
