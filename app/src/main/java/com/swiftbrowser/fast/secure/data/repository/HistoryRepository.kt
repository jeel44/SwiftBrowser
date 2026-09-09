package com.swiftbrowser.fast.secure.data.repository

import com.swiftbrowser.fast.secure.data.local.dao.HistoryDao
import com.swiftbrowser.fast.secure.data.local.entity.HistoryEntity
import com.swiftbrowser.fast.secure.domain.model.HistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for browsing history data.
 */
@Singleton
class HistoryRepository @Inject constructor(
    private val historyDao: HistoryDao,
) {

    fun getAll(): Flow<List<HistoryItem>> =
        historyDao.getAll().map { entities -> entities.map { it.toDomain() } }

    fun search(query: String): Flow<List<HistoryItem>> =
        historyDao.search(query).map { entities -> entities.map { it.toDomain() } }

    suspend fun getRecent(limit: Int = 20): List<HistoryItem> =
        historyDao.getRecent(limit).map { it.toDomain() }

    suspend fun add(item: HistoryItem): Long =
        historyDao.insert(item.toEntity())

    suspend fun delete(id: Long) = historyDao.deleteById(id)

    suspend fun clearAll() = historyDao.deleteAll()

    suspend fun deleteOlderThan(timestamp: Long) = historyDao.deleteOlderThan(timestamp)

    // ──────────────────────────────── Mappers ────────────────────────────────

    private fun HistoryEntity.toDomain() = HistoryItem(
        id = id,
        title = title,
        url = url,
        faviconUrl = faviconUrl,
        visitedAt = visitedAt,
    )

    private fun HistoryItem.toEntity() = HistoryEntity(
        id = id,
        title = title,
        url = url,
        faviconUrl = faviconUrl,
        visitedAt = visitedAt,
    )
}
