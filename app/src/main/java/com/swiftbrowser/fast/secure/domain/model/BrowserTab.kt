package com.swiftbrowser.fast.secure.domain.model

import java.util.UUID

/**
 * Domain model for a browser tab. Tabs are in-memory only — they are not persisted to the
 * database, so this is a pure Kotlin data class with no Room dependency.
 */
data class BrowserTab(
    /** Stable unique ID for the tab's lifetime. */
    val id: String = UUID.randomUUID().toString(),

    val title: String = "New Tab",
    val url: String = "",
    val faviconUrl: String? = null,

    /** `true` for private/incognito tabs — history and cookies must not be persisted. */
    val isPrivate: Boolean = false,

    val isLoading: Boolean = false,
    val loadProgress: Int = 0,

    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
)
