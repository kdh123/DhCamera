package com.dhkim.dhcamera.model

import androidx.compose.runtime.Stable

data class SelectFontAlignElement(
    val isSelected: Boolean = false,
    val alignment: FontAlign = FontAlign.Center
)

@Stable
enum class FontAlign {
    Center,
    Left,
    Right
}
