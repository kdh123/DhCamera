package com.dhkim.dhcamera.camera

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim.dhcamera.R
import com.dhkim.dhcamera.camera.model.Element
import com.dhkim.dhcamera.camera.model.FontAlign
import com.dhkim.dhcamera.camera.model.SelectColorElement
import com.dhkim.dhcamera.camera.model.SelectFontAlignElement
import com.dhkim.dhcamera.camera.model.SelectFontElement
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    internal val uiState = _uiState.asStateFlow()

    private val _inputTextUiState = MutableStateFlow(InputTextUiState())
    internal val inputTextUiState = _inputTextUiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<CameraSideEffect>()
    internal val sideEffect = _sideEffect.asSharedFlow()

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
                _inputTextUiState.value = _inputTextUiState.value.copy(text = action.text)
            }

            is CameraAction.ChangeFont -> {
                val updateFonts =
                    _inputTextUiState.value.fonts.mapIndexed { index, selectFontElement ->
                        selectFontElement.copy(isSelected = index == action.selectedIndex)
                    }.toImmutableList()
                _inputTextUiState.value = _inputTextUiState.value.copy(fonts = updateFonts)
            }

            is CameraAction.ChangeFontColor -> {
                val updateColors =
                    _inputTextUiState.value.colors.mapIndexed { index, selectColorElement ->
                        selectColorElement.copy(isSelected = index == action.selectedIndex)
                    }.toImmutableList()
                _inputTextUiState.value = _inputTextUiState.value.copy(colors = updateColors)
            }

            CameraAction.ChangeFontAlign -> {
                val currentSelectedIndex =
                    _inputTextUiState.value.alignments.indexOfFirst { it.isSelected }.apply {
                        if (this == -1) {
                            plus(1)
                        }
                    }
                val updateAlignments =
                    _inputTextUiState.value.alignments.mapIndexed { index, selectFontAlignElement ->
                        selectFontAlignElement.copy(
                            isSelected = index == (currentSelectedIndex + 1) % _inputTextUiState.value.alignments.size
                        )
                    }.toImmutableList()

                _inputTextUiState.value =
                    _inputTextUiState.value.copy(alignments = updateAlignments)
            }

            CameraAction.AddText -> {
                addText()
            }

            is CameraAction.ChangeElementProperties -> {
                val index = _uiState.value.elements.indexOfFirst { it._id == action.id }
                if (index < 0) {
                    return
                }

                val element = _uiState.value.elements[index]

                when (element) {
                    is Element.Text -> {
                        element.apply {
                            prevScale = action.prevScale
                            _prevScale = prevScale

                            scale = action.scale
                            _scale = scale

                            rotation = action.rotation
                            _rotation = rotation

                            centerOffset = action.centerOffset
                            _centerOffset = centerOffset

                            offset = action.offset
                            _offset = offset
                        }
                    }

                    is Element.Image -> {
                        element.apply {
                            prevScale = action.prevScale
                            _prevScale = prevScale

                            scale = action.scale
                            _scale = scale

                            rotation = action.rotation
                            _rotation = rotation

                            centerOffset = action.centerOffset
                            _centerOffset = centerOffset

                            offset = action.offset
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

            CameraAction.ClearText -> {
                _inputTextUiState.value = InputTextUiState()
            }

            is CameraAction.DeleteElement -> {
                val updateElements = _uiState.value.elements.filter { it._id != action.id }
                _uiState.value = _uiState.value.copy(elements = updateElements)
            }

            is CameraAction.InitTextElement -> {
                var isFontSelected = false
                val updateFonts = _inputTextUiState.value.fonts.mapIndexed { index, element ->
                    SelectFontElement(
                        isSelected = if (!isFontSelected && element.font.fontId == action.properties.font) {
                            isFontSelected = true
                            true
                        } else {
                            false
                        },
                        font = _inputTextUiState.value.fonts[index].font
                    )
                }.toImmutableList()
                val updateColors = _inputTextUiState.value.colors.mapIndexed { index, element ->
                    SelectColorElement(
                        isSelected = element.color == action.properties.color,
                        color = _inputTextUiState.value.colors[index].color
                    )
                }.toImmutableList()
                val updateAlignments =
                    _inputTextUiState.value.alignments.mapIndexed { index, element ->
                        SelectFontAlignElement(
                            isSelected = element.alignment == action.properties.alignment,
                            alignment = _inputTextUiState.value.alignments[index].alignment
                        )
                    }.toImmutableList()
                with(action.properties) {
                    _inputTextUiState.value = _inputTextUiState.value.copy(
                        id = id,
                        text = text,
                        fonts = updateFonts,
                        colors = updateColors,
                        alignments = updateAlignments
                    )
                }
            }

            is CameraAction.EditText -> {
                editText(id = action.id)
            }

            is CameraAction.AddImage -> {
                addImage(imageUri = action.imageUri)
            }
        }
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

    private fun addText() {
        with(_inputTextUiState.value) {
            val text = text
            val font = fonts.firstOrNull { it.isSelected }?.font
            val color = colors.firstOrNull { it.isSelected }?.color ?: R.color.white
            val alignment = alignments.firstOrNull { it.isSelected }?.alignment ?: FontAlign.Center

            val updateElements = _uiState.value.elements.toMutableList()
                .apply {
                    val element = Element.Text(
                        text = text,
                        fontId = font?.fontId ?: 0,
                        font = font?.font,
                        color = color,
                        alignment = alignment
                    )
                    add(element)
                }
            _uiState.value = _uiState.value.copy(elements = updateElements)
            _inputTextUiState.value = InputTextUiState()
        }
    }

    private fun editText(id: String) {
        val currentElements = _uiState.value.elements
        val index = currentElements.indexOfFirst { it._id == id }
        if (index < 0) {
            return
        }
        with(_inputTextUiState.value) {
            val text = text
            val font = fonts.firstOrNull { it.isSelected }?.font
            val color = colors.firstOrNull { it.isSelected }?.color ?: R.color.white
            val alignment = alignments.firstOrNull { it.isSelected }?.alignment ?: FontAlign.Center
            val updateElements = currentElements.toMutableList()
                .apply {
                    val element = (get(index) as Element.Text).copy(
                        text = text,
                        fontId = font?.fontId ?: 0,
                        font = font?.font,
                        color = color,
                        alignment = alignment
                    )
                    set(index, element)
                }
            _uiState.value = _uiState.value.copy(elements = updateElements)
            _inputTextUiState.value = InputTextUiState()
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