package com.dhkim.dhcamera.inputText

import androidx.compose.ui.text.input.TextFieldValue
import com.dhkim.dhcamera.navigation.InputTextRoute

internal sealed interface InputTextAction {

    data class ChangeFont(val selectedIndex: Int) : InputTextAction
    data class ChangeFontColor(val selectedIndex: Int) : InputTextAction
    data object ChangeFontAlign : InputTextAction
    data class AddText(val text: TextFieldValue) : InputTextAction
    data class EditText(val id: String, val text: TextFieldValue) : InputTextAction
    data object ClearText : InputTextAction
    data class InitTextElement(val properties: InputTextRoute) : InputTextAction
}