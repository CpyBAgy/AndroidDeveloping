package com.example.imageredactor.models

import android.graphics.Typeface

data class TextElement(
    var text: String,
    var x: Float,
    var y: Float,
    var color: Int,
    var size: Float,
    var typeface: Typeface? = null,
    var isSelected: Boolean = false
)
