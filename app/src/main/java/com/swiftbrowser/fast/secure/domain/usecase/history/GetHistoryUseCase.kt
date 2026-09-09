package com.swiftbrowser.fast.secure.domain.usecase.history

import com.swiftbrowser.fast.secure.data.repository.HistoryRepository
import com.swiftbrowser.fast.secure.domain.model.HistoryItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns browsing history as a [Flow], ordered by visit time descending.
 * Optionally filters by [query] for the search feature.
 */
class GetHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    operator fun invoke(query: String = ""): Flow<List<HistoryItem>> =
        if (query.isBlank()) repository.getAll() else repository.search(query)
}
