package com.dhkim.dhcamera.camera

import android.graphics.Bitmap
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.ImageBitmap

@Stable
internal data class CameraUiState(
    val isLoading: Boolean = false,
    val bitmap: Bitmap? = null,
    val backgroundBitmap: ImageBitmap? = null,
    val savedUrl: String = "",
    val currentBackgroundImageIndex: Int = 0,
    val backgroundItems: List<BackgroundItem> = if (DhCamera.getBackgroundItems().isEmpty()) {
        listOf()
    } else {
        listOf(
            BackgroundItem.BackgroundImageItem(
                imageUrl = "",
                propIsSelected = true
            )
        ) + DhCamera.getBackgroundItems()
    }
)