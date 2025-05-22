package com.example.imageredactor.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import com.example.imageredactor.models.TextElement


class TextOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val textContainers = mutableListOf<TextContainer>()

    var onTextElementClickListener: ((TextElement) -> Unit)? = null

    var onTextDoubleClickListener: ((TextElement) -> Unit)? = null

    init {
        Log.d("TextOverlayView", "Инициализация TextOverlayView")

        setWillNotDraw(false)
        isClickable = false
        isFocusable = false
    }

    fun clearAllTexts() {
        val textElementsToRemove = ArrayList(textContainers.map { it.textElement })

        textElementsToRemove.forEach { removeText(it) }

        Log.d("TextOverlayView", "Все текстовые элементы удалены")
    }

    fun addText(text: String, x: Float, y: Float, color: Int, size: Float, typeface: Typeface? = null): TextElement {
        Log.d("TextOverlayView", "Добавление текста: '$text' на позиции ($x, $y), цвет: $color, размер: $size")

        val textElement = TextElement(text, x, y, color, size, typeface)

        val container = TextContainer(context, textElement)

        container.x = x
        container.y = y - size - 20

        addView(container)
        textContainers.add(container)

        Log.d("TextOverlayView", "Текстовый элемент добавлен, текущее количество: ${textContainers.size}")

        return textElement
    }

    fun removeText(textElement: TextElement) {
        val containerToRemove = textContainers.find { it.textElement == textElement }

        if (containerToRemove != null) {
            removeView(containerToRemove)
            textContainers.remove(containerToRemove)
            Log.d("TextOverlayView", "Текстовый элемент удален, осталось: ${textContainers.size}")
        } else {
            Log.w("TextOverlayView", "Не удалось найти текстовый элемент для удаления")
        }
    }

    fun updateTextElement(textElement: TextElement) {
        val container = textContainers.find { it.textElement == textElement }
        container?.updateText(textElement)
        invalidate()
    }

    fun getTextElements(): List<TextElement> {
        return textContainers.map { it.textElement }
    }

    inner class TextContainer(
        context: Context,
        val textElement: TextElement
    ) : CardView(context) {

        private val textView: AppCompatTextView

        private val deleteButton: ImageView

        private var lastTouchX = 0f
        private var lastTouchY = 0f
        private var isDragging = false
        private var lastClickTime = 0L
        private val doubleClickTimeout = 300L

        init {
            radius = 0f
            cardElevation = 0f
            alpha = 0.95f

            setCardBackgroundColor(Color.TRANSPARENT)

            minimumWidth = 10
            minimumHeight = 10

            val contentPadding = 0
            setContentPadding(contentPadding, contentPadding, contentPadding, contentPadding)

            val containerLayout = FrameLayout(context)

            isClickable = true
            isFocusable = true

            textView = AppCompatTextView(context).apply {
                text = textElement.text
                setTextColor(textElement.color)
                textSize = textElement.size / resources.displayMetrics.density // пиксели в sp

                setPadding(0, 0, 0, 0)

                if (textElement.typeface != null) {
                    typeface = textElement.typeface
                } else {
                    setTypeface(typeface, Typeface.BOLD)
                }

                setShadowLayer(1f, 0.5f, 0.5f, Color.DKGRAY)
            }

            containerLayout.addView(textView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ))

            deleteButton = ImageView(context).apply {
                setImageBitmap(createDeleteButtonWithX())

                val buttonSize = (textElement.size * 0.6f).toInt().coerceAtLeast(24)

                val params = FrameLayout.LayoutParams(buttonSize, buttonSize)
                params.gravity = Gravity.TOP or Gravity.END
                layoutParams = params

                translationX = 8f
                translationY = -8f

                setOnClickListener {
                    removeText(textElement)
                }
            }

            containerLayout.addView(deleteButton)

            addView(containerLayout)

            Log.d("TextOverlayView", "Создан TextContainer для текста: '${textElement.text}'")
        }

        private fun createDeleteButtonWithX(): Bitmap {
            val size = 24
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val bgPaint = Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(size/2f, size/2f, size/2f, bgPaint)

            val xPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }

            canvas.drawLine(size * 0.3f, size * 0.3f, size * 0.7f, size * 0.7f, xPaint)
            canvas.drawLine(size * 0.7f, size * 0.3f, size * 0.3f, size * 0.7f, xPaint)

            return bitmap
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                val x = event.x
                val y = event.y

                val deleteButtonBounds = Rect()
                deleteButton.getHitRect(deleteButtonBounds)

                if (deleteButtonBounds.contains(x.toInt(), y.toInt())) {
                    deleteButton.performClick()
                    return true
                }
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                    isDragging = false

                    val clickTime = System.currentTimeMillis()
                    if (clickTime - lastClickTime < doubleClickTimeout) {
                        onTextDoubleClickListener?.invoke(textElement)
                        return true
                    }
                    lastClickTime = clickTime

                    bringToFront()

                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - lastTouchX
                    val dy = event.rawY - lastTouchY

                    if (!isDragging && (Math.abs(dx) > 5 || Math.abs(dy) > 5)) {
                        isDragging = true
                    }

                    if (isDragging) {
                        x += dx
                        y += dy

                        textElement.x = x
                        textElement.y = y + height / 2

                        lastTouchX = event.rawX
                        lastTouchY = event.rawY
                    }

                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        textElement.isSelected = !textElement.isSelected
                        onTextElementClickListener?.invoke(textElement)
                    }

                    isDragging = false
                    return true
                }

                MotionEvent.ACTION_CANCEL -> {
                    isDragging = false
                    return true
                }
            }

            return super.onTouchEvent(event)
        }

        fun updateText(updatedElement: TextElement) {
            textElement.text = updatedElement.text
            textElement.color = updatedElement.color
            textElement.size = updatedElement.size
            textElement.typeface = updatedElement.typeface

            textView.text = updatedElement.text
            textView.setTextColor(updatedElement.color)
            textView.textSize = updatedElement.size / resources.displayMetrics.density
            if (updatedElement.typeface != null) {
                textView.typeface = updatedElement.typeface
            }

            requestLayout()
            invalidate()
        }
    }
}