package com.dhkim.dhcamera.camera

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim.dhcamera.model.BackgroundItem
import com.dhkim.dhcamera.model.Element
import com.dhkim.dhcamera.navigation.InputTextRoute
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    internal val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<CameraSideEffect>()
    internal val sideEffect = _sideEffect.asSharedFlow()

    internal fun onCameraAction(action: CameraAction) {
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

            is CameraAction.ChangeElementProperties -> {
                with(action) {
                    changeElementProperties(id, prevScale, scale, rotation, centerOffset, offset)
                }
            }

            is CameraAction.DeleteElement -> {
                deleteElement(id = action.id)
            }

            is CameraAction.AddImage -> {
                addImage(imageUri = action.imageUri)
            }

            is CameraAction.AddText -> {
                val isEdit = _uiState.value.elements.firstOrNull { it._id == action.inputText.id } != null

                if (isEdit) {
                    editText(inputText = action.inputText)
                } else {
                    addText(inputText = action.inputText)
                }
            }
        }
    }

    private fun changeElementProperties(
        id: String,
        prevScale: Float,
        scale: Float,
        rotation: Float,
        centerOffset: Offset,
        offset: Offset
    ) {
        val index = _uiState.value.elements.indexOfFirst { it._id == id }
        if (index < 0) {
            return
        }

        val element = _uiState.value.elements[index]

        when (element) {
            is Element.Text -> {
                element.apply {
                    this.prevScale = prevScale
                    _prevScale = prevScale

                    this.scale = scale
                    _scale = scale

                    this.rotation = rotation
                    _rotation = rotation

                    this.centerOffset = centerOffset
                    _centerOffset = centerOffset

                    this.offset = offset
                    _offset = offset
                }
            }

            is Element.Image -> {
                element.apply {
                    this.prevScale = prevScale
                    _prevScale = prevScale

                    this.scale = scale
                    _scale = scale

                    this.rotation = rotation
                    _rotation = rotation

                    this.centerOffset = centerOffset
                    _centerOffset = centerOffset

                    this.offset = offset
                    _offset = offset
                }
            }
        }

        val updateElements = _uiState.value.elements.toMutableList().apply {
            set(index, element)
        }

        _uiState.update {
            uiState.value.copy(elements = updateElements)
        }
    }

    private fun deleteElement(id: String) {
        val updateElements = _uiState.value.elements.filter { it._id != id }
        _uiState.value = _uiState.value.copy(elements = updateElements)
    }

    private fun addImage(imageUri: String) {
        val updateElements = _uiState.value.elements.toMutableList()
            .apply {
                val element = Element.Image(
                    imageUri = imageUri
                )
                add(element)
            }
        _uiState.value = _uiState.value.copy(elements = updateElements)
    }

    private fun addText(inputText: InputTextRoute) {
        val element = with(inputText) {
            Element.Text(
                id = id,
                text = text,
                fontId = font,
                color = color,
                alignment = alignment
            )
        }

        val updateElements = _uiState.value.elements.toMutableList().apply {
            add(element)
        }
        _uiState.value = _uiState.value.copy(elements = updateElements)
    }

    private fun editText(inputText: InputTextRoute) {
        val currentElements = _uiState.value.elements
        val index = currentElements.indexOfFirst { it._id == inputText.id }
        val updateElements = currentElements.toMutableList()
            .apply {
                val element = (get(index) as Element.Text).copy(
                    text = inputText.text,
                    fontId = inputText.font,
                    color = inputText.color,
                    alignment = inputText.alignment
                )
                set(index, element)
            }

        _uiState.value = _uiState.value.copy(elements = updateElements)
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
        _uiState.value =
            _uiState.value.copy(bitmap = null, backgroundBitmap = null, elements = listOf())
    }

    private fun onTakePhoto(bitmap: Bitmap, backgroundBitmap: ImageBitmap) {
        _uiState.value =
            _uiState.value.copy(bitmap = bitmap, backgroundBitmap = backgroundBitmap)
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