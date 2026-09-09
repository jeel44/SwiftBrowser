/*
 * Swift Browser - Shared remote-tab tracking store
 * Copyright (C) 2026 RebelRoot Ltd
 */

package com.swiftbrowser.fast.secure.sync.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TabInfo(
    val title: String,
    val url: String,
    val iconUrl: String? = null,
    val lastAccessed: Long = System.currentTimeMillis()
)

data class RemoteDeviceTabs(
    val deviceId: String,
    val deviceName: String,
    val deviceType: String = "desktop",
    val lastModified: Long = System.currentTimeMillis(),
    val tabs: List<TabInfo>
)

/**
 * Tracks open tabs reported by paired remote devices, keyed by device ID.
 *
 * Extracted from the former MozillaTabBridge (Phase 3b removal of Firefox
 * Account Sync): the Firefox-specific BSO record conversion methods
 * (exportToBsoRecord/parseBsoRecords) were removed along with that feature,
 * but this generic tab-tracking store is also used by the Swift Sync Mesh
 * LAN transport (LanWebSocketServer.kt) and its "synced tabs" UI
 * (SyncedTabsSheet.kt), so it's kept as shared infrastructure.
 */
class TabSyncBridge {

    private val _remoteTabsFlow = MutableStateFlow<List<RemoteDeviceTabs>>(emptyList())
    val remoteTabsFlow: StateFlow<List<RemoteDeviceTabs>> = _remoteTabsFlow.asStateFlow()

    private val remoteTabsByDevice = mutableMapOf<String, RemoteDeviceTabs>()

    fun updateDirectRemoteTabs(deviceId: String, deviceName: String, tabs: List<TabInfo>) {
        synchronized(remoteTabsByDevice) {
            remoteTabsByDevice[deviceId] = RemoteDeviceTabs(
                deviceId = deviceId,
                deviceName = deviceName,
                deviceType = "desktop",
                lastModified = System.currentTimeMillis(),
                tabs = tabs
            )
            _remoteTabsFlow.value = remoteTabsByDevice.values.toList()
        }
    }

    /**
     * Stores synced tabs received from a paired Swift Sync Mesh device.
     */
    fun updateRemoteDeviceTabs(remoteDevice: RemoteDeviceTabs) {
        remoteTabsByDevice[remoteDevice.deviceId] = remoteDevice
        _remoteTabsFlow.value = remoteTabsByDevice.values.toList()
    }

    /**
     * Returns all synced remote tabs grouped by device.
     */
    fun getAllRemoteDeviceTabs(): List<RemoteDeviceTabs> {
        return remoteTabsByDevice.values.toList()
    }

    fun clearRemoteTabs() {
        remoteTabsByDevice.clear()
        _remoteTabsFlow.value = emptyList()
    }
}
