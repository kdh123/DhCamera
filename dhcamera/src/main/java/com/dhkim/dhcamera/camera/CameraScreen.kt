package com.dhkim.dhcamera.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.dhkim.dhcamera.R
import com.dhkim.dhcamera.camera.DhCamera.BOTTOM_CENTER
import com.dhkim.dhcamera.camera.DhCamera.BOTTOM_END
import com.dhkim.dhcamera.camera.DhCamera.BOTTOM_START
import com.dhkim.dhcamera.camera.DhCamera.CENTER
import com.dhkim.dhcamera.camera.DhCamera.CENTER_END
import com.dhkim.dhcamera.camera.DhCamera.CENTER_START
import com.dhkim.dhcamera.camera.DhCamera.TEXT_CENTER
import com.dhkim.dhcamera.camera.DhCamera.TEXT_END
import com.dhkim.dhcamera.camera.DhCamera.TEXT_START
import com.dhkim.dhcamera.camera.DhCamera.TOP_CENTER
import com.dhkim.dhcamera.camera.DhCamera.TOP_END
import com.dhkim.dhcamera.camera.DhCamera.TOP_START
import com.dhkim.dhcamera.model.BackgroundItem
import com.dhkim.dhcamera.model.Element
import com.dhkim.dhcamera.model.FontAlign
import com.dhkim.dhcamera.navigation.InputTextRoute
import com.dhkim.dhcamera.ui.noRippleClick
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

internal typealias SavedUrl = String
internal typealias Permission = String

@SuppressLint("MissingPermission", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun CameraScreen(
    inputText: InputTextRoute?,
    uiState: CameraUiState,
    sideEffect: SharedFlow<CameraSideEffect>,
    onAction: (CameraAction) -> Unit,
    onNext: (SavedUrl) -> Unit,
    onPermissionDenied: (Permission) -> Unit,
    onNavigateToInputText: (InputTextRoute?) -> Unit,
    onBack: () -> Unit,
    onInitInputText: () -> Unit,
) {
    var showImageBottomSheet by remember {
        mutableStateOf(false)
    }
    val isPhotoTaken = uiState.bitmap != null && uiState.backgroundBitmap != null
    val backgroundItems = uiState.backgroundItems
    val context = LocalContext.current
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or
                        CameraController.VIDEO_CAPTURE
            )
        }
    }
    val graphicsLayer = rememberGraphicsLayer()
    val resultGraphicsLayer = rememberGraphicsLayer()
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { state ->
        state.keys.forEach {
            if (state[it] == false) {
                onPermissionDenied(it)
            }
        }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            onAction(CameraAction.AddImage(imageUri = "$it"))
        }
    }
    val imagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { state ->
        val isGranted = state.keys.count { state[it] == false } == 0
        if (isGranted) {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        } else {
            Toast.makeText(context, context.getString(R.string.allow_storage_permission), Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(inputText) {
        if (inputText != null) {
            onAction(CameraAction.AddText(inputText))
        }
    }

    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(
                    Manifest.permission.CAMERA
                )
            } else {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        )
    }

    LaunchedEffect(Unit) {
        sideEffect.collectLatest {
            when (it) {
                is CameraSideEffect.Completed -> {
                    onNext(it.savedUrl)
                }

                is CameraSideEffect.Message -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    BackHandler {
        if (isPhotoTaken) {
            onAction(CameraAction.ResetPhoto)
        } else {
            (context as? Activity)?.finish()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.dp)
                    .weight(1f)
            ) {
                if (isPhotoTaken) {
                    AfterTakePhotoLayout(
                        uiState = uiState,
                        onAction = onAction,
                        resultGraphicsLayer = resultGraphicsLayer,
                        onTextOptionClick = onNavigateToInputText,
                        onImageOptionClick = {
                            showImageBottomSheet = true
                            imagePermissionLauncher.launch(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    arrayOf(
                                        Manifest.permission.READ_MEDIA_IMAGES,
                                        Manifest.permission.READ_MEDIA_VIDEO,
                                    )
                                } else {
                                    arrayOf(
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    )
                                }
                            )
                        },
                        onNavigateToInputText = onNavigateToInputText,
                        onInitInputText = onInitInputText,
                        onBack = {
                            onAction(CameraAction.ResetPhoto)
                        }
                    )
                } else {
                    BeforeTakePhotoLayout(
                        controller = controller,
                        graphicsLayer = graphicsLayer,
                        backgroundItems = backgroundItems.toImmutableList(),
                        currentBackgroundImageIndex = uiState.currentBackgroundImageIndex,
                        onSelfieClick = {
                            controller.cameraSelector =
                                if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                }
                        },
                        onBack = {
                            onBack()
                            onAction(CameraAction.ResetPhoto)
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(color = Color.White)
            ) {
                if (isPhotoTaken) {
                    AfterTakePhotoBottomLayout(
                        context = context,
                        resultGraphicsLayer = resultGraphicsLayer,
                        folderName = DhCamera.getFolderName(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        onAction = onAction
                    )
                } else {
                    BeforeTakePhotoBottomLayout(
                        context = context,
                        graphicsLayer = graphicsLayer,
                        controller = controller,
                        backgroundItems = backgroundItems.toImmutableList(),
                        onAction = onAction
                    )
                }
            }
        }
    }
}

@Composable
internal fun BeforeTakePhotoLayout(
    controller: LifecycleCameraController,
    graphicsLayer: GraphicsLayer,
    backgroundItems: ImmutableList<BackgroundItem>,
    currentBackgroundImageIndex: Int,
    onSelfieClick: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .background(color = Color.Green)
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(graphicsLayer)
                }
        ) {
            CameraPreview(
                controller = controller,
                modifier = Modifier
                    .fillMaxSize()
            )

            if (backgroundItems.isNotEmpty()) {
                val currentItem = backgroundItems[currentBackgroundImageIndex]
                val modifier = if (currentItem.isFillMaxSize) {
                    Modifier
                        .padding(
                            start = currentItem.start.dp,
                            end = currentItem.end.dp,
                            top = currentItem.top.dp,
                            bottom = currentItem.bottom.dp
                        )
                        .fillMaxSize()
                } else {
                    Modifier
                        .width(currentItem.width.dp)
                        .height(currentItem.height.dp)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    when (currentItem) {
                        is BackgroundItem.BackgroundImageItem -> {
                            GlideImage(
                                imageOptions = ImageOptions(
                                    contentScale = ContentScale.FillWidth
                                ),
                                imageModel = {
                                    when {
                                        currentItem.drawable != null -> {
                                            currentItem.drawable
                                        }

                                        currentItem.imageUrl != null -> {
                                            currentItem.imageUrl
                                        }

                                        else -> {}
                                    }
                                },
                                modifier = modifier
                                    .padding(
                                        start = currentItem.start.dp,
                                        end = currentItem.end.dp,
                                        top = currentItem.top.dp,
                                        bottom = currentItem.bottom.dp
                                    )
                                    .align(backgroundItems[currentBackgroundImageIndex].align.toAlignment())
                            )
                        }

                        is BackgroundItem.BackgroundTextItem -> {
                            BackgroundTextLayout(item = currentItem)
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_back_white),
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(48.dp)
                    .background(color = colorResource(id = R.color.black_40))
                    .padding(10.dp)
                    .align(Alignment.CenterStart)
                    .noRippleClick {
                        onBack()
                    }
            )

            Image(
                painter = painterResource(id = R.drawable.ic_camera_flip_white),
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(48.dp)
                    .background(color = colorResource(id = R.color.black_40))
                    .padding(10.dp)
                    .align(Alignment.CenterEnd)
                    .noRippleClick {
                        onSelfieClick()
                    }
            )
        }
    }
}

@Composable
internal fun AfterTakePhotoLayout(
    uiState: CameraUiState,
    onAction: (CameraAction) -> Unit,
    resultGraphicsLayer: GraphicsLayer,
    onTextOptionClick: (InputTextRoute?) -> Unit,
    onImageOptionClick: () -> Unit,
    onNavigateToInputText: (InputTextRoute?) -> Unit,
    onInitInputText: () -> Unit,
    onBack: () -> Unit
) {
    var isDragging by remember {
        mutableStateOf(false)
    }
    var showCenterPortraitLine by remember {
        mutableStateOf(false)
    }
    var showCenterLandscapeLine by remember {
        mutableStateOf(false)
    }
    var centerOffset by remember {
        mutableStateOf(Offset.Zero)
    }
    var centerDeleteViewOffset by remember {
        mutableStateOf(Offset.Zero)
    }
    var deleteViewContainElement by remember {
        mutableStateOf(false)
    }
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                if (centerOffset == Offset.Zero) {
                    val x = it.size.width / 2f
                    val y = it.size.height / 2f
                    centerOffset = Offset(x, y)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .drawWithContent {
                    resultGraphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(resultGraphicsLayer)
                }
        ) {
            Image(
                bitmap = uiState.bitmap!!.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )

            Image(
                bitmap = uiState.backgroundBitmap!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )

            uiState.elements.forEachIndexed { index, element ->
                ElementView(
                    centerOffset = centerOffset,
                    centerDeleteViewOffset = centerDeleteViewOffset,
                    statusBarHeight = statusBarHeight,
                    element = element,
                    modifier = Modifier
                        .align(Alignment.Center),
                    onAction = onAction,
                    onNavigateToInputText = onNavigateToInputText,
                    onCenterPortraitShow = { show ->
                        showCenterPortraitLine = show
                    },
                    onCenterLandscapeShow = { show ->
                        showCenterLandscapeLine = show
                    },
                    onDrag = {
                        isDragging = it
                    },
                    onDeleteContained = {
                        deleteViewContainElement = it
                    },
                    onInitInputText = onInitInputText
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(64.dp)
                        .align(Alignment.Center),
                    color = Color.White,
                    trackColor = colorResource(id = R.color.gray)
                )
            }
        }

        if (isDragging && showCenterPortraitLine) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(centerOffset.x.roundToInt(), 0) }
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(color = colorResource(id = R.color.sky_blue))
            )

        }

        if (isDragging && showCenterLandscapeLine) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, centerOffset.y.roundToInt()) }
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color = colorResource(id = R.color.sky_blue))
            )
        }

        PhotoOptions(
            onTextOptionClick = onTextOptionClick,
            onImageOptionClick = onImageOptionClick,
            onBack = onBack
        )

        DeleteView(
            containElement = deleteViewContainElement,
            modifier = Modifier
                .alpha(
                    if (isDragging) {
                        1f
                    } else {
                        0f
                    }
                )
                .align(Alignment.BottomCenter)
                .padding(14.dp),
            onInitDeleteViewCenter = {
                centerDeleteViewOffset = it
            }
        )
    }
}

@Composable
internal fun dpToPx(dp: Dp): Float {
    return with(LocalDensity.current) { dp.toPx() }
}

@Composable
internal fun DeleteView(
    containElement: Boolean,
    modifier: Modifier = Modifier,
    onInitDeleteViewCenter: (Offset) -> Unit,
) {
    val size by animateDpAsState(
        targetValue = if (containElement) {
            56.dp
        } else {
            42.dp
        },
        tween(durationMillis = 100, easing = LinearEasing),
        label = ""
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.drag_delete),
            color = colorResource(id = R.color.white),
            modifier = Modifier
                .padding(bottom = 10.dp)
        )

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .border(
                    width = 3.dp,
                    color = colorResource(id = R.color.white),
                    shape = CircleShape
                )
                .size(size)
                .onGloballyPositioned {
                    val centerX = it.positionInRoot().x + (it.size.width / 2f)
                    val centerY = it.positionInRoot().y + (it.size.width / 2f)
                    onInitDeleteViewCenter(Offset(centerX, centerY))
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_delete_white),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        if (containElement) {
                            12.dp
                        } else {
                            8.dp
                        }
                    )
            )
        }
    }
}

internal fun calculateDistance(offset1: Offset, offset2: Offset): Float {
    return if (offset1.x == 0f && offset1.y == 0f) {
        1000f
    } else {
        sqrt((offset2.x - offset1.x).pow(2) + (offset2.y - offset1.y).pow(2))
    }
}

@Composable
internal fun ElementView(
    centerOffset: Offset,
    centerDeleteViewOffset: Offset,
    statusBarHeight: Int,
    element: Element,
    modifier: Modifier = Modifier,
    onAction: (CameraAction) -> Unit,
    onNavigateToInputText: (InputTextRoute) -> Unit,
    onCenterPortraitShow: (Boolean) -> Unit,
    onCenterLandscapeShow: (Boolean) -> Unit,
    onDrag: (Boolean) -> Unit,
    onDeleteContained: (Boolean) -> Unit,
    onInitInputText: () -> Unit
) {
    val length = dpToPx(dp = 28.dp)
    var elementCenterOffset by remember(element.centerOffset) {
        mutableStateOf(element.centerOffset)
    }
    val isDeleteContained by remember(elementCenterOffset) {
        derivedStateOf {
            calculateDistance(elementCenterOffset, centerDeleteViewOffset) <= length
        }
    }

    var prevScale by remember(element.prevScale) { mutableFloatStateOf(element.prevScale) }
    var scale by remember(element.scale) { mutableFloatStateOf(element.scale) }
    val animScale by if (isDeleteContained) {
        animateFloatAsState(
            targetValue = scale,
            tween(durationMillis = 100, easing = LinearEasing),
            label = ""
        )
    } else {
        remember(element.scale) { mutableFloatStateOf(element.scale) }
    }
    var rotation by remember(element.rotation) { mutableFloatStateOf(element.rotation) }
    var offset by remember(element.offset) { mutableStateOf(element.offset) }
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        prevScale *= zoomChange
        scale = if (isDeleteContained) {
            0.5f
        } else {
            prevScale * zoomChange
        }
        rotation += rotationChange
        offset += (offsetChange * scale).rotate(rotation)
        elementCenterOffset += (offsetChange * scale).rotate(rotation)

        val isXCenter = elementCenterOffset.x >= centerOffset.x.roundToInt() - 40 &&
                elementCenterOffset.x <= centerOffset.x.roundToInt() + 40

        val isYCenter = elementCenterOffset.y >= centerOffset.y.roundToInt() - 40 &&
                elementCenterOffset.y <= centerOffset.y.roundToInt() + 40

        if (isXCenter) {
            offset = Offset(0f, offset.y)
        }

        if (isYCenter) {
            offset = Offset(offset.x, 0f)
        }

        onCenterPortraitShow(isXCenter)
        onCenterLandscapeShow(isYCenter)

        onAction(
            CameraAction.ChangeElementProperties(
                id = element.id,
                prevScale = prevScale,
                scale = scale,
                rotation = rotation,
                centerOffset = elementCenterOffset,
                offset = offset
            )
        )
    }

    LaunchedEffect(isDeleteContained) {
        onDeleteContained(isDeleteContained)
    }

    LaunchedEffect(state.isTransformInProgress) {
        onDrag(state.isTransformInProgress)
        if (elementCenterOffset != Offset.Zero && isDeleteContained && !state.isTransformInProgress) {
            onAction(CameraAction.DeleteElement(element.id))
            onInitInputText()
        }
    }

    Box(
        modifier = modifier
            .padding(15.dp)
            .graphicsLayer(
                scaleX = animScale.coerceIn(0.5f..5f),
                scaleY = animScale.coerceIn(0.5f..5f),
                rotationZ = rotation,
                translationX = offset.x,
                translationY = offset.y
            )
            .onGloballyPositioned {
                if (elementCenterOffset == Offset.Zero) {
                    val centerX = (it.positionInRoot().x + (it.size.width / 2f))
                    val centerY = (it.positionInRoot().y - statusBarHeight + (it.size.height / 2f))
                    elementCenterOffset = Offset(centerX, centerY)
                }
            }
            .transformable(state = state)
    ) {
        when (element) {
            is Element.Image -> {
                GlideImage(
                    imageModel = { element.imageUri },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.FillWidth
                    ),
                    modifier = Modifier
                        .width(180.dp)
                )
            }

            is Element.Text -> {
                Text(
                    text = element.text,
                    fontSize = 48.sp,
                    color = colorResource(id = element.color),
                    fontFamily = if (element.fontId == 0) {
                        null
                    } else {
                        FontFamily(Font(element.fontId))
                    },
                    textAlign = when (element.alignment) {
                        FontAlign.Center -> TextAlign.Center
                        FontAlign.Left -> TextAlign.Start
                        FontAlign.Right -> TextAlign.End
                    },
                    modifier = Modifier
                        .noRippleClick {
                            onNavigateToInputText(
                                InputTextRoute(
                                    id = element.id,
                                    text = element.text,
                                    font = element.fontId,
                                    color = element.color,
                                    alignment = element.alignment
                                )
                            )
                        }
                )
            }
        }
    }
}

internal fun Offset.rotate(rotation: Float): Offset {
    val angleInRadians = rotation * PI / 180
    return Offset(
        (x * cos(angleInRadians) - y * sin(angleInRadians)).toFloat(),
        (x * sin(angleInRadians) + y * cos(angleInRadians)).toFloat()
    )
}

@Composable
internal fun PhotoOptions(
    onTextOptionClick: (InputTextRoute?) -> Unit,
    onImageOptionClick: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_back_white),
            contentDescription = null,
            modifier = Modifier
                .padding(end = 8.dp)
                .clip(CircleShape)
                .size(48.dp)
                .background(color = colorResource(id = R.color.black_40))
                .padding(10.dp)
                .align(Alignment.CenterStart)
                .noRippleClick {
                    onBack()
                }
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.CenterEnd)
        ) {
            if (DhCamera.getEnableInputText()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_text_field_white),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(48.dp)
                        .background(color = colorResource(id = R.color.black_40))
                        .padding(10.dp)
                        .noRippleClick {
                            onTextOptionClick(null)
                        }
                )
            }

            if (DhCamera.getEnableAddGalleryImage()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_gallery_white),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(48.dp)
                        .background(color = colorResource(id = R.color.black_40))
                        .padding(10.dp)
                        .noRippleClick {
                            onImageOptionClick()
                        }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PhotoOptionsPreview() {
    PhotoOptions(
        onTextOptionClick = {},
        onImageOptionClick = {},
        onBack = {}
    )
}

@Composable
internal fun BeforeTakePhotoBottomLayout(
    context: Context,
    graphicsLayer: GraphicsLayer,
    controller: LifecycleCameraController,
    backgroundItems: ImmutableList<BackgroundItem>,
    onAction: (CameraAction) -> Unit
) {
    val configuration = LocalConfiguration.current
    val proportion = ((configuration.screenHeightDp - 180) / 64)
    val scope = rememberCoroutineScope()

    Column {
        if (backgroundItems.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(top = 10.dp)
            ) {
                itemsIndexed(items = backgroundItems, key = { index, _ ->
                    index
                }) { index, item ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .width(64.dp)
                            .aspectRatio(1f)
                            .run {
                                if (DhCamera.getThumbnailBackground() == null) {
                                    background(color = colorResource(id = R.color.light_gray))
                                } else {
                                    background(color = colorResource(id = R.color.white))
                                }
                            }
                            .clickable {
                                onAction(CameraAction.ChangeBackgroundImage(selectedIndex = index))
                            }
                    ) {
                        if (DhCamera.getThumbnailBackground() != null) {
                            GlideImage(
                                imageModel = { DhCamera.getThumbnailBackground() },
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }

                        val modifier = Modifier
                            .padding(
                                start = item.start.dp / proportion + 5.dp,
                                end = item.end.dp / proportion + 5.dp,
                                top = item.top.dp / proportion + 5.dp,
                                bottom = item.bottom.dp / proportion + 5.dp
                            )
                            .run {
                                if (item.isFillMaxSize) {
                                    fillMaxSize()
                                } else {
                                    width((item.width / proportion).dp)
                                        .height((item.height / proportion).dp)
                                        .align(item.align.toAlignment())
                                }
                            }

                        when (item) {
                            is BackgroundItem.BackgroundImageItem -> {
                                GlideImage(
                                    imageOptions = ImageOptions(
                                        contentScale = ContentScale.FillWidth
                                    ),
                                    imageModel = {
                                        when {
                                            item.drawable != null -> {
                                                item.drawable
                                            }

                                            item.imageUrl != null -> {
                                                item.imageUrl
                                            }

                                            else -> {}
                                        }
                                    },
                                    modifier = modifier
                                )
                            }

                            is BackgroundItem.BackgroundTextItem -> {
                                BackgroundTextThumbnailLayout(item = item)
                            }
                        }

                        if (item.isSelected) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .fillMaxSize()
                                    .background(color = colorResource(id = R.color.black_40))
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            CameraButton(
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                scope.launch {
                    takePhoto(
                        context = context,
                        controller = controller,
                        backgroundImageBitmap = graphicsLayer.toImageBitmap(),
                        onAction = onAction
                    )
                }
            }
        }
    }
}

@Composable
internal fun BackgroundTextLayout(item: BackgroundItem.BackgroundTextItem) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .run {
                    if (item.showTextBackground) {
                        padding(
                            start = item.start.dp,
                            end = item.end.dp,
                            top = item.top.dp,
                            bottom = item.bottom.dp
                        )
                            .clip(RoundedCornerShape(10.dp))
                            .background(color = colorResource(id = R.color.black_40))
                            .align(
                                item.align.toAlignment()
                            )
                    } else {
                        padding(
                            start = item.start.dp,
                            end = item.end.dp,
                            top = item.top.dp,
                            bottom = item.bottom.dp
                        )
                            .align(item.align.toAlignment())
                    }
                }
                .run {
                    if (item.width == 0 || item.height == 0) {
                        wrapContentSize()
                    } else {
                        width(item.width.dp)
                            .height(item.height.dp)
                    }
                }
        ) {
            Text(
                textAlign = item.textAlign.toTextAlignment(),
                color = if (item.textColor == -1) {
                    Color.White
                } else {
                    colorResource(id = item.textColor)
                },
                fontSize = if (item.textSize == 0) {
                    TextUnit.Unspecified
                } else {
                    item.textSize.sp
                },
                text = item.text,
                fontFamily = if (item.font == null) {
                    null
                } else {
                    FontFamily(item.font)
                },
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
internal fun BackgroundTextThumbnailLayout(item: BackgroundItem.BackgroundTextItem) {
    val configuration = LocalConfiguration.current
    val proportion = ((configuration.screenHeightDp - 180) / 64)

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .run {
                    if (item.showTextBackground) {
                        padding(
                            start = (item.start / proportion).dp,
                            end = (item.end / proportion).dp,
                            top = (item.top / proportion).dp,
                            bottom = (item.bottom / proportion).dp
                        )
                            .clip(RoundedCornerShape(10.dp))
                            .background(color = colorResource(id = R.color.black_40))
                            .align(
                                item.align.toAlignment()
                            )
                    } else {
                        align(item.align.toAlignment())
                    }
                }
        ) {
            Text(
                textAlign = item.textAlign.toTextAlignment(),
                color = if (item.textColor == -1) {
                    Color.White
                } else {
                    colorResource(id = item.textColor)
                },
                fontFamily = if (item.font == null) {
                    null
                } else {
                    FontFamily(item.font)
                },
                fontSize = 4.sp,
                text = item.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding((10 / proportion).dp)
            )
        }
    }
}

@Composable
internal fun AfterTakePhotoBottomLayout(
    context: Context,
    resultGraphicsLayer: GraphicsLayer,
    folderName: String,
    modifier: Modifier,
    onAction: (CameraAction) -> Unit
) {
    val scope = rememberCoroutineScope()

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
    ) {
        if (DhCamera.getPrevBtnImage() == null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .width(72.dp)
                    .height(72.dp)
                    .background(color = colorResource(id = R.color.primary))
                    .clickable {
                        onAction(CameraAction.ResetPhoto)
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_replay_white),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                )
            }
        } else {
            GlideImage(
                imageModel = {
                    DhCamera.getPrevBtnImage()
                },
                modifier = Modifier
                    .width(72.dp)
                    .height(72.dp)
                    .align(Alignment.CenterVertically)
                    .clickable {
                        onAction(CameraAction.ResetPhoto)
                    }
            )
        }

        if (DhCamera.getNextBtnImage() == null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .width(72.dp)
                    .height(72.dp)
                    .background(color = colorResource(id = R.color.primary))
                    .clickable {
                        scope.launch {
                            savePhoto(
                                bitmap = resultGraphicsLayer.toImageBitmap(),
                                folderName = folderName.ifEmpty { "DhPicture" },
                                context = context,
                                onAction = onAction
                            )
                        }
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_download_white),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                )
            }
        } else {
            GlideImage(
                imageModel = {
                    DhCamera.getNextBtnImage()
                },
                modifier = Modifier
                    .width(72.dp)
                    .height(72.dp)
                    .align(Alignment.CenterVertically)
                    .clickable {
                        scope.launch {
                            savePhoto(
                                bitmap = resultGraphicsLayer.toImageBitmap(),
                                folderName = folderName.ifEmpty { "DhPicture" },
                                context = context,
                                onAction = onAction
                            )
                        }
                    }
            )
        }
    }
}

internal fun Int.toAlignment() = when (this) {
    TOP_START -> Alignment.TopStart
    TOP_CENTER -> Alignment.TopCenter
    TOP_END -> Alignment.TopEnd
    CENTER_START -> Alignment.CenterStart
    CENTER -> Alignment.Center
    CENTER_END -> Alignment.CenterEnd
    BOTTOM_START -> Alignment.BottomStart
    BOTTOM_CENTER -> Alignment.BottomCenter
    BOTTOM_END -> Alignment.BottomEnd
    else -> {
        Alignment.Center
    }
}

internal fun Int.toTextAlignment() = when (this) {
    TEXT_START -> TextAlign.Start
    TEXT_END -> TextAlign.End
    TEXT_CENTER -> TextAlign.Center
    else -> TextAlign.Center
}

@Composable
internal fun CameraButton(modifier: Modifier = Modifier, onPhotoTaken: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .width(76.dp)
            .aspectRatio(1f)
            .border(color = colorResource(id = R.color.black), width = 8.dp, shape = CircleShape)
            .padding(10.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onPhotoTaken()
            }
    )
}

@Preview(showBackground = true)
@Composable
private fun CameraButtonPreview() {
    CameraButton(modifier = Modifier) {

    }
}

private suspend fun savePhoto(
    folderName: String,
    context: Context,
    bitmap: ImageBitmap,
    onAction: (CameraAction) -> Unit
) {
    onAction(CameraAction.SavingPhoto)
    withContext(NonCancellable) {
        val savedUrl = saveBitmap(context = context, bitmap = bitmap, folderName = folderName)
        onAction(CameraAction.SavedPhoto(savedUrl = savedUrl))
    }
}

private suspend fun saveBitmap(
    context: Context,
    bitmap: ImageBitmap,
    folderName: String
): SavedUrl {
    val imageName = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        .format(System.currentTimeMillis())
    val timestamp = System.currentTimeMillis()
    var savedUrl: Uri? = null

    withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, timestamp)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, timestamp)
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
            values.put(MediaStore.Images.Media.IS_PENDING, true)

            val uri =
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                try {
                    val outputStream = context.contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        bitmap.asAndroidBitmap()
                            .compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        outputStream.close()
                    }
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    context.contentResolver.update(uri, values, null, null)
                } catch (_: Exception) {

                }
            }
            savedUrl = uri
        } else {
            val imageFileFolder =
                File(Environment.getExternalStorageDirectory().toString() + '/' + folderName)
            if (!imageFileFolder.exists()) {
                imageFileFolder.mkdirs()
            }
            val mImageName = "$timestamp.png"
            val imageFile = File(imageFileFolder, mImageName)
            try {
                val outputStream: OutputStream = FileOutputStream(imageFile)

                bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()

                values.put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            } catch (_: Exception) {
            }
            savedUrl = imageFile.toUri()
        }
    }

    return savedUrl?.toString() ?: ""
}

private fun takePhoto(
    context: Context,
    controller: LifecycleCameraController,
    backgroundImageBitmap: ImageBitmap,
    onAction: (CameraAction) -> Unit,
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )
                onAction(
                    CameraAction.TakePhoto(
                        bitmap = rotatedBitmap,
                        backgroundBitmap = backgroundImageBitmap
                    )
                )
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Couldn't take photo: ", exception)
            }
        }
    )
}