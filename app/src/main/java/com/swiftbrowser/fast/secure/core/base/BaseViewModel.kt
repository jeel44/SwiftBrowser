package com.swiftbrowser.fast.secure.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Base ViewModel that provides:
 * - A [_uiState] `StateFlow` for screen state
 * - A [_events] `SharedFlow` for one-shot UI events (navigation, snackbars)
 * - A [launchWithErrorHandling] helper that catches exceptions and emits [UiState.Error]
 *
 * Every feature ViewModel should extend this class.
 */
abstract class BaseViewModel<T> : ViewModel() {

    protected val _uiState = MutableStateFlow<UiState<T>>(UiState.Loading)

    /** Exposed to UI as read-only; screens collect this to render state. */
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()

    protected val _events = MutableSharedFlow<ViewModelEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** One-shot events that should not be re-delivered after process death. */
    val events: SharedFlow<ViewModelEvent> = _events.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "${this::class.simpleName} uncaught exception")
        _uiState.value = UiState.Error(
            message = throwable.localizedMessage ?: "An unexpected error occurred",
            throwable = throwable,
        )
    }

    /**
     * Launches a coroutine on [dispatcher] (defaults to [Dispatchers.IO] for data work).
     * Automatically catches any uncaught [Throwable] and transitions to [UiState.Error].
     */
    protected fun launchWithErrorHandling(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend () -> Unit,
    ) {
        viewModelScope.launch(dispatcher + exceptionHandler) {
            block()
        }
    }

    /** Emits a one-shot event to the UI layer. */
    protected fun emitEvent(event: ViewModelEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}

/**
 * Base class for one-shot UI events emitted via [BaseViewModel.events].
 * Subclasses define feature-specific events (e.g. NavigateToBrowser, ShowSnackbar).
 */
abstract class ViewModelEvent
