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

    internal fun onAction(action: CameraAction) {
        when (action) {
            is CameraAction.ChangeBackgroundImage -> {
                onChangeBackgroundImage(selectedIndex = action.selectedIndex)
            }

            CameraAction.ResetPhoto -> {
                onResetPhoto()
            }

            is CameraAction.SavedPhoto -> {
                onSavedPhoto(savedUrl = action.savedUrl)
            }

            CameraAction.SavingPhoto -> {
                onSavingPhoto()
            }

            is CameraAction.TakePhoto -> {
                onTakePhoto(bitmap = action.bitmap, backgroundBitmap = action.backgroundBitmap)
            }

            is CameraAction.Typing -> {
                _uiState.value = _uiState.value.copy(currentText = action.text)
            }
        }
    }

    private fun onChangeBackgroundImage(selectedIndex: Int) {
        val currentBackgroundImages =
            _uiState.value.backgroundItems.mapIndexed { index, backgroundItem ->
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
        _uiState.value = _uiState.value.copy(
            currentBackgroundImageIndex = selectedIndex,
            backgroundItems = currentBackgroundImages
        )
    }

    private fun onResetPhoto() {
        _uiState.value = _uiState.value.copy(bitmap = null, backgroundBitmap = null)
    }

    private fun onTakePhoto(bitmap: Bitmap, backgroundBitmap: ImageBitmap) {
        _uiState.value = _uiState.value.copy(bitmap = bitmap, backgroundBitmap = backgroundBitmap)
    }

    private fun onSavingPhoto() {
        _uiState.value = _uiState.value.copy(isLoading = true)
    }

    private fun onSavedPhoto(savedUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                bitmap = null,
                backgroundBitmap = null,
                savedUrl = savedUrl
            )
            _sideEffect.emit(CameraSideEffect.Completed(isCompleted = true, savedUrl = savedUrl))
        }
    }
}