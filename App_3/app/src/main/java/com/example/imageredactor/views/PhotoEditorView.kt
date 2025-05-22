package com.example.imageredactor.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.imageredactor.models.TextElement


class PhotoEditorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val source: ImageView

    val drawingView: DrawingView

    val textOverlayView: TextOverlayView

    var onTextElementClickListener: ((TextElement) -> Unit)? = null
        set(value) {
            field = value
            textOverlayView.onTextElementClickListener = value
        }

    var onTextDoubleClickListener: ((TextElement) -> Unit)? = null
        set(value) {
            field = value
            textOverlayView.onTextDoubleClickListener = value
        }

    init {
        setWillNotDraw(false)

        Log.d("PhotoEditorView", "Инициализация PhotoEditorView")

        source = ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        addView(source)

        drawingView = DrawingView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        addView(drawingView)

        textOverlayView = TextOverlayView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        addView(textOverlayView)

        Log.d("PhotoEditorView", "Все view добавлены")
    }

    fun getFinalBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val textOverlayVisibility = textOverlayView.visibility
        textOverlayView.visibility = View.INVISIBLE

        draw(canvas)

        textOverlayView.visibility = textOverlayVisibility

        val textElements = textOverlayView.getTextElements()
        for (textElement in textElements) {
            val paint = Paint().apply {
                color = textElement.color
                textSize = textElement.size
                textElement.typeface?.let { typeface = it }
                isAntiAlias = true
                setShadowLayer(1f, 0f, 1f, Color.BLACK)
            }

            canvas.drawText(
                textElement.text,
                textElement.x,
                textElement.y,
                paint
            )
        }

        return bitmap
    }

    fun addText(text: String, x: Float, y: Float, color: Int, size: Float, typeface: Typeface? = null): TextElement {
        Log.d("PhotoEditorView", "addText: Добавление текста '$text' через TextOverlayView")
        return textOverlayView.addText(text, x, y, color, size, typeface)
    }

    fun removeText(textElement: TextElement) {
        textOverlayView.removeText(textElement)
    }

    fun updateTextElement(textElement: TextElement) {
        textOverlayView.updateTextElement(textElement)
    }

    fun clearDrawing() {
        drawingView.clearPaths()
    }

    fun setBrushDrawingMode(enabled: Boolean) {
        Log.d("PhotoEditorView", "Установка режима рисования: $enabled")
        drawingView.isDrawingEnabled = enabled
    }

    fun setEraserMode(enabled: Boolean) {
        Log.d("PhotoEditorView", "Установка режима ластика: $enabled")
        drawingView.isEraserEnabled = enabled
        if (enabled) {
            drawingView.isDrawingEnabled = true
        }
    }

    fun setBrushColor(color: Int) {
        drawingView.brushColor = color
    }

    fun setBrushSize(size: Float) {
        drawingView.brushSize = size
    }
}