package com.swiftbrowser.fast.secure.domain.model

import com.swiftbrowser.fast.secure.data.local.entity.DownloadStatus

/**
 * Domain model for a file download entry.
 */
data class DownloadItem(
    val id: Long = 0,
    val systemDownloadId: Long = -1L,
    val fileName: String,
    val url: String,
    val mimeType: String? = null,
    val fileSize: Long = -1L,
    val localPath: String? = null,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
)
