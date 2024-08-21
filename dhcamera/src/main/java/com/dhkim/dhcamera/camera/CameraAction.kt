package com.dhkim.dhcamera.camera

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap

sealed interface CameraAction {

    data class ChangeBackgroundImage(val selectedIndex: Int) : CameraAction
    data object SavingPhoto : CameraAction
    data class SavedPhoto(val savedUrl: String) : CameraAction
    data class TakePhoto(val bitmap: Bitmap, val backgroundBitmap: ImageBitmap) : CameraAction
    data object ResetPhoto : CameraAction
    data class Typing(val text: String) : CameraAction
    data object ClearText: CameraAction

    data class ChangeFont(val selectedIndex: Int) : CameraAction
    data class ChangeFontColor(val selectedIndex: Int) : CameraAction
    data object AddText : CameraAction

    data class ChangeElementProperties(
        val id: String,
        val scale: Float,
        val rotation: Float,
        val offset: Offset
    ) : CameraAction
}