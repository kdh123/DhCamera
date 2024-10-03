package com.dhkim.dhcamera.camera

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import com.dhkim.dhcamera.navigation.InputTextRoute

internal sealed interface CameraAction {

    data class ChangeBackgroundImage(val selectedIndex: Int) : CameraAction
    data object SavingPhoto : CameraAction
    data class SavedPhoto(val savedUrl: String) : CameraAction
    data class TakePhoto(val bitmap: Bitmap, val backgroundBitmap: ImageBitmap) : CameraAction
    data object ResetPhoto : CameraAction
    data class AddImage(val imageUri: String) : CameraAction
    data class AddText(val inputText: InputTextRoute) : CameraAction
    data class DeleteElement(val id: String) : CameraAction
    data class ChangeElementProperties(
        val id: String,
        val prevScale: Float,
        val scale: Float,
        val rotation: Float,
        val centerOffset: Offset,
        val offset: Offset
    ) : CameraAction
}