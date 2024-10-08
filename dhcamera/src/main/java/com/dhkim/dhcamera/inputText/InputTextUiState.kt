package com.dhkim.dhcamera.inputText

import androidx.compose.runtime.Stable
import com.dhkim.dhcamera.R
import com.dhkim.dhcamera.camera.DhCamera
import com.dhkim.dhcamera.model.FontAlign
import com.dhkim.dhcamera.model.SelectColorElement
import com.dhkim.dhcamera.model.SelectFontAlignElement
import com.dhkim.dhcamera.model.SelectFontElement
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Stable
internal data class InputTextUiState(
    val id: String = "",
    val fonts: ImmutableList<SelectFontElement> = DhCamera.getFontElements()
        .mapIndexed { index, fontElement ->
            SelectFontElement(
                isSelected = index == 0,
                font = fontElement
            )
        }.toImmutableList(),
    val colors: ImmutableList<SelectColorElement> = listOf(
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
        R.color.yellow,
        R.color.pink
    ).mapIndexed { index, fontColor ->
        SelectColorElement(
            isSelected = index == 0,
            color = fontColor
        )
    }.toImmutableList(),
    val alignments: ImmutableList<SelectFontAlignElement> = FontAlign.entries.mapIndexed { index, fontAlign ->
        SelectFontAlignElement(isSelected = index == 0, alignment = fontAlign)
    }.toImmutableList()
)