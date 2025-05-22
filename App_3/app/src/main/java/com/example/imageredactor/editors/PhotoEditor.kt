package com.example.imageredactor.editors

import android.content.Context
import android.graphics.Bitmap
import com.example.imageredactor.models.TextElement
import com.example.imageredactor.views.PhotoEditorView



class PhotoEditor private constructor(
    private val context: Context,
    private val photoEditorView: PhotoEditorView
) {

    var brushColor: Int
        get() = photoEditorView.drawingView.brushColor
        set(value) {
            photoEditorView.setBrushColor(value)
        }

    var brushSize: Float
        get() = photoEditorView.drawingView.brushSize
        set(value) {
            photoEditorView.setBrushSize(value)
        }

    private val textElements = mutableListOf<TextElement>()

    init {
        photoEditorView.onTextElementClickListener = { textElement ->
            textElement.isSelected = !textElement.isSelected
            photoEditorView.updateTextElement(textElement)
        }

        photoEditorView.onTextDoubleClickListener = { textElement ->
        }
    }

    fun setBrushDrawingMode(enabled: Boolean) {
        photoEditorView.setBrushDrawingMode(enabled)
    }

    fun setEraserMode(enabled: Boolean) {
        photoEditorView.setEraserMode(enabled)
    }

    fun addText(text: String, styleBuilder: TextStyleBuilder) {
        if (text.isEmpty()) {
            return
        }

        android.util.Log.d("PhotoEditor", "Добавление текста: $text")
        android.util.Log.d("PhotoEditor", "Размеры view: ${photoEditorView.width}x${photoEditorView.height}")

        val x = (photoEditorView.width / 4).toFloat()
        val y = (photoEditorView.height / 3).toFloat()

        val textSize = if (styleBuilder.textSize < 20f) 40f else styleBuilder.textSize

        android.util.Log.d("PhotoEditor", "Координаты текста: ($x, $y), размер: $textSize")

        val textElement = photoEditorView.addText(
            text,
            x,
            y,
            styleBuilder.textColor,
            textSize,
            styleBuilder.textFont
        )

        textElement.isSelected = true

        textElements.add(textElement)

        photoEditorView.invalidate()
    }

    fun updateTextElement(textElement: TextElement) {
        photoEditorView.updateTextElement(textElement)
        photoEditorView.invalidate()
    }

    fun clearDrawing() {
        photoEditorView.clearDrawing()
    }

    fun getResultBitmap(): Bitmap {
        android.util.Log.d("PhotoEditor", "Количество текстовых элементов: ${textElements.size}")

        textElements.forEachIndexed { index, textElement ->
            android.util.Log.d("PhotoEditor", "Текстовый элемент #$index: '${textElement.text}' " +
                    "на позиции (${textElement.x}, ${textElement.y}), " +
                    "размер: ${textElement.size}, цвет: ${textElement.color}")
        }

        return photoEditorView.getFinalBitmap()
    }

    class Builder(private val context: Context, private val photoEditorView: PhotoEditorView) {
        private var isTextPinchZoomable = true

        fun setPinchTextScalable(isPinchZoomable: Boolean): Builder {
            isTextPinchZoomable = isPinchZoomable
            return this
        }

        fun build(): PhotoEditor {
            return PhotoEditor(context, photoEditorView)
        }
    }
}