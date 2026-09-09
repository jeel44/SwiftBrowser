package com.swiftbrowser.fast.secure.domain.usecase.bookmark

import com.swiftbrowser.fast.secure.data.repository.BookmarkRepository
import com.swiftbrowser.fast.secure.domain.model.Bookmark
import javax.inject.Inject

/**
 * Deletes a single bookmark by its full domain model.
 */
class DeleteBookmarkUseCase @Inject constructor(
    private val repository: BookmarkRepository,
) {
    suspend operator fun invoke(bookmark: Bookmark) =
        repository.delete(bookmark)
}
