package com.swiftbrowser.fast.secure.domain.usecase.bookmark

import com.swiftbrowser.fast.secure.data.repository.BookmarkRepository
import com.swiftbrowser.fast.secure.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns all bookmarks as a [Flow], ordered by creation date descending.
 * Optionally filters by [query] for the search feature.
 */
class GetBookmarksUseCase @Inject constructor(
    private val repository: BookmarkRepository,
) {
    operator fun invoke(query: String = ""): Flow<List<Bookmark>> =
        if (query.isBlank()) repository.getAll() else repository.search(query)
}
