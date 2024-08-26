package com.dhkim.dhcamera.model

import androidx.annotation.ColorRes
import androidx.compose.ui.geometry.Offset
import com.dhkim.dhcamera.R

sealed class Element(
    val _id: String,
    var _prevScale: Float,
    var _scale: Float,
    var _rotation: Float,
    var _centerOffset: Offset,
    var _offset: Offset
) {
    data class Text(
        val id: String = "${System.currentTimeMillis()}",
        val text: String,
        val fontId: Int = 0,
        val alignment: FontAlign = FontAlign.Center,
        @ColorRes val color: Int = R.color.black,
        var prevScale: Float = 1f,
        var scale: Float = 1f,
        var rotation: Float = 0f,
        var centerOffset: Offset = Offset.Zero,
        var offset: Offset = Offset.Zero
    ) : Element(
        _id = id,
        _prevScale = prevScale,
        _scale = scale,
        _rotation = rotation,
        _centerOffset = centerOffset,
        _offset = offset
    )

    data class Image(
        val id: String = "${System.currentTimeMillis()}",
        val imageUri: Any = "",
        var prevScale: Float = 1f,
        var scale: Float = 1f,
        var rotation: Float = 0f,
        var centerOffset: Offset = Offset.Zero,
        var offset: Offset = Offset.Zero
    ) : Element(
        _id = id,
        _prevScale = prevScale,
        _scale = scale,
        _rotation = rotation,
        _centerOffset = centerOffset,
        _offset = offset
    )
}