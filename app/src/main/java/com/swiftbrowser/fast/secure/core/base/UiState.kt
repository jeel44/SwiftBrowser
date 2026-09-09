package com.swiftbrowser.fast.secure.core.base

/**
 * Sealed class representing the four possible states of any async UI data load.
 * ViewModels expose `StateFlow<UiState<T>>`; screens collect and branch on the type.
 */
sealed class UiState<out T> {

    /** Data is being fetched — show a loading indicator. */
    data object Loading : UiState<Nothing>()

    /** Data loaded successfully. */
    data class Success<T>(val data: T) : UiState<T>()

    /** An error occurred. [throwable] is available for crash reporting but not shown to users. */
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : UiState<Nothing>()

    /** The operation succeeded but returned no results (empty list, no search hits, etc.). */
    data object Empty : UiState<Nothing>()

    // ──────────────────────────────── Convenience helpers ────────────────────────────────

    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isEmpty: Boolean get() = this is Empty

    /** Returns the data if [Success], otherwise `null`. */
    fun getOrNull(): T? = (this as? Success)?.data
}
