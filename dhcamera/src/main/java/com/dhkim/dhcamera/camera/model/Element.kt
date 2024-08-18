package com.dhkim.dhcamera.camera.model

import androidx.annotation.ColorRes
import com.dhkim.dhcamera.R

sealed interface Element {

    data class Text(
        val text: String,
        val font: Int = -1,
        @ColorRes val color: Int = R.color.black
    )

    data class Image(
        val imageUri: Any = "",
    )
}