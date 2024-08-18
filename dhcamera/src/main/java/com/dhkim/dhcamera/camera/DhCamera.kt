package com.dhkim.dhcamera.camera

import android.content.Context
import android.content.Intent
import com.dhkim.dhcamera.camera.model.FontItem

object DhCamera {

    const val TOP_START = 0
    const val TOP_CENTER = 1
    const val TOP_END = 2
    const val CENTER_START = 3
    const val CENTER = 4
    const val CENTER_END = 5
    const val BOTTOM_START = 6
    const val BOTTOM_CENTER = 7
    const val BOTTOM_END = 8

    const val TEXT_START = 0
    const val TEXT_END = 1
    const val TEXT_CENTER = 2

    private var folderName = "DhCamera"
    private var onCompleted: (SavedUrl) -> Unit = {}
    private var onPermissionDenied: (Permission) -> Unit = {}
    private var backgroundItems = mutableListOf<BackgroundItem>()
    private var nextBtnImage: Any? = null
    private var prevBtnImage: Any? = null
    private var thumbnailBackground: Any? = null
    private var isFinishCamera: Boolean = false

    private var fontElements = listOf<FontItem>()

    class Builder(private val context: Context) {

        fun backgroundItems(backgroundItems: List<BackgroundItem>): Builder {
            DhCamera.backgroundItems.clear()
            backgroundItems.forEach { backgroundImage ->
                DhCamera.backgroundItems.add(backgroundImage)
            }
            return this
        }

        fun folderName(folderName: String): Builder {
            DhCamera.folderName = folderName.ifEmpty { "DhCamera" }
            return this
        }

        fun onCompleted(isFinishCamera: Boolean, onCompleted: (SavedUrl) -> Unit): Builder {
            DhCamera.isFinishCamera = isFinishCamera
            DhCamera.onCompleted = onCompleted
            return this
        }

        fun onPermissionDenied(onPermissionDenied: (Permission) -> Unit): Builder {
            DhCamera.onPermissionDenied = onPermissionDenied
            return this
        }

        fun prevBtnImage(prevBtnImage: Any): Builder {
            DhCamera.prevBtnImage = prevBtnImage
            return this
        }

        fun nextBtnImage(nextBtnImage: Any): Builder {
            DhCamera.nextBtnImage = nextBtnImage
            return this
        }

        fun thumbnailBackground(thumbnailBackground: Any): Builder {
            DhCamera.thumbnailBackground = thumbnailBackground
            return this
        }

        fun fontElements(fontElements: List<FontItem>): Builder {
            DhCamera.fontElements = fontElements
            return this
        }

        fun start() {
            Intent(context, CameraActivity::class.java).run {
                context.startActivity(this)
            }
        }
    }

    internal fun isFinishCamera() = isFinishCamera
    internal fun getFolderName() = folderName
    internal fun getOnCompleted() = onCompleted
    internal fun getOnPermissionDenied() = onPermissionDenied
    internal fun getBackgroundItems() = backgroundItems
    internal fun getThumbnailBackground() = thumbnailBackground
    internal fun getPrevBtnImage() = prevBtnImage
    internal fun getNextBtnImage() = nextBtnImage
    internal fun getFontElements() = fontElements
}