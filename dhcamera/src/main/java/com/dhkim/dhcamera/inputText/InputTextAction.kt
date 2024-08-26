package com.dhkim.dhcamera.inputText

import androidx.compose.ui.text.input.TextFieldValue
import com.dhkim.dhcamera.navigation.InputTextRoute

sealed interface InputTextAction {

    data class ChangeFont(val selectedIndex: Int) : InputTextAction
    data class ChangeFontColor(val selectedIndex: Int) : InputTextAction
    data object ChangeFontAlign : InputTextAction
    data object AddText : InputTextAction
    data class EditText(val id: String) : InputTextAction
    data class Typing(val text: TextFieldValue) : InputTextAction
    data object ClearText : InputTextAction
    data class InitTextElement(val properties: InputTextRoute) : InputTextAction
}