package com.swiftbrowser.fast.secure.domain.model

/**
 * Domain model for a single browsing history entry.
 */
data class HistoryItem(
    val id: Long = 0,
    val title: String,
    val url: String,
    val faviconUrl: String? = null,
    val visitedAt: Long = System.currentTimeMillis(),
)
