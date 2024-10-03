package com.dhkim.dhcamera.model

import androidx.annotation.ColorRes
import androidx.compose.runtime.Stable
import com.dhkim.dhcamera.R

@Stable
internal data class SelectColorElement(
    val isSelected: Boolean = false,
    @ColorRes val color: Int = R.color.white
)
