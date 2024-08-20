package com.dhkim.dhcamera.camera.model

import androidx.compose.runtime.Stable

@Stable
data class SelectFontElement(
    val isSelected: Boolean = false,
    val font: FontElement = FontElement()
)
