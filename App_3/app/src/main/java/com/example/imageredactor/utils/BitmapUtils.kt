package com.example.imageredactor.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BitmapUtils {
    fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getBitmapFromFile(file: File): Bitmap? {
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Uri? {
        val filename = "PhotoEditor_${
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
        }.jpg"

        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val uri = context.contentResolver.insert(imageCollection, contentValues)

        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                    return null
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }
        }

        return uri
    }

    fun applyGrayscaleFilter(bitmap: Bitmap): Bitmap {
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, config)
        val canvas = android.graphics.Canvas(result)

        val paint = android.graphics.Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    fun applySepiaFilter(bitmap: Bitmap): Bitmap {
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, config)
        val canvas = android.graphics.Canvas(result)

        val paint = android.graphics.Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)

        val sepiaMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                1.3f, -0.3f, 1.1f, 0f, 0f,
                0.0f, 1.3f, 0.2f, 0f, 0f,
                -0.3f, -0.2f, 0.8f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        colorMatrix.postConcat(sepiaMatrix)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    fun applyInvertFilter(bitmap: Bitmap): Bitmap {
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, config)
        val canvas = android.graphics.Canvas(result)

        val paint = android.graphics.Paint()
        val colorMatrix = ColorMatrix(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    fun applyContrastFilter(bitmap: Bitmap, contrast: Float = 1.5f): Bitmap {
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, config)
        val canvas = android.graphics.Canvas(result)

        val paint = android.graphics.Paint()
        val colorMatrix = ColorMatrix().apply {
            val scale = contrast
            val translate = (-.5f * scale + .5f) * 255f

            set(floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    fun applyBrightnessFilter(bitmap: Bitmap, brightness: Float = 30f): Bitmap {
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, config)
        val canvas = android.graphics.Canvas(result)

        val paint = android.graphics.Paint()
        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                1f, 0f, 0f, 0f, brightness,
                0f, 1f, 0f, 0f, brightness,
                0f, 0f, 1f, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }
}