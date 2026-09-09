package com.swiftbrowser.fast.secure.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a tracked download. [systemDownloadId] is the ID returned by
 * [android.app.DownloadManager] and is used to query download progress.
 */
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    /** ID returned by Android's DownloadManager — used to track/cancel the download. */
    @ColumnInfo(name = "system_download_id")
    val systemDownloadId: Long = -1L,

    @ColumnInfo(name = "file_name")
    val fileName: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "mime_type")
    val mimeType: String? = null,

    @ColumnInfo(name = "file_size")
    val fileSize: Long = -1L,

    /** Absolute path to the downloaded file on device storage. */
    @ColumnInfo(name = "local_path")
    val localPath: String? = null,

    @ColumnInfo(name = "status")
    val status: DownloadStatus = DownloadStatus.PENDING,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)

/** Mirrors DownloadManager status constants with a readable enum. */
enum class DownloadStatus {
    PENDING,
    RUNNING,
    PAUSED,
    SUCCESSFUL,
    FAILED,
}
