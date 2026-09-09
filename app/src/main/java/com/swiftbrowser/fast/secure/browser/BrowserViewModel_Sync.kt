/*
 * Swift Browser - A premium, private, and secure web browser.
 * Copyright (C) 2026 RebelRoot Ltd
 */

package com.swiftbrowser.fast.secure.browser

import android.util.Log
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.WebExtensionController

internal const val SWIFT_SYNC_EXTENSION_ID = "sync@swiftbrowser.app"

internal fun BrowserViewModel.installSwiftSyncExtension(runtime: GeckoRuntime) {
    runtime.webExtensionController.ensureBuiltIn(
        "resource://android/assets/web_extensions/swift_sync/",
        SWIFT_SYNC_EXTENSION_ID
    ).accept(
        { ext ->
            ext?.let {
                runtime.webExtensionController.setAllowedInPrivateBrowsing(it, true)
                runtime.webExtensionController.enable(it, WebExtensionController.EnableSource.APP)
                Log.i(BrowserViewModel.Companion.TAG, "Swift Sync pre-installed built-in WebExtension active.")
            }
        },
        { error ->
            Log.e(BrowserViewModel.Companion.TAG, "Failed to load Swift Sync WebExtension", error)
        }
    )
}
