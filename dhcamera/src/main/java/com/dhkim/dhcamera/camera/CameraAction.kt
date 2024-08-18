package com.dhkim.dhcamera.camera

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap

sealed interface CameraAction {

    data class ChangeBackgroundImage(val selectedIndex: Int) : CameraAction
    data object SavingPhoto : CameraAction
    data class SavedPhoto(val savedUrl: String) : CameraAction
    data class TakePhoto(val bitmap: Bitmap, val backgroundBitmap: ImageBitmap) : CameraAction
    data object ResetPhoto : CameraAction
    data class Typing(val text: String): CameraAction
}