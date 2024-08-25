package com.dhkim.dhcamera.camera.model

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat

class FontElement(
    internal var text: String = "ABC",
    internal var font: Typeface? = null,
    internal var fontId: Int = 0
) {

    class Builder(private val context: Context) {

        private var text: String = "ABC"
        private var font: Typeface? = null
        private var fontId: Int = 0

        fun text(text: String): Builder {
            this.text = text
            return this
        }

        fun font(resId: Int): Builder {
            this.fontId = resId
            this.font = if (resId != 0) {
                ResourcesCompat.getFont(context, resId)
            } else {
                null
            }
            return this
        }

        fun build(): FontElement {
            return FontElement(
                text = text,
                font = font,
                fontId = fontId
            )
        }
    }
}