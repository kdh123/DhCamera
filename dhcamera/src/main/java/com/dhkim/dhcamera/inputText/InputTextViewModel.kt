package com.dhkim.dhcamera.inputText

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim.dhcamera.R
import com.dhkim.dhcamera.model.FontAlign
import com.dhkim.dhcamera.model.SelectColorElement
import com.dhkim.dhcamera.model.SelectFontAlignElement
import com.dhkim.dhcamera.model.SelectFontElement
import com.dhkim.dhcamera.navigation.InputTextRoute
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InputTextViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InputTextUiState())
    internal val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<InputTextSideEffect>()
    internal val sideEffect = _sideEffect.asSharedFlow()

    internal fun onAction(action: InputTextAction) {
        when (action) {
            is InputTextAction.ChangeFont -> {
                changeFont(selectedIndex = action.selectedIndex)
            }

            is InputTextAction.ChangeFontColor -> {
                changeFontColor(selectedIndex = action.selectedIndex)
            }

            InputTextAction.ChangeFontAlign -> {
                changeFontAlign()
            }

            is InputTextAction.AddText -> {
                addText(text = action.text)
            }

            is InputTextAction.EditText -> {
                editText(id = action.id, text = action.text)
            }

            InputTextAction.ClearText -> {
                _uiState.value = InputTextUiState()
            }

            is InputTextAction.InitTextElement -> {
                initTextElement(properties = action.properties)
            }
        }
    }

    private fun changeFont(selectedIndex: Int) {
        val updateFonts =
            _uiState.value.fonts.mapIndexed { index, selectFontElement ->
                selectFontElement.copy(isSelected = index == selectedIndex)
            }.toImmutableList()
        _uiState.value = _uiState.value.copy(fonts = updateFonts)
    }

    private fun changeFontColor(selectedIndex: Int) {
        val updateColors =
            _uiState.value.colors.mapIndexed { index, selectColorElement ->
                selectColorElement.copy(isSelected = index == selectedIndex)
            }.toImmutableList()
        _uiState.value = _uiState.value.copy(colors = updateColors)
    }

    private fun changeFontAlign() {
        val currentSelectedIndex = _uiState.value.alignments
            .indexOfFirst { it.isSelected }
            .apply {
                if (this == -1) {
                    plus(1)
                }
            }
        val updateAlignments =
            _uiState.value.alignments.mapIndexed { index, selectFontAlignElement ->
                selectFontAlignElement.copy(
                    isSelected = index == (currentSelectedIndex + 1) % _uiState.value.alignments.size
                )
            }.toImmutableList()

        _uiState.value =
            _uiState.value.copy(alignments = updateAlignments)
    }

    private fun initTextElement(properties: InputTextRoute) {
        var isFontSelected = false
        val updateFonts = _uiState.value.fonts.mapIndexed { index, element ->
            SelectFontElement(
                isSelected = if (!isFontSelected && element.font.fontId == properties.font) {
                    isFontSelected = true
                    true
                } else {
                    false
                },
                font = _uiState.value.fonts[index].font
            )
        }.toImmutableList()
        val updateColors = _uiState.value.colors.mapIndexed { index, element ->
            SelectColorElement(
                isSelected = element.color == properties.color,
                color = _uiState.value.colors[index].color
            )
        }.toImmutableList()
        val updateAlignments =
            _uiState.value.alignments.mapIndexed { index, element ->
                SelectFontAlignElement(
                    isSelected = element.alignment == properties.alignment,
                    alignment = _uiState.value.alignments[index].alignment
                )
            }.toImmutableList()
        with(properties) {
            _uiState.value = _uiState.value.copy(
                id = id,
                fonts = updateFonts,
                colors = updateColors,
                alignments = updateAlignments
            )
        }
    }

    private fun addText(text: TextFieldValue) {
        viewModelScope.launch {
            with(_uiState.value) {
                val font = fonts.firstOrNull { it.isSelected }?.font
                val color = colors.firstOrNull { it.isSelected }?.color ?: R.color.white
                val alignment = alignments.firstOrNull { it.isSelected }?.alignment ?: FontAlign.Center

                val element = InputTextRoute(
                    id = "${System.currentTimeMillis()}",
                    text = text.text,
                    font = font?.fontId ?: 0,
                    color = color,
                    alignment = alignment
                )

                _sideEffect.emit(InputTextSideEffect.OnCompleted(element))
                _uiState.value = InputTextUiState()
            }
        }
    }

    private fun editText(id: String, text: TextFieldValue) {
        viewModelScope.launch {
            with(_uiState.value) {
                val font = fonts.firstOrNull { it.isSelected }?.font
                val color = colors.firstOrNull { it.isSelected }?.color ?: R.color.white
                val alignment = alignments.firstOrNull { it.isSelected }?.alignment ?: FontAlign.Center

                val element = InputTextRoute(
                    id = id,
                    text = text.text,
                    font = font?.fontId ?: 0,
                    color = color,
                    alignment = alignment
                )

                _sideEffect.emit(InputTextSideEffect.OnCompleted(element))
                _uiState.value = InputTextUiState()
            }
        }
    }
}