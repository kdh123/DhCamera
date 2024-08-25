package com.dhkim.dhcamera.camera.inputText

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhkim.dhcamera.R
import com.dhkim.dhcamera.camera.model.FontAlign
import com.dhkim.dhcamera.camera.model.FontElement
import com.dhkim.dhcamera.camera.model.SelectColorElement
import com.dhkim.dhcamera.camera.model.SelectFontAlignElement
import com.dhkim.dhcamera.camera.model.SelectFontElement
import com.dhkim.dhcamera.camera.navigation.InputTextRoute
import com.dhkim.dhcamera.camera.ui.noRippleClick
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun InputTextScreen(
    currentFontProperties: InputTextRoute?,
    uiState: InputTextUiState,
    onAction: (InputTextAction) -> Unit,
    onBack: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val font = uiState.fonts.firstOrNull { it.isSelected }?.font?.font
    val color = uiState.colors.firstOrNull { it.isSelected }?.color ?: R.color.white
    val alignment = uiState.alignments.firstOrNull { it.isSelected }?.alignment
        .run {
            when (this) {
                FontAlign.Center -> TextAlign.Center
                FontAlign.Left -> TextAlign.Start
                FontAlign.Right -> TextAlign.End
                null -> TextAlign.Center
            }
        }

    LaunchedEffect(Unit) {
        if (currentFontProperties != null && currentFontProperties.id.isNotEmpty()) {
            onAction(InputTextAction.InitTextElement(currentFontProperties))
        } else {
            onAction(InputTextAction.ClearText)
        }
        focusRequester.requestFocus()
    }

    Box(modifier = Modifier.imePadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .background(color = colorResource(id = R.color.deep_gray))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "완료",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterEnd)
                        .noRippleClick {
                            if (currentFontProperties?.id.isNullOrEmpty()) {
                                onAction(InputTextAction.AddText)
                            } else {
                                onAction(
                                    InputTextAction.EditText(
                                        id = currentFontProperties?.id ?: ""
                                    )
                                )
                            }
                            onBack()
                        }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.dp)
                    .weight(1f)
            ) {
                BasicTextField(
                    value = uiState.textFieldValue,
                    onValueChange = {
                        onAction(InputTextAction.Typing(text = it))
                    },
                    textStyle = LocalTextStyle.current.copy(
                        color = colorResource(id = color),
                        fontSize = TextUnit(24f, TextUnitType.Sp),
                        fontFamily = if (font == null) {
                            null
                        } else {
                            FontFamily(font)
                        },
                        textAlign = alignment
                    ),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier
                        .padding(20.dp)
                        .run {
                            if (uiState.textFieldValue.text.isNotEmpty()) {
                                width(IntrinsicSize.Min)
                            } else {
                                width(3.dp)
                            }
                        }
                        .align(Alignment.Center)
                        .focusRequester(focusRequester),
                )
            }

            TextOptions(
                fonts = uiState.fonts,
                colors = uiState.colors,
                alignments = uiState.alignments,
                onFontChanged = {
                    onAction(InputTextAction.ChangeFont(it))
                },
                onColorChanged = {
                    onAction(InputTextAction.ChangeFontColor(it))
                },
                onFontAlignChanged = {
                    onAction(InputTextAction.ChangeFontAlign)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InputTextScreenPreview() {
    val fonts = mutableListOf<FontElement>().apply {
        repeat(10) {
            add(
                FontElement(
                    text = "폰트 $it"
                )
            )
        }
    }.mapIndexed { index, fontElement ->
        SelectFontElement(
            isSelected = index == 0,
            font = fontElement
        )
    }.toImmutableList()

    val colors = listOf(
        R.color.white,
        R.color.black,
        R.color.primary,
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

    InputTextScreen(
        currentFontProperties = null,
        uiState = InputTextUiState(
            fonts = fonts,
            colors = colors
        ),
        onAction = {},
        onBack = {}
    )
}

@Composable
internal fun TextOptions(
    fonts: ImmutableList<SelectFontElement>,
    colors: ImmutableList<SelectColorElement>,
    alignments: ImmutableList<SelectFontAlignElement>,
    onFontChanged: (Int) -> Unit,
    onColorChanged: (Int) -> Unit,
    onFontAlignChanged: () -> Unit
) {
    var currentOptionIndex by remember {
        mutableIntStateOf(0)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        FontsLayout(
            currentOptionIndex = currentOptionIndex,
            fonts = fonts,
            onFontChanged = onFontChanged,
        )

        when (currentOptionIndex) {
            1 -> {
                ColorsLayout(
                    colors = colors,
                    onColorChanged = onColorChanged
                )
            }
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(color = colorResource(id = R.color.black_50))
                .align(Alignment.CenterHorizontally)
        ) {
            TextOption(
                drawableResId = R.drawable.ic_text_field_white,
                isSelected = currentOptionIndex == 0,
                onClick = {
                    currentOptionIndex = 0
                }
            )

            TextOption(
                drawableResId = R.drawable.ic_color_white,
                isSelected = currentOptionIndex == 1,
                onClick = {
                    currentOptionIndex = 1
                }
            )

            val fontAlignDrawable = alignments.firstOrNull { it.isSelected }?.alignment
                .run {
                    when (this) {
                        FontAlign.Center -> R.drawable.ic_align_center_white
                        FontAlign.Left -> R.drawable.ic_align_left_white
                        FontAlign.Right -> R.drawable.ic_align_right_white
                        null -> R.drawable.ic_align_center_white
                    }
                }

            TextOption(
                drawableResId = fontAlignDrawable,
                isSelected = currentOptionIndex == 2,
                onClick = {
                    onFontAlignChanged()
                }
            )
        }
    }
}

@Composable
internal fun ColorsLayout(
    colors: ImmutableList<SelectColorElement>,
    onColorChanged: (Int) -> Unit
) {
    val state = rememberLazyListState()
    val selectedIndex = colors.indexOfFirst { it.isSelected }.apply {
        if (this == -1) {
            plus(1)
        }
    }
    val selectedColor = colors[selectedIndex].color

    LaunchedEffect(colors) {
        state.scrollToItem(selectedIndex)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(color = colorResource(id = selectedColor))
                .border(
                    width = 3.dp,
                    color = colorResource(id = R.color.white),
                    shape = RoundedCornerShape(10.dp)
                )
                .size(48.dp)
        ) {
            Image(
                painter = if (selectedColor == R.color.white) {
                    painterResource(id = R.drawable.ic_colorize_black)
                } else {
                    painterResource(id = R.drawable.ic_colorize_white)
                },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .padding(12.dp)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            state = state
        ) {
            itemsIndexed(items = colors, key = { index, _ ->
                index
            }) { index, colorElement ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(color = colorResource(id = colorElement.color))
                        .border(
                            width = 1.dp,
                            color = colorResource(id = R.color.white),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .size(32.dp)
                        .noRippleClick {
                            onColorChanged(index)
                        }
                )
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
internal fun FontsLayout(
    currentOptionIndex: Int,
    fonts: ImmutableList<SelectFontElement>,
    onFontChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var centerOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    val state = rememberLazyListState()
    var scrollIndex by remember {
        mutableIntStateOf(fonts.indexOfFirst { it.isSelected })
    }

    var firstItemWidth by remember {
        mutableFloatStateOf(0f)
    }
    var lastItemWidth by remember {
        mutableFloatStateOf(0f)
    }
    var parentWidth by remember {
        mutableFloatStateOf(0f)
    }

    val density = LocalDensity.current.density
    val startDpValue = (parentWidth - firstItemWidth).roundToInt() / density
    val endDpValue = ((parentWidth - firstItemWidth) / density).roundToInt()
    val dpValue = 10.dp
    val pxValue = with(LocalDensity.current) { dpValue.toPx() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(fonts) {
        val selectedIndex = fonts.indexOfFirst { it.isSelected }.apply {
            if (this == -1) {
                plus(1)
            }
        }
        state.scrollToItem(selectedIndex)
    }

    LazyRow(
        userScrollEnabled = currentOptionIndex == 0,
        modifier = modifier
            .fillMaxWidth()
            .alpha(
                if (currentOptionIndex == 0) {
                    1f
                } else {
                    0f
                }
            )
            .onGloballyPositioned { layoutCoordinates ->
                if (currentOptionIndex != 0) {
                    return@onGloballyPositioned
                }

                val size = layoutCoordinates.size
                val position = layoutCoordinates.positionInRoot()
                if (parentWidth <= 0) {
                    parentWidth = position.x + size.width / 2f
                }
                centerOffset = Offset(
                    x = position.x + size.width / 2f,
                    y = position.y + size.height / 2f
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        state = state,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = state),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        contentPadding = PaddingValues(
            start = if (startDpValue.dp >= 10.dp) {
                startDpValue.dp - 10.dp
            } else {
                startDpValue.dp
            },
            end = if (endDpValue.dp >= 10.dp) {
                endDpValue.dp - 10.dp
            } else {
                endDpValue.dp
            }
        )
    ) {
        itemsIndexed(items = fonts, key = { index, _ ->
            index
        }) { index, fontElement ->
            Text(
                text = fontElement.font.text,
                color = if (scrollIndex == index) {
                    colorResource(id = R.color.black)
                } else {
                    colorResource(id = R.color.white)
                },
                fontFamily = if (fontElement.font.font == null) {
                    null
                } else {
                    FontFamily(fontElement.font.font!!)
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        color = if (scrollIndex == index) {
                            Color.White
                        } else {
                            colorResource(id = R.color.black_50)
                        }
                    )
                    .padding(10.dp)
                    .clickable {
                        if (currentOptionIndex != 0) {
                            return@clickable
                        }
                        scope.launch {
                            Log.e("index23333", "result : $index")
                            state.animateScrollToItem(
                                index = index,
                                scrollOffset = pxValue.roundToInt()
                            )
                            onFontChanged(index)
                        }
                    }
                    .onGloballyPositioned { layoutCoordinates ->
                        if (currentOptionIndex != 0) {
                            return@onGloballyPositioned
                        }

                        val size = layoutCoordinates.size
                        val position = layoutCoordinates.positionInRoot()
                        if (firstItemWidth <= 0 && index == 0) {
                            firstItemWidth = size.width / 2f
                        }
                        if (lastItemWidth <= 0 && index == fonts.size - 1) {
                            lastItemWidth = size.width / 2f
                        }

                        scrollIndex = if (
                            centerOffset.x >= position.x
                            && centerOffset.x < (position.x + size.width)
                        ) {
                            index
                        } else {
                            scrollIndex
                        }

                        onFontChanged(scrollIndex)
                    }
            )
        }
    }
}

@Composable
internal fun TextOption(
    drawableResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(id = drawableResId),
        contentDescription = null,
        modifier = Modifier
            .padding(3.dp)
            .clip(RoundedCornerShape(10.dp))
            .run {
                if (isSelected) {
                    background(color = colorResource(id = R.color.deep_gray))
                } else {
                    this
                }
            }
            .padding(vertical = 5.dp, horizontal = 10.dp)
            .noRippleClick {
                onClick()
            }
    )
}