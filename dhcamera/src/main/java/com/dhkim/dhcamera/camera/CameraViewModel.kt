package com.dhkim.dhcamera.camera

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<CameraSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    fun onChangeBackgroundImage(selectedIndex: Int) {
        val currentBackgroundImages = _uiState.value.backgroundItems.mapIndexed { index, backgroundItem ->
            when (backgroundItem) {
                is BackgroundItem.BackgroundImageItem -> {
                    if (selectedIndex == index) {
                        backgroundItem.copy(propIsSelected = true)
                    } else {
                        backgroundItem.copy(propIsSelected = false)
                    }
                }

                is BackgroundItem.BackgroundTextItem -> {
                    if (selectedIndex == index) {
                        backgroundItem.copy(propIsSelected = true)
                    } else {
                        backgroundItem.copy(propIsSelected = false)
                    }
                }
            }
        }
        _uiState.value = _uiState.value.copy(currentBackgroundImageIndex = selectedIndex, backgroundItems = currentBackgroundImages)
    }

    fun onResetPhoto() {
        _uiState.value = _uiState.value.copy(bitmap = null, backgroundBitmap = null)
    }

    fun onTakePhoto(bitmap: Bitmap, backgroundBitmap: ImageBitmap) {
        _uiState.value = _uiState.value.copy(bitmap = bitmap, backgroundBitmap = backgroundBitmap)
    }

    fun onSavingPhoto() {
        _uiState.value = _uiState.value.copy(isLoading = true)
    }

    fun onSavedPhoto(savedUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = false, bitmap = null, backgroundBitmap = null, savedUrl = savedUrl)
            _sideEffect.emit(CameraSideEffect.Completed(isCompleted = true, savedUrl = savedUrl))
        }
    }
}