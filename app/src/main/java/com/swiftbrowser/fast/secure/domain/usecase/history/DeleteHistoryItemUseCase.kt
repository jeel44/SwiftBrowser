package com.swiftbrowser.fast.secure.domain.usecase.history

import com.swiftbrowser.fast.secure.data.repository.HistoryRepository
import javax.inject.Inject

class DeleteHistoryItemUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}
