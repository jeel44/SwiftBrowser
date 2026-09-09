package com.swiftbrowser.fast.secure.domain.model

/**
 * Represents a single speed-dial tile on the home screen.
 *
 * [iconColor] is stored as a `Long` ARGB value (e.g. `0xFFEA4335L` for Google red) so it can
 * live in DataStore as a primitive without a custom serialiser.
 */
data class SpeedDialSite(
    val id: Int,
    val name: String,
    val url: String,
    val iconColor: Long,
    val iconLabel: String,
    val isDefault: Boolean = true,
    val drawableRes: Int = 0,
)
