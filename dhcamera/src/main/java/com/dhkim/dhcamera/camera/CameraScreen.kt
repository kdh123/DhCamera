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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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

typealias SavedUrl = String
typealias Permission = String

@SuppressLint("MissingPermission")
@Composable
internal fun CameraScreen(
    uiState: CameraUiState,
    sideEffect: SharedFlow<CameraSideEffect>,
    onChangeBackgroundImage: (Int) -> Unit,
    onSavingPhoto: () -> Unit,
    onSavedPhoto: (String) -> Unit,
    onTakePhoto: (Bitmap, ImageBitmap) -> Unit,
    onResetPhoto: () -> Unit,
    onNext: (SavedUrl) -> Unit,
    onPermissionDenied: (Permission) -> Unit,
    onBack: () -> Unit
) {
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

    BackHandler {
        if (uiState.bitmap != null && uiState.backgroundBitmap != null) {
            onResetPhoto()
        } else {
            (context as? Activity)?.finish()
        }
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

    Scaffold(
        topBar = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back_black),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(10.dp)
                        .clickable {
                            if (uiState.bitmap != null && uiState.backgroundBitmap != null) {
                                onResetPhoto()
                            } else {
                                onBack()
                            }
                        }
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_filp_camera_black),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterEnd)
                        .clickable {
                            controller.cameraSelector =
                                if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                }
                        }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding()
                )
        ) {
            Box(
                modifier = Modifier
                    .height(0.dp)
                    .weight(1f)
            ) {
                if (uiState.bitmap != null && uiState.backgroundBitmap != null) {
                    AfterTakePhotoLayout(
                        uiState = uiState,
                        resultGraphicsLayer = resultGraphicsLayer
                    )
                } else {
                    BeforeTakePhotoLayout(
                        controller = controller,
                        graphicsLayer = graphicsLayer,
                        backgroundItems = backgroundItems.toImmutableList(),
                        currentBackgroundImageIndex = uiState.currentBackgroundImageIndex
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(color = Color.White)
            ) {
                if (uiState.backgroundBitmap != null && uiState.bitmap != null) {
                    AfterTakePhotoBottomLayout(
                        context = context,
                        resultGraphicsLayer = resultGraphicsLayer,
                        folderName = DhCamera.getFolderName(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        onSavingPhoto = onSavingPhoto,
                        onSavedPhoto = onSavedPhoto,
                        onResetPhoto = onResetPhoto
                    )
                } else {
                    BeforeTakePhotoBottomLayout(
                        context = context,
                        graphicsLayer = graphicsLayer,
                        controller = controller,
                        backgroundItems = backgroundItems.toImmutableList(),
                        onChangeBackgroundImage = onChangeBackgroundImage,
                        onTakePhoto = onTakePhoto
                    )
                }
            }
        }
    }
}

@Composable
fun BeforeTakePhotoLayout(
    controller: LifecycleCameraController,
    graphicsLayer: GraphicsLayer,
    backgroundItems: ImmutableList<BackgroundItem>,
    currentBackgroundImageIndex: Int
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
}

@Composable
internal fun AfterTakePhotoLayout(
    uiState: CameraUiState,
    resultGraphicsLayer: GraphicsLayer
) {
    Box(modifier = Modifier
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
}

@Composable
fun BeforeTakePhotoBottomLayout(
    context: Context,
    graphicsLayer: GraphicsLayer,
    controller: LifecycleCameraController,
    backgroundItems: ImmutableList<BackgroundItem>,
    onChangeBackgroundImage: (Int) -> Unit,
    onTakePhoto: (Bitmap, ImageBitmap) -> Unit
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
                                onChangeBackgroundImage(index)
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
                        onPhotoTaken = onTakePhoto,
                    )
                }
            }
        }
    }
}

@Composable
fun BackgroundTextLayout(item: BackgroundItem.BackgroundTextItem) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .run {
                    if (item.showTextBackground) {
                        padding(
                            start = item.propStart.dp,
                            end = item.propEnd.dp,
                            top = item.propTop.dp,
                            bottom = item.propBottom.dp
                        )
                            .clip(RoundedCornerShape(10.dp))
                            .background(color = colorResource(id = R.color.black_40))
                            .align(
                                item.align.toAlignment()
                            )
                    } else {
                        padding(
                            start = item.propStart.dp,
                            end = item.propEnd.dp,
                            top = item.propTop.dp,
                            bottom = item.propBottom.dp
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
fun BackgroundTextThumbnailLayout(item: BackgroundItem.BackgroundTextItem) {
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
                            start = (item.propStart / proportion).dp,
                            end = (item.propEnd / proportion).dp,
                            top = (item.propTop / proportion).dp,
                            bottom = (item.propBottom / proportion).dp
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
fun AfterTakePhotoBottomLayout(
    context: Context,
    resultGraphicsLayer: GraphicsLayer,
    folderName: String,
    modifier: Modifier,
    onSavingPhoto: () -> Unit,
    onSavedPhoto: (String) -> Unit,
    onResetPhoto: () -> Unit
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
                        onResetPhoto()
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
                        onResetPhoto()
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
                                onSavingPhoto = onSavingPhoto,
                                onSavedPhoto = onSavedPhoto
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
                                onSavingPhoto = onSavingPhoto,
                                onSavedPhoto = onSavedPhoto
                            )
                        }
                    }
            )
        }
    }
}

fun Int.toAlignment() = when (this) {
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

fun Int.toTextAlignment() = when (this) {
    TEXT_START -> TextAlign.Start
    TEXT_END -> TextAlign.End
    TEXT_CENTER -> TextAlign.Center
    else -> TextAlign.Center
}

@Composable
fun CameraButton(modifier: Modifier = Modifier, onPhotoTaken: () -> Unit) {
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
    onSavingPhoto: () -> Unit,
    onSavedPhoto: (String) -> Unit
) {
    onSavingPhoto()
    withContext(NonCancellable) {
        val savedUrl = saveBitmap(context = context, bitmap = bitmap, folderName = folderName)
        onSavedPhoto(savedUrl)
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
    onPhotoTaken: (Bitmap, ImageBitmap) -> Unit,
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
                onPhotoTaken(rotatedBitmap, backgroundImageBitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Couldn't take photo: ", exception)
            }
        }
    )
}