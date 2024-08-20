package com.dhkim.dhcamera.camera.model

import androidx.annotation.ColorRes
import androidx.compose.runtime.Stable
import com.dhkim.dhcamera.R

@Stable
data class SelectColorElement(
    val isSelected: Boolean = false,
    @ColorRes val color: Int = R.color.white
)
