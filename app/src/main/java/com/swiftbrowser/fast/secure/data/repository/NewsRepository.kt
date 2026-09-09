package com.swiftbrowser.fast.secure.data.repository

import com.swiftbrowser.fast.secure.core.base.UiState
import com.swiftbrowser.fast.secure.core.utils.Constants
import com.swiftbrowser.fast.secure.data.local.dao.NewsDao
import com.swiftbrowser.fast.secure.data.local.entity.NewsEntity
import com.swiftbrowser.fast.secure.data.remote.api.ApiService
import com.swiftbrowser.fast.secure.data.remote.rss.NewsSources
import com.swiftbrowser.fast.secure.data.remote.rss.RssParser
import com.swiftbrowser.fast.secure.domain.model.NewsCategory
import com.swiftbrowser.fast.secure.domain.model.NewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val apiService: ApiService,
    private val newsDao: NewsDao,
    private val rssParser: RssParser,
) {

    fun getNews(): Flow<UiState<List<NewsItem>>> = flow {
        emit(UiState.Loading)

        val cached = newsDao.getAll()
        val now = System.currentTimeMillis()
        val isCacheFresh = cached.isNotEmpty() &&
            cached.first().cachedAt > now - Constants.NEWS_CACHE_EXPIRY_MS

        if (isCacheFresh) {
            emit(UiState.Success(injectAds(cached.map { it.toDomain() })))
            return@flow
        }

        try {
            val fresh = fetchAllFeeds()
            if (fresh.isNotEmpty()) {
                newsDao.deleteAll()
                newsDao.insertAll(fresh.mapIndexed { i, item ->
                    item.toEntity(id = i, cachedAt = now)
                })
                emit(UiState.Success(injectAds(fresh)))
            } else if (cached.isNotEmpty()) {
                emit(UiState.Success(injectAds(cached.map { it.toDomain() })))
            } else {
                emit(UiState.Empty)
            }
        } catch (e: Exception) {
            Timber.e(e, "NewsRepository.fetchAllFeeds failed")
            if (cached.isNotEmpty()) {
                emit(UiState.Success(injectAds(cached.map { it.toDomain() })))
            } else {
                emit(UiState.Error(e.localizedMessage ?: "Failed to load news"))
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun fetchAllFeeds(): List<NewsItem> = coroutineScope {
        NewsSources.DEFAULT.map { source ->
            async {
                try {
                    val body = apiService.fetchRss(source.url)
                    rssParser.parse(body.string(), source.name, source.category)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to fetch ${source.name}")
                    emptyList()
                }
            }
        }.flatMap { it.await() }
    }

    private fun injectAds(items: List<NewsItem>): List<NewsItem> {
        val result = mutableListOf<NewsItem>()
        items.forEachIndexed { index, item ->
            result += item
            if ((index + 1) % 5 == 0) {
                result += NewsItem(
                    id = -(index + 1),
                    title = "",
                    source = "",
                    timeAgo = "",
                    thumbnailEmoji = "",
                    url = "",
                    isAd = true,
                    adTitle = "Sponsored",
                    adCta = "Learn More",
                )
            }
        }
        return result
    }

    private fun NewsEntity.toDomain() = NewsItem(
        id = id,
        title = title,
        source = source,
        timeAgo = timeAgo(publishedAt),
        thumbnailEmoji = "",
        url = url,
        category = runCatching { NewsCategory.valueOf(category) }.getOrDefault(NewsCategory.TOP),
        imageUrl = imageUrl,
    )

    private fun NewsItem.toEntity(id: Int, cachedAt: Long) = NewsEntity(
        id = id,
        title = title,
        url = url,
        source = source,
        category = category.name,
        publishedAt = System.currentTimeMillis(),
        cachedAt = cachedAt,
        imageUrl = imageUrl,
    )

    private fun timeAgo(publishedAt: Long): String {
        val diff = System.currentTimeMillis() - publishedAt
        return when {
            diff < 3_600_000L  -> "${diff / 60_000}m ago"
            diff < 86_400_000L -> "${diff / 3_600_000}h ago"
            else               -> "${diff / 86_400_000}d ago"
        }
    }
}
