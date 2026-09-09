package com.swiftbrowser.fast.secure.browser

data class CustomSearchEngine(
    val name: String,
    val queryUrl: String,
    val suggestUrl: String = ""
)
