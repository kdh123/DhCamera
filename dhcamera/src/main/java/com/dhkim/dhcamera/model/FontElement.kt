package com.dhkim.dhcamera.model

class FontElement(
    internal var text: String = "ABC",
    internal var fontId: Int = 0
) {

    class Builder {

        private var text: String = "ABC"
        private var fontId: Int = 0

        fun text(text: String): Builder {
            this.text = text
            return this
        }

        fun font(resId: Int): Builder {
            this.fontId = resId
            return this
        }

        fun build(): FontElement {
            return FontElement(
                text = text,
                fontId = fontId
            )
        }
    }
}