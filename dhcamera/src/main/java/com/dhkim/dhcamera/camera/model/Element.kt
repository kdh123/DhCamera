package com.dhkim.dhcamera.camera.model

import android.graphics.Typeface
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
        val font: Typeface? = null,
        val alignment: FontAlign = FontAlign.Center,
        @ColorRes val color: Int = R.color.black,
        val prevScale: Float = 1f,
        val scale: Float = 1f,
        val rotation: Float = 0f,
        val centerOffset: Offset = Offset.Zero,
        val offset: Offset = Offset.Zero
    ) : Element(id, prevScale, scale, rotation, centerOffset, offset)

    data class Image(
        val id: String = "${System.currentTimeMillis()}",
        val imageUri: Any = "",
        val prevScale: Float = 1f,
        val scale: Float = 1f,
        val rotation: Float = 0f,
        val centerOffset: Offset = Offset.Zero,
        val offset: Offset = Offset.Zero
    ) : Element(id, prevScale, scale, rotation, centerOffset, offset)
}