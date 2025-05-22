package com.example.imageredactor.editors

import android.graphics.Typeface

class TextStyleBuilder {
    var textColor: Int = android.graphics.Color.BLACK
    var textSize: Float = 40f
    var textFont: Typeface? = null

    fun withTextColor(color: Int): TextStyleBuilder {
        this.textColor = color
        return this
    }

    fun withTextSize(size: Float): TextStyleBuilder {
        this.textSize = size
        return this
    }

    fun withTextFont(typeface: Typeface?): TextStyleBuilder {
        this.textFont = typeface
        return this
    }
}