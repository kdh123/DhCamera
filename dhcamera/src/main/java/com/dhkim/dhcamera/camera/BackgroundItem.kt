package com.dhkim.dhcamera.camera

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat


sealed class BackgroundItem(
    val align: Int,
    val width: Int,
    val height: Int,
    val isFillMaxSize: Boolean,
    val start: Int,
    val end: Int,
    val top: Int,
    val bottom: Int,
    val isSelected: Boolean
) {
    data class BackgroundImageItem(
        val imageUrl: Any? = null,
        val drawable: Drawable? = null,
        val propAlign: Int = 4,
        val propWidth: Int = 0,
        val propHeight: Int = 0,
        val propIsFillMaxSize: Boolean = false,
        val propStart: Int = 0,
        val propEnd: Int = 0,
        val propTop: Int = 0,
        val propBottom: Int = 0,
        val propIsSelected: Boolean = false
    ) : BackgroundItem(
        align = propAlign,
        width = propWidth,
        height = propHeight,
        isFillMaxSize = propIsFillMaxSize,
        start = propStart,
        end = propEnd,
        top = propTop,
        bottom = propBottom,
        isSelected = propIsSelected
    )

    data class BackgroundTextItem(
        val text: String,
        val textColor: Int = -1,
        val textSize: Int = 0,
        val textAlign: Int = 2,
        val font: Typeface? = null,
        val showTextBackground: Boolean = true,
        val propAlign: Int = 4,
        val propWidth: Int = 0,
        val propHeight: Int = 0,
        val propIsFillMaxSize: Boolean = false,
        val propStart: Int = 0,
        val propEnd: Int = 0,
        val propTop: Int = 0,
        val propBottom: Int = 0,
        val propIsSelected: Boolean = false
    ) : BackgroundItem(
        align = propAlign,
        width = propWidth,
        height = propHeight,
        isFillMaxSize = propIsFillMaxSize,
        start = propStart,
        end = propEnd,
        top = propTop,
        bottom = propBottom,
        isSelected = propIsSelected
    )
}

data class Padding(
    val start: Int = 0,
    val end: Int = 0,
    val top: Int = 0,
    val bottom: Int = 0
)

class BackgroundImage {

    class Builder(private val context: Context) {

        private var align: Int = 4
        private var width: Int = 0
        private var height: Int = 0
        private var padding: Padding = Padding()
        private var isFillMaxSize: Boolean = false
        private var drawable: Drawable? = null
        private var imageUrl: Any? = null

        fun imageUrl(imageUrl: Any): Builder {
            this.imageUrl = imageUrl
            return this
        }

        fun drawable(resName: String): Builder {
            val packageName = context.packageName
            val resId = context.resources.getIdentifier(resName, "drawable", packageName)
            drawable = if (resId != 0) {
                context.getDrawable(resId)
            } else {
                null
            }

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
                imageUrl = imageUrl,
                drawable = drawable,
                propAlign = align,
                propWidth = width,
                propHeight = height,
                propIsFillMaxSize = isFillMaxSize,
                propStart = padding.start,
                propEnd = padding.end,
                propTop = padding.top,
                propBottom = padding.bottom
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
                text = text,
                textColor = textColor,
                textAlign = textAlign,
                textSize = textSize,
                font = font,
                showTextBackground = showTextBackground,
                propAlign = align,
                propWidth = width,
                propHeight = height,
                propIsFillMaxSize = isFillMaxSize,
                propStart = padding.start,
                propEnd = padding.end,
                propTop = padding.top,
                propBottom = padding.bottom
            )
        }
    }
}
