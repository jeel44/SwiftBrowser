package com.swiftbrowser.fast.secure.domain.usecase.bookmark

import com.swiftbrowser.fast.secure.data.repository.BookmarkRepository
import com.swiftbrowser.fast.secure.domain.model.Bookmark
import javax.inject.Inject

/**
 * Adds a new bookmark. Returns the Row ID of the inserted entry.
 */
class AddBookmarkUseCase @Inject constructor(
    private val repository: BookmarkRepository,
) {
    suspend operator fun invoke(bookmark: Bookmark): Long =
        repository.add(bookmark)
}
