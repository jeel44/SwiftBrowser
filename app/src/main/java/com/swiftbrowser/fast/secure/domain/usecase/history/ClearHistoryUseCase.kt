package com.swiftbrowser.fast.secure.domain.usecase.history

import com.swiftbrowser.fast.secure.data.repository.HistoryRepository
import javax.inject.Inject

/**
 * Wipes all browsing history. Used from the Clear Data settings action.
 */
class ClearHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke() = repository.clearAll()
}
