package com.dhkim.dhcamera.camera.model

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat

class FontItem(
    internal var text: String = "ABC",
    internal var font: Typeface? = null
) {

    class Builder(private val context: Context) {

        private var text: String = "ABC"
        private var font: Typeface? = null

        fun text(text: String): Builder {
            this.text = text
            return this
        }

        fun font(resId: Int): Builder {
            this.font = if (resId != 0) {
                ResourcesCompat.getFont(context, resId)
            } else {
                null
            }
            return this
        }

        fun build(): FontItem {
            return FontItem(
                text = text,
                font = font
            )
        }
    }
}