package com.dhkim.dhcamera.model

import androidx.compose.runtime.Stable

internal data class SelectFontAlignElement(
    val isSelected: Boolean = false,
    val alignment: FontAlign = FontAlign.Center
)

@Stable
internal enum class FontAlign {
    Center,
    Left,
    Right
}
