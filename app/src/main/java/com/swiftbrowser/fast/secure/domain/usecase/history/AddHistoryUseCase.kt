package com.swiftbrowser.fast.secure.domain.usecase.history

import com.swiftbrowser.fast.secure.data.repository.HistoryRepository
import com.swiftbrowser.fast.secure.domain.model.HistoryItem
import javax.inject.Inject

/**
 * Records a page visit in history. Private-tab visits must NOT call this use case.
 */
class AddHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke(item: HistoryItem): Long =
        repository.add(item)
}
