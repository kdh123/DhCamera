package com.dhkim.dhcamera.camera

internal sealed interface CameraSideEffect {

    data class Message(val message: String): CameraSideEffect
    data class Completed(val isCompleted: Boolean, val savedUrl: String): CameraSideEffect
}