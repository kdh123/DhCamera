package com.dhkim.dhcamera.model

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Stable
import androidx.core.content.res.ResourcesCompat
import com.dhkim.dhcamera.camera.DhCamera
import java.util.UUID

@Stable
sealed interface BackgroundItem {

    val id: String
    val align: Int
    val width: Int
    val height: Int
    val isFillMaxSize: Boolean
    val start: Int
    val end: Int
    val top: Int
    val bottom: Int
    val isSelected: Boolean

    @Stable
    data class BackgroundImageItem(
        override val id: String = "${UUID.randomUUID()}",
        override val align: Int = DhCamera.CENTER,
        override val width: Int = 0,
        override val height: Int = 0 ,
        override val isFillMaxSize: Boolean = false,
        override val start: Int = 0,
        override val end: Int = 0,
        override val top: Int = 0,
        override val bottom: Int = 0,
        override val isSelected: Boolean = false,
        val imageSrc: Any? = null
    ) : BackgroundItem

    @Stable
    data class BackgroundTextItem(
        override val id: String = "${UUID.randomUUID()}",
        override val align: Int = DhCamera.CENTER,
        override val width: Int = 0,
        override val height: Int = 0 ,
        override val isFillMaxSize: Boolean = false,
        override val start: Int = 0,
        override val end: Int = 0,
        override val top: Int = 0,
        override val bottom: Int = 0,
        override val isSelected: Boolean = false,
        val text: String,
        val textColor: Int = -1,
        val textSize: Int = 0,
        val textAlign: Int = 2,
        val font: Typeface? = null,
        val showTextBackground: Boolean = true,
    ) : BackgroundItem
}

data class Padding(
    val start: Int = 0,
    val end: Int = 0,
    val top: Int = 0,
    val bottom: Int = 0
)

class BackgroundImage {

    class Builder {

        private var align: Int = DhCamera.CENTER
        private var width: Int = 0
        private var height: Int = 0
        private var padding: Padding = Padding()
        private var isFillMaxSize: Boolean = false
        private var imageSrc: Any? = null

        fun imageSrc(imageSrc: Any): Builder {
            this.imageSrc = imageSrc
            return this
        }

        fun align(align: Int): Builder {
            this.align = align
            return this
        }

        fun width(with: Int): Builder {
            this.width = with
            return this
        }

        fun height(height: Int): Builder {
            this.height = height
            return this
        }

        fun padding(start: Int = 0, end: Int = 0, top: Int = 0, bottom: Int = 0): Builder {
            this.padding = Padding(start, end, top, bottom)
            return this
        }

        fun fillMaxSize(): Builder {
            this.isFillMaxSize = true
            return this
        }

        fun build(): BackgroundItem.BackgroundImageItem {
            return BackgroundItem.BackgroundImageItem(
                align = align,
                width = width,
                height = height,
                isFillMaxSize = isFillMaxSize,
                start = padding.start,
                end = padding.end,
                top = padding.top,
                bottom = padding.bottom,
                imageSrc = imageSrc
            )
        }
    }
}

class BackgroundText {

    class Builder(private val context: Context) {

        private var text: String = ""
        private var textSize: Int = 0
        private var textColor: Int = -1
        private var textAlign: Int = 2
        private var align: Int = 4
        private var width: Int = 0
        private var height: Int = 0
        private var padding: Padding = Padding()
        private var showTextBackground: Boolean = false
        private var isFillMaxSize: Boolean = false
        private var fontFamily: Int = -1
        private var font: Typeface? = null

        fun text(text: String): Builder {
            this.text = text
            return this
        }

        fun font(resId: Int): Builder {
            this.font = if (resId != 0 ) {
                ResourcesCompat.getFont(context, resId)
            } else {
                null
            }
            return this
        }

        fun textSize(textSize: Int): Builder {
            this.textSize = textSize
            return this
        }

        fun align(align: Int): Builder {
            this.align = align
            return this
        }

        fun width(with: Int): Builder {
            this.width = with
            return this
        }

        fun height(height: Int): Builder {
            this.height = height
            return this
        }

        fun padding(start: Int = 0, end: Int = 0, top: Int = 0, bottom: Int = 0): Builder {
            this.padding = Padding(start, end, top, bottom)
            return this
        }

        fun fillMaxSize(): Builder {
            this.isFillMaxSize = true
            return this
        }

        fun textColor(textColor: Int): Builder {
            this.textColor = textColor
            return this
        }

        fun textAlign(textAlign: Int): Builder {
            this.textAlign = textAlign
            return this
        }

        fun showTextBackground(): Builder {
            this.showTextBackground = true
            return this
        }

        fun build(): BackgroundItem.BackgroundTextItem {
            return BackgroundItem.BackgroundTextItem(
                align = align,
                width = width,
                height = height,
                isFillMaxSize = isFillMaxSize,
                start = padding.start,
                end = padding.end,
                top = padding.top,
                bottom = padding.bottom,
                text = text,
                textColor = textColor,
                textAlign = textAlign,
                textSize = textSize,
                font = font,
                showTextBackground = showTextBackground,
            )
        }
    }
}
