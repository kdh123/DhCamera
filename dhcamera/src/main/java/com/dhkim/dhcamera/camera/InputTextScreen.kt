package com.dhkim.dhcamera.camera

import android.graphics.Typeface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.dhkim.dhcamera.R
import com.dhkim.dhcamera.camera.model.FontItem
import com.dhkim.dhcamera.camera.ui.noRippleClick
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun InputTextScreen(
    uiState: CameraUiState,
    onAction: (CameraAction) -> Unit,
    fonts: ImmutableList<FontItem>,
    onBack: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var font: Typeface? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(Unit) {
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
                    .height(0.dp)
                    .weight(1f)
            ) {
                BasicTextField(
                    value = uiState.currentText,
                    onValueChange = {
                        onAction(CameraAction.Typing(text = it))
                    },
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontSize = TextUnit(24f, TextUnitType.Sp),
                        fontFamily = if (font == null) {
                            null
                        } else {
                            FontFamily(font!!)
                        }
                    ),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier
                        .padding(20.dp)
                        .run {
                            if (uiState.currentText.isNotEmpty()) {
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
                fonts = fonts,
                onFontChanged = {
                    font = it
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InputTextScreenPreview() {
    val fonts = mutableListOf<FontItem>().apply {
        repeat(10) {
            add(
                FontItem(
                    text = "폰트 $it"
                )
            )
        }
    }.toImmutableList()

    InputTextScreen(
        uiState = CameraUiState(),
        onAction = {},
        fonts = fonts,
        onBack = {}
    )
}

@Composable
internal fun TextOptions(
    fonts: ImmutableList<FontItem>,
    onFontChanged: (Typeface) -> Unit
) {
    var currentOptionIndex by remember {
        mutableIntStateOf(0)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        when (currentOptionIndex) {
            0 -> {
                FontsLayout(
                    fonts = fonts,
                    onFontChanged = onFontChanged
                )
            }

            1 -> {

            }

            2 -> {

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

            TextOption(
                drawableResId = R.drawable.ic_align_center_white,
                isSelected = currentOptionIndex == 2,
                onClick = {
                    currentOptionIndex = 2
                }
            )
        }
    }
}

@Composable
internal fun FontsLayout(
    fonts: ImmutableList<FontItem>,
    onFontChanged: (Typeface) -> Unit
) {
    var centerOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    val state = rememberLazyListState()
    var selectedIndex by remember {
        mutableIntStateOf(0)
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

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { layoutCoordinates ->
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
        }) { index, font ->
            Text(
                text = font.text,
                color = if (selectedIndex == index) {
                    colorResource(id = R.color.black)
                } else {
                    colorResource(id = R.color.white)
                },
                fontFamily = if (font.font == null) {
                    null
                } else {
                    FontFamily(font.font!!)
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        color = if (selectedIndex == index) {
                            Color.White
                        } else {
                            colorResource(id = R.color.black_50)
                        }
                    )
                    .padding(10.dp)
                    .clickable {
                        scope.launch {
                            state.animateScrollToItem(
                                index = index,
                                scrollOffset = pxValue.roundToInt()
                            )
                            onFontChanged(fonts[selectedIndex].font!!)
                        }
                    }
                    .onGloballyPositioned { layoutCoordinates ->
                        val size = layoutCoordinates.size
                        val position = layoutCoordinates.positionInRoot()
                        if (firstItemWidth <= 0 && index == 0) {
                            firstItemWidth = size.width / 2f
                        }
                        if (lastItemWidth <= 0 && index == fonts.size - 1) {
                            lastItemWidth = size.width / 2f
                        }

                        selectedIndex = if (
                            centerOffset.x >= position.x
                            && centerOffset.x < (position.x + size.width)
                        ) {
                            index
                        } else {
                            selectedIndex
                        }

                        onFontChanged(fonts[selectedIndex].font!!)
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