package com.example.imageredactor

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.imageredactor.utils.BitmapUtils
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView

class CropActivity : AppCompatActivity() {

    private lateinit var cropImageView: CropImageView
    private var originalBitmap: Bitmap? = null

    companion object {
        const val EXTRA_IMAGE_PATH = "image_path"
        const val RESULT_CROPPED_IMAGE_PATH = "cropped_image_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        val toolbar = findViewById<Toolbar>(R.id.crop_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        cropImageView = findViewById(R.id.crop_image_view)

        val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
        if (imagePath != null) {
            originalBitmap = BitmapUtils.getBitmapFromFile(File(imagePath))
            originalBitmap?.let {
                cropImageView.setImageBitmap(it)
            }
        } else {
            Toast.makeText(this, getString(R.string.error_saving), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.crop_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_crop_done -> {
                cropImage()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun cropImage() {
        originalBitmap?.let { bitmap ->
            val cropRect = cropImageView.getCropRect()

            if (cropRect.width() <= 0 || cropRect.height() <= 0 ||
                cropRect.right > bitmap.width || cropRect.bottom > bitmap.height) {
                Toast.makeText(this, getString(R.string.incorrect_crop), Toast.LENGTH_SHORT).show()
                return
            }

            val croppedBitmap = Bitmap.createBitmap(
                bitmap,
                cropRect.left.toInt(),
                cropRect.top.toInt(),
                cropRect.width().toInt(),
                cropRect.height().toInt()
            )

            val tempFile = File(cacheDir, "cropped_image.jpg")
            try {
                FileOutputStream(tempFile).use { out ->
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }

                val resultIntent = Intent().apply {
                    putExtra(RESULT_CROPPED_IMAGE_PATH, tempFile.absolutePath)
                }
                setResult(Activity.RESULT_OK, resultIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка при обрезке: ${e.message}", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_CANCELED)
            }

            finish()
        }
    }

    class CropImageView(context: Context, attrs: AttributeSet? = null) :
        AppCompatImageView(context, attrs) {

        private val cropRect = RectF(0f, 0f, 0f, 0f)
        private val paint = Paint().apply {
            color = 0x88000000.toInt()
            style = Paint.Style.FILL
        }
        private val borderPaint = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        private var dragStartX = 0f
        private var dragStartY = 0f
        private var isDragging = false
        private var dragCorner = 0
        private val cornerTouchRadius = 40f

        fun getCropRect(): RectF {
            val drawable = drawable ?: return RectF(0f, 0f, 0f, 0f)

            val imageWidth = drawable.intrinsicWidth.toFloat()
            val imageHeight = drawable.intrinsicHeight.toFloat()

            val viewWidth = width.toFloat()
            val viewHeight = height.toFloat()

            val scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight)

            val offsetX = (viewWidth - imageWidth * scale) / 2f
            val offsetY = (viewHeight - imageHeight * scale) / 2f

            val imageRect = RectF(
                (cropRect.left - offsetX) / scale,
                (cropRect.top - offsetY) / scale,
                (cropRect.right - offsetX) / scale,
                (cropRect.bottom - offsetY) / scale
            )

            imageRect.left = imageRect.left.coerceIn(0f, imageWidth)
            imageRect.top = imageRect.top.coerceIn(0f, imageHeight)
            imageRect.right = imageRect.right.coerceIn(0f, imageWidth)
            imageRect.bottom = imageRect.bottom.coerceIn(0f, imageHeight)

            return imageRect
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            val padding = Math.min(w, h) / 10f
            cropRect.set(
                padding,
                padding,
                w - padding,
                h - padding
            )
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            canvas.drawRect(0f, 0f, width.toFloat(), cropRect.top, paint)
            canvas.drawRect(0f, cropRect.top, cropRect.left, cropRect.bottom, paint)
            canvas.drawRect(cropRect.right, cropRect.top, width.toFloat(), cropRect.bottom, paint)
            canvas.drawRect(0f, cropRect.bottom, width.toFloat(), height.toFloat(), paint)

            canvas.drawRect(cropRect, borderPaint)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isNearCorner(x, y, cropRect.left, cropRect.top)) {
                        dragCorner = 1
                        isDragging = true
                    } else if (isNearCorner(x, y, cropRect.right, cropRect.top)) {
                        dragCorner = 2
                        isDragging = true
                    } else if (isNearCorner(x, y, cropRect.left, cropRect.bottom)) {
                        dragCorner = 3
                        isDragging = true
                    } else if (isNearCorner(x, y, cropRect.right, cropRect.bottom)) {
                        dragCorner = 4
                        isDragging = true
                    } else if (cropRect.contains(x, y)) {
                        dragCorner = 0
                        dragStartX = x
                        dragStartY = y
                        isDragging = true
                    }
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDragging) {
                        when (dragCorner) {
                            0 -> {
                                val dx = x - dragStartX
                                val dy = y - dragStartY
                                cropRect.offset(dx, dy)
                                dragStartX = x
                                dragStartY = y
                            }
                            1 -> {
                                cropRect.left = x.coerceAtMost(cropRect.right - 50)
                                cropRect.top = y.coerceAtMost(cropRect.bottom - 50)
                            }
                            2 -> {
                                cropRect.right = x.coerceAtLeast(cropRect.left + 50)
                                cropRect.top = y.coerceAtMost(cropRect.bottom - 50)
                            }
                            3 -> {
                                cropRect.left = x.coerceAtMost(cropRect.right - 50)
                                cropRect.bottom = y.coerceAtLeast(cropRect.top + 50)
                            }
                            4 -> {
                                cropRect.right = x.coerceAtLeast(cropRect.left + 50)
                                cropRect.bottom = y.coerceAtLeast(cropRect.top + 50)
                            }
                        }
                        constrainRectToView()
                        invalidate()
                        return true
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isDragging = false
                    return true
                }
            }
            return super.onTouchEvent(event)
        }

        private fun isNearCorner(x: Float, y: Float, cornerX: Float, cornerY: Float): Boolean {
            val dx = x - cornerX
            val dy = y - cornerY
            return Math.sqrt((dx * dx + dy * dy).toDouble()) < cornerTouchRadius
        }

        private fun constrainRectToView() {
            if (cropRect.left < 0) cropRect.left = 0f
            if (cropRect.top < 0) cropRect.top = 0f
            if (cropRect.right > width) cropRect.right = width.toFloat()
            if (cropRect.bottom > height) cropRect.bottom = height.toFloat()
        }

        override fun setImageBitmap(bitmap: Bitmap) {
            super.setImageBitmap(bitmap)

            post {
                val drawable = drawable ?: return@post

                val imageWidth = drawable.intrinsicWidth.toFloat()
                val imageHeight = drawable.intrinsicHeight.toFloat()

                val viewWidth = width.toFloat()
                val viewHeight = height.toFloat()

                val scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight)

                val scaledWidth = imageWidth * scale
                val scaledHeight = imageHeight * scale

                val offsetX = (viewWidth - scaledWidth) / 2f
                val offsetY = (viewHeight - scaledHeight) / 2f

                cropRect.set(
                    offsetX,
                    offsetY,
                    offsetX + scaledWidth,
                    offsetY + scaledHeight
                )

                invalidate()
            }
        }
    }
}