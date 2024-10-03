package com.dhkim.dhcamera.model

import androidx.annotation.ColorRes
import androidx.compose.ui.geometry.Offset
import com.dhkim.dhcamera.R

sealed interface Element {

    val id: String
    var prevScale: Float
    var scale: Float
    var rotation: Float
    var centerOffset: Offset
    var offset: Offset

    data class Text(
        override val id: String = "${System.currentTimeMillis()}",
        override var prevScale: Float = 1f,
        override var scale: Float = 1f,
        override var rotation: Float = 0f,
        override var centerOffset: Offset = Offset.Zero,
        override var offset: Offset = Offset.Zero,
        val text: String,
        val fontId: Int = 0,
        val alignment: FontAlign = FontAlign.Center,
        @ColorRes val color: Int = R.color.black,
    ) : Element

    data class Image(
        override val id: String = "${System.currentTimeMillis()}",
        override var prevScale: Float = 1f,
        override var scale: Float = 1f,
        override var rotation: Float = 0f,
        override var centerOffset: Offset = Offset.Zero,
        override var offset: Offset = Offset.Zero,
        val imageUri: Any = "",
    ) : Element
}