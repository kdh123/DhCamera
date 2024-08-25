package com.dhkim.dhcamera.camera

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import com.dhkim.dhcamera.camera.navigation.InputTextRoute

sealed interface CameraAction {

    data class ChangeBackgroundImage(val selectedIndex: Int) : CameraAction
    data object SavingPhoto : CameraAction
    data class SavedPhoto(val savedUrl: String) : CameraAction
    data class TakePhoto(val bitmap: Bitmap, val backgroundBitmap: ImageBitmap) : CameraAction
    data object ResetPhoto : CameraAction
    data class Typing(val text: String) : CameraAction

    data class ChangeFont(val selectedIndex: Int) : CameraAction
    data class ChangeFontColor(val selectedIndex: Int) : CameraAction
    data object ChangeFontAlign : CameraAction
    data object AddText : CameraAction
    data class EditText(val id: String) : CameraAction
    data class AddImage(val imageUri: String) : CameraAction
    data class DeleteElement(val id: String) : CameraAction
    data object ClearText: CameraAction
    data class InitTextElement(val properties: InputTextRoute) : CameraAction

    data class ChangeElementProperties(
        val id: String,
        val prevScale: Float,
        val scale: Float,
        val rotation: Float,
        val centerOffset: Offset,
        val offset: Offset
    ) : CameraAction
}