package com.dhkim.dhcamera.camera

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim.dhcamera.R
import com.dhkim.dhcamera.camera.inputText.InputTextAction
import com.dhkim.dhcamera.camera.inputText.InputTextUiState
import com.dhkim.dhcamera.camera.model.Element
import com.dhkim.dhcamera.camera.model.FontAlign
import com.dhkim.dhcamera.camera.model.SelectColorElement
import com.dhkim.dhcamera.camera.model.SelectFontAlignElement
import com.dhkim.dhcamera.camera.model.SelectFontElement
import com.dhkim.dhcamera.camera.navigation.InputTextRoute
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CameraViewModel : ViewModel() {

    private val _cameraUiState = MutableStateFlow(CameraUiState())
    internal val cameraUiState = _cameraUiState.asStateFlow()

    private val _inputTextUiState = MutableStateFlow(InputTextUiState())
    internal val inputTextUiState = _inputTextUiState.asStateFlow()

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
        }
    }

    internal fun onInputTextAction(action: InputTextAction) {
        when (action) {
            is InputTextAction.Typing -> {
                _inputTextUiState.value = _inputTextUiState.value.copy(text = action.text)
            }

            is InputTextAction.ChangeFont -> {
                changeFont(selectedIndex = action.selectedIndex)
            }

            is InputTextAction.ChangeFontColor -> {
                changeFontColor(selectedIndex = action.selectedIndex)
            }

            InputTextAction.ChangeFontAlign -> {
                changeFontAlign()
            }

            InputTextAction.AddText -> {
                addText()
            }

            InputTextAction.ClearText -> {
                _inputTextUiState.value = InputTextUiState()
            }

            is InputTextAction.InitTextElement -> {
                initTextElement(properties = action.properties)
            }

            is InputTextAction.EditText -> {
                editText(id = action.id)
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
        val index = _cameraUiState.value.elements.indexOfFirst { it._id == id }
        if (index < 0) {
            return
        }

        val element = _cameraUiState.value.elements[index]

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

        val updateElements = _cameraUiState.value.elements.toMutableList().apply {
            set(index, element)
        }

        _cameraUiState.update {
            cameraUiState.value.copy(elements = updateElements)
        }
    }

    private fun deleteElement(id: String) {
        val updateElements = _cameraUiState.value.elements.filter { it._id != id }
        _cameraUiState.value = _cameraUiState.value.copy(elements = updateElements)
    }

    private fun changeFont(selectedIndex: Int) {
        val updateFonts =
            _inputTextUiState.value.fonts.mapIndexed { index, selectFontElement ->
                selectFontElement.copy(isSelected = index == selectedIndex)
            }.toImmutableList()
        _inputTextUiState.value = _inputTextUiState.value.copy(fonts = updateFonts)
    }

    private fun changeFontColor(selectedIndex: Int) {
        val updateColors =
            _inputTextUiState.value.colors.mapIndexed { index, selectColorElement ->
                selectColorElement.copy(isSelected = index == selectedIndex)
            }.toImmutableList()
        _inputTextUiState.value = _inputTextUiState.value.copy(colors = updateColors)
    }

    private fun changeFontAlign() {
        val currentSelectedIndex = _inputTextUiState.value.alignments
            .indexOfFirst { it.isSelected }
            .apply {
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

    private fun initTextElement(properties: InputTextRoute) {
        var isFontSelected = false
        val updateFonts = _inputTextUiState.value.fonts.mapIndexed { index, element ->
            SelectFontElement(
                isSelected = if (!isFontSelected && element.font.fontId == properties.font) {
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
                isSelected = element.color == properties.color,
                color = _inputTextUiState.value.colors[index].color
            )
        }.toImmutableList()
        val updateAlignments =
            _inputTextUiState.value.alignments.mapIndexed { index, element ->
                SelectFontAlignElement(
                    isSelected = element.alignment == properties.alignment,
                    alignment = _inputTextUiState.value.alignments[index].alignment
                )
            }.toImmutableList()
        with(properties) {
            _inputTextUiState.value = _inputTextUiState.value.copy(
                id = id,
                text = text,
                fonts = updateFonts,
                colors = updateColors,
                alignments = updateAlignments
            )
        }
    }

    private fun addImage(imageUri: String) {
        val updateElements = _cameraUiState.value.elements.toMutableList()
            .apply {
                val element = Element.Image(
                    imageUri = imageUri
                )
                add(element)
            }
        _cameraUiState.value = _cameraUiState.value.copy(elements = updateElements)
    }

    private fun addText() {
        with(_inputTextUiState.value) {
            val text = text
            val font = fonts.firstOrNull { it.isSelected }?.font
            val color = colors.firstOrNull { it.isSelected }?.color ?: R.color.white
            val alignment = alignments.firstOrNull { it.isSelected }?.alignment ?: FontAlign.Center

            val updateElements = _cameraUiState.value.elements.toMutableList()
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
            _cameraUiState.value = _cameraUiState.value.copy(elements = updateElements)
            _inputTextUiState.value = InputTextUiState()
        }
    }

    private fun editText(id: String) {
        val currentElements = _cameraUiState.value.elements
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
            _cameraUiState.value = _cameraUiState.value.copy(elements = updateElements)
            _inputTextUiState.value = InputTextUiState()
        }
    }

    private fun onChangeBackgroundImage(selectedIndex: Int) {
        val currentBackgroundImages =
            _cameraUiState.value.backgroundItems.mapIndexed { index, backgroundItem ->
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
        _cameraUiState.value = _cameraUiState.value.copy(
            currentBackgroundImageIndex = selectedIndex,
            backgroundItems = currentBackgroundImages
        )
    }

    private fun onResetPhoto() {
        _cameraUiState.value = _cameraUiState.value.copy(bitmap = null, backgroundBitmap = null)
    }

    private fun onTakePhoto(bitmap: Bitmap, backgroundBitmap: ImageBitmap) {
        _cameraUiState.value = _cameraUiState.value.copy(bitmap = bitmap, backgroundBitmap = backgroundBitmap)
    }

    private fun onSavingPhoto() {
        _cameraUiState.value = _cameraUiState.value.copy(isLoading = true)
    }

    private fun onSavedPhoto(savedUrl: String) {
        viewModelScope.launch {
            _cameraUiState.value = _cameraUiState.value.copy(
                isLoading = false,
                bitmap = null,
                backgroundBitmap = null,
                savedUrl = savedUrl
            )
            _sideEffect.emit(CameraSideEffect.Completed(isCompleted = true, savedUrl = savedUrl))
        }
    }
}