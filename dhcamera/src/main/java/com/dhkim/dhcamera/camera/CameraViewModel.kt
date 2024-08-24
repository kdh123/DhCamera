package com.dhkim.dhcamera.camera

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim.dhcamera.R
import com.dhkim.dhcamera.camera.model.Element
import com.dhkim.dhcamera.camera.model.FontAlign
import com.dhkim.dhcamera.camera.model.SelectColorElement
import com.dhkim.dhcamera.camera.model.SelectFontAlignElement
import com.dhkim.dhcamera.camera.model.SelectFontElement
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    internal val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<CameraSideEffect>()
    internal val sideEffect = _sideEffect.asSharedFlow()

    internal var fontElements: ImmutableList<SelectFontElement> by mutableStateOf(
        DhCamera.getFontElements().mapIndexed { index, fontElement ->
            SelectFontElement(
                isSelected = index == 0,
                font = fontElement
            )
        }.toImmutableList()
    )
    internal var colorElements: ImmutableList<SelectColorElement> by mutableStateOf(
        listOf(
            R.color.white,
            R.color.black,
            R.color.blue,
            R.color.purple_200,
            R.color.teal_200,
            R.color.teal_700,
            R.color.purple_500,
            R.color.red,
            R.color.orange,
            R.color.sky_blue,
            R.color.yellow
        ).mapIndexed { index, color ->
            SelectColorElement(
                isSelected = index == 0,
                color = color
            )
        }.toImmutableList()
    )
    internal var fontAlignElements: ImmutableList<SelectFontAlignElement> by mutableStateOf(
        FontAlign.entries.mapIndexed { index, fontAlign ->
            SelectFontAlignElement(isSelected = index == 0, alignment = fontAlign)
        }.toImmutableList()
    )

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

            is CameraAction.ChangeFont -> {
                fontElements = fontElements.mapIndexed { index, selectFontElement ->
                    selectFontElement.copy(isSelected = index == action.selectedIndex)
                }.toImmutableList()
            }

            is CameraAction.ChangeFontColor -> {
                colorElements = colorElements.mapIndexed { index, selectColorElement ->
                    selectColorElement.copy(isSelected = index == action.selectedIndex)
                }.toImmutableList()
            }

            CameraAction.ChangeFontAlign -> {
                val currentSelectedIndex = fontAlignElements.indexOfFirst { it.isSelected }.apply {
                    if (this == -1) {
                        plus(1)
                    }
                }
                fontAlignElements = fontAlignElements.mapIndexed { index, selectFontAlignElement ->
                    selectFontAlignElement.copy(
                        isSelected = index == (currentSelectedIndex + 1) % fontAlignElements.size
                    )
                }.toImmutableList()
            }

            CameraAction.AddText -> {
                val text = _uiState.value.currentText
                val font = fontElements.firstOrNull { it.isSelected }?.font?.font
                val color = colorElements.firstOrNull { it.isSelected }?.color ?: R.color.white
                val alignment = fontAlignElements.firstOrNull { it.isSelected }?.alignment ?: FontAlign.Center

                val updateElements = _uiState.value.elements.toMutableList()
                    .apply {
                        val element = Element.Text(
                            text = text,
                            font = font,
                            color = color,
                            alignment = alignment
                        )
                        add(element)
                    }
                _uiState.value = _uiState.value.copy(elements = updateElements)
            }

            is CameraAction.ChangeElementProperties -> {
                val index = _uiState.value.elements.indexOfFirst { it._id == action.id }
                if (index < 0) {
                    return
                }

                val element = _uiState.value.elements[index]
                element.run {
                    _prevScale *= action.scale
                    _scale *= action.scale
                    _rotation += action.rotation
                    _centerOffset = action.centerOffset
                    _offset = action.offset
                }
                val updateElements = _uiState.value.elements.toMutableList().apply {
                    set(index, element)
                }
                _uiState.value = _uiState.value.copy(elements = updateElements)
            }

            CameraAction.ClearText -> {
                _uiState.value = _uiState.value.copy(currentText = "")
            }

            is CameraAction.DeleteElement -> {
                val updateElements = _uiState.value.elements.filter { it._id != action.id }
                _uiState.value = _uiState.value.copy(elements = updateElements)
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