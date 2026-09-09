package com.swiftbrowser.fast.secure.domain.model

/**
 * Domain model for a saved bookmark. Decoupled from both the Room entity and any UI state.
 */
data class Bookmark(
    val id: Long = 0,
    val title: String,
    val url: String,
    val faviconUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val folder: String = "default",
)
