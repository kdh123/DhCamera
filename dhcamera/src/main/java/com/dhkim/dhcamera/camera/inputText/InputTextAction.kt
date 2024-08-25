package com.dhkim.dhcamera.camera.inputText

import com.dhkim.dhcamera.camera.navigation.InputTextRoute

sealed interface InputTextAction {

    data class ChangeFont(val selectedIndex: Int) : InputTextAction
    data class ChangeFontColor(val selectedIndex: Int) : InputTextAction
    data object ChangeFontAlign : InputTextAction
    data object AddText : InputTextAction
    data class EditText(val id: String) : InputTextAction
    data class Typing(val text: String) : InputTextAction
    data object ClearText : InputTextAction
    data class InitTextElement(val properties: InputTextRoute) : InputTextAction
}