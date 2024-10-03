package com.dhkim.dhcamera.inputText

import com.dhkim.dhcamera.navigation.InputTextRoute

internal sealed interface InputTextSideEffect {

    data class OnCompleted(val inputText: InputTextRoute): InputTextSideEffect
}