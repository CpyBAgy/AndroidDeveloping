package com.example.imageredactor.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.imageredactor.models.DrawingPath


class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paths = mutableListOf<DrawingPath>()

    private var currentPath: Path? = null

    private val currentPoint = PointF()

    var isDrawingEnabled = false

    var isEraserEnabled = false

    var brushColor = Color.BLACK

    var brushSize = 10f

    var onDrawingFinished: ((DrawingPath) -> Unit)? = null

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
        Log.d("DrawingView", "Инициализация DrawingView")
    }

    fun clearPaths() {
        paths.clear()
        invalidate()
    }

    fun addPath(path: DrawingPath) {
        paths.add(path)
        invalidate()
    }

    fun removePath(path: DrawingPath) {
        paths.remove(path)
        invalidate()
    }

    fun getAllPaths(): List<DrawingPath> {
        return paths.toList()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paths.forEach { drawingPath ->
            val paint = Paint().apply {
                color = drawingPath.color
                strokeWidth = drawingPath.width
                style = Paint.Style.STROKE
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND

                if (drawingPath.isEraser) {
                    xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
                }
            }

            canvas.drawPath(drawingPath.path, paint)
        }

        currentPath?.let {
            val paint = Paint().apply {
                color = brushColor
                strokeWidth = brushSize
                style = Paint.Style.STROKE
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND

                if (isEraserEnabled) {
                    xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
                }
            }

            canvas.drawPath(it, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDrawingEnabled) {
            Log.d("DrawingView", "Режим рисования выключен, игнорируем событие touch")
            return false
        }

        Log.d("DrawingView", "Обработка события touch: ${event.action}, isDrawingEnabled=$isDrawingEnabled, isEraserEnabled=$isEraserEnabled")

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path().apply {
                    moveTo(x, y)
                }
                currentPoint.set(x, y)
                invalidate()
                Log.d("DrawingView", "ACTION_DOWN: начат новый путь в ($x, $y)")
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath?.let {
                    it.quadTo(
                        currentPoint.x,
                        currentPoint.y,
                        (x + currentPoint.x) / 2,
                        (y + currentPoint.y) / 2
                    )
                    currentPoint.set(x, y)
                    invalidate()
                    Log.d("DrawingView", "ACTION_MOVE: продолжение пути к ($x, $y)")
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                currentPath?.let {
                    it.lineTo(x, y)
                    val drawingPath = DrawingPath(it, brushColor, brushSize, isEraserEnabled)
                    paths.add(drawingPath)
                    onDrawingFinished?.invoke(drawingPath)
                    currentPath = null
                    invalidate()
                    Log.d("DrawingView", "ACTION_UP: путь завершен и сохранен, isEraser=$isEraserEnabled")
                }
                return true
            }
            else -> return false
        }
    }
}