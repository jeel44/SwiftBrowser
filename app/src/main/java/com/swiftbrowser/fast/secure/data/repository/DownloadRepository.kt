package com.swiftbrowser.fast.secure.data.repository

import com.swiftbrowser.fast.secure.data.local.dao.DownloadDao
import com.swiftbrowser.fast.secure.data.local.entity.DownloadEntity
import com.swiftbrowser.fast.secure.data.local.entity.DownloadStatus
import com.swiftbrowser.fast.secure.domain.model.DownloadItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for download data.
 */
@Singleton
class DownloadRepository @Inject constructor(
    private val downloadDao: DownloadDao,
) {

    fun getAll(): Flow<List<DownloadItem>> =
        downloadDao.getAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun add(item: DownloadItem): Long =
        downloadDao.insert(item.toEntity())

    suspend fun updateStatus(systemDownloadId: Long, status: DownloadStatus) =
        downloadDao.updateStatus(systemDownloadId, status)

    suspend fun delete(item: DownloadItem) = downloadDao.delete(item.toEntity())

    suspend fun deleteAll() = downloadDao.deleteAll()

    // ──────────────────────────────── Mappers ────────────────────────────────

    private fun DownloadEntity.toDomain() = DownloadItem(
        id = id,
        systemDownloadId = systemDownloadId,
        fileName = fileName,
        url = url,
        mimeType = mimeType,
        fileSize = fileSize,
        localPath = localPath,
        status = status,
        createdAt = createdAt,
    )

    private fun DownloadItem.toEntity() = DownloadEntity(
        id = id,
        systemDownloadId = systemDownloadId,
        fileName = fileName,
        url = url,
        mimeType = mimeType,
        fileSize = fileSize,
        localPath = localPath,
        status = status,
        createdAt = createdAt,
    )
}
