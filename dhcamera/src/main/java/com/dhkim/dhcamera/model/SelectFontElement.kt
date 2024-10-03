package com.dhkim.dhcamera.model

import androidx.compose.runtime.Stable

@Stable
internal data class SelectFontElement(
    val isSelected: Boolean = false,
    val font: FontElement = FontElement()
)
