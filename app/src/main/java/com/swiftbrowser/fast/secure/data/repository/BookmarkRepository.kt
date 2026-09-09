package com.swiftbrowser.fast.secure.data.repository

import com.swiftbrowser.fast.secure.data.local.dao.BookmarkDao
import com.swiftbrowser.fast.secure.data.local.entity.BookmarkEntity
import com.swiftbrowser.fast.secure.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for bookmark data. Translates between [BookmarkEntity] (data layer)
 * and [Bookmark] (domain layer) so neither layer leaks into the other.
 */
@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao,
) {

    fun getAll(): Flow<List<Bookmark>> =
        bookmarkDao.getAll().map { entities -> entities.map { it.toDomain() } }

    fun search(query: String): Flow<List<Bookmark>> =
        bookmarkDao.search(query).map { entities -> entities.map { it.toDomain() } }

    fun isBookmarked(url: String): Flow<Boolean> = bookmarkDao.isBookmarked(url)

    suspend fun add(bookmark: Bookmark): Long =
        bookmarkDao.insert(bookmark.toEntity())

    suspend fun update(bookmark: Bookmark) =
        bookmarkDao.update(bookmark.toEntity())

    suspend fun delete(bookmark: Bookmark) =
        bookmarkDao.delete(bookmark.toEntity())

    suspend fun deleteAll() = bookmarkDao.deleteAll()

    // ──────────────────────────────── Mappers ────────────────────────────────

    private fun BookmarkEntity.toDomain() = Bookmark(
        id = id,
        title = title,
        url = url,
        faviconUrl = faviconUrl,
        createdAt = createdAt,
        folder = folder,
    )

    private fun Bookmark.toEntity() = BookmarkEntity(
        id = id,
        title = title,
        url = url,
        faviconUrl = faviconUrl,
        createdAt = createdAt,
        folder = folder,
    )
}
