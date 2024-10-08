package com.dhkim.dhcamera.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

internal fun Modifier.noRippleClick(
    onClick: () -> Unit
) = composed {
    val interactionSource = remember { MutableInteractionSource() }

    Modifier.clickable(
        interactionSource = interactionSource,
        indication = null
    ) {
        onClick()
    }
}