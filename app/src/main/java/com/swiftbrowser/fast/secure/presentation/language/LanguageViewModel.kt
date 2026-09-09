package com.swiftbrowser.fast.secure.presentation.language

import androidx.lifecycle.ViewModel
import com.swiftbrowser.fast.secure.data.datastore.BrowserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    val browserPreferences: BrowserPreferences
) : ViewModel()
