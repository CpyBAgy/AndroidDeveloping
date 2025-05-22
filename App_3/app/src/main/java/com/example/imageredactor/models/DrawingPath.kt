package com.example.imageredactor.models

import android.graphics.Path

data class DrawingPath(
    val path: Path,
    val color: Int,
    val width: Float,
    val isEraser: Boolean = false
)
