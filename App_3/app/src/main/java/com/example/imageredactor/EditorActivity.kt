package com.example.imageredactor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.imageredactor.adapters.FilterAdapter
import com.example.imageredactor.editors.PhotoEditor
import com.example.imageredactor.editors.TextStyleBuilder
import com.example.imageredactor.models.TextElement
import com.example.imageredactor.utils.BitmapUtils
import com.example.imageredactor.views.PhotoEditorView
import android.graphics.Color

class EditorActivity : AppCompatActivity() {

    private lateinit var photoEditorView: PhotoEditorView
    private lateinit var photoEditor: PhotoEditor
    private lateinit var originalBitmap: Bitmap

    private val cropLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val croppedImagePath = result.data?.getStringExtra(CropActivity.RESULT_CROPPED_IMAGE_PATH)
            if (croppedImagePath != null) {
                val file = File(croppedImagePath)
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    originalBitmap = bitmap
                    photoEditorView.source.setImageBitmap(bitmap)
                    Toast.makeText(this, getString(R.string.cut), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private lateinit var btnBrush: ImageButton
    private lateinit var btnEraser: ImageButton
    private lateinit var btnText: ImageButton
    private lateinit var btnFilter: ImageButton
    private lateinit var btnCrop: ImageButton
    private lateinit var btnSave: ImageButton

    private lateinit var brushSettingsLayout: LinearLayout
    private lateinit var textSettingsLayout: LinearLayout
    private lateinit var filterRecyclerView: RecyclerView

    private lateinit var brushSizeSeekBar: SeekBar
    private lateinit var textSizeSeekBar: SeekBar
    private lateinit var textInput: EditText
    private lateinit var btnAddText: Button

    private var currentBrushColor = Color.BLACK
    private var currentTextColor = Color.BLACK

    private var editingTextElement: TextElement? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        val toolbar = findViewById<Toolbar>(R.id.editor_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val imageUriString = intent.getStringExtra("imageUri")
        if (imageUriString.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.download_e), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        val imageUri = Uri.parse(imageUriString)
        loadImage(imageUri)

        setupClickListeners()
    }

    private fun initViews() {
        photoEditorView = findViewById(R.id.photo_editor_view)

        btnBrush = findViewById(R.id.btn_brush)
        btnEraser = findViewById(R.id.btn_eraser)
        btnText = findViewById(R.id.btn_text)
        btnFilter = findViewById(R.id.btn_filter)
        btnCrop = findViewById(R.id.btn_crop)
        btnSave = findViewById(R.id.btn_save)

        brushSettingsLayout = findViewById(R.id.brush_settings_layout)
        textSettingsLayout = findViewById(R.id.text_settings_layout)
        filterRecyclerView = findViewById(R.id.filter_recycler_view)

        brushSizeSeekBar = findViewById(R.id.brush_size_seekbar)
        textSizeSeekBar = findViewById(R.id.text_size_seekbar)
        textInput = findViewById(R.id.text_input)
        btnAddText = findViewById(R.id.btn_add_text)

        findViewById<View>(R.id.color_black).setOnClickListener { setBrushColor(Color.BLACK) }
        findViewById<View>(R.id.color_red).setOnClickListener { setBrushColor(Color.RED) }
        findViewById<View>(R.id.color_green).setOnClickListener { setBrushColor(Color.GREEN) }
        findViewById<View>(R.id.color_blue).setOnClickListener { setBrushColor(Color.BLUE) }
        findViewById<View>(R.id.color_yellow).setOnClickListener { setBrushColor(Color.YELLOW) }
        findViewById<View>(R.id.color_purple).setOnClickListener { setBrushColor(Color.MAGENTA) }

        findViewById<View>(R.id.text_color_black).setOnClickListener { setTextColor(Color.BLACK) }
        findViewById<View>(R.id.text_color_white).setOnClickListener { setTextColor(Color.WHITE) }
        findViewById<View>(R.id.text_color_red).setOnClickListener { setTextColor(Color.RED) }
        findViewById<View>(R.id.text_color_green).setOnClickListener { setTextColor(Color.GREEN) }
        findViewById<View>(R.id.text_color_blue).setOnClickListener { setTextColor(Color.BLUE) }
        findViewById<View>(R.id.text_color_yellow).setOnClickListener { setTextColor(Color.YELLOW) }

        filterRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun loadImage(imageUri: Uri) {
        val bitmap = BitmapUtils.getBitmapFromUri(contentResolver, imageUri)

        if (bitmap != null) {
            originalBitmap = bitmap
            photoEditorView.source.setImageBitmap(bitmap)

            photoEditor = PhotoEditor.Builder(this, photoEditorView)
                .setPinchTextScalable(true)
                .build()

            photoEditorView.onTextDoubleClickListener = { textElement ->
                showTextEditingMode(textElement)
            }
        } else {
            Toast.makeText(this, getString(R.string.install_e), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupClickListeners() {
        btnBrush.setOnClickListener {
            hideAllSettingsPanels()
            brushSettingsLayout.visibility = View.VISIBLE

            photoEditor.setEraserMode(false)
            photoEditor.setBrushDrawingMode(true)

            Log.d("EditorActivity", "Включен режим рисования")
        }

        btnEraser.setOnClickListener {
            hideAllSettingsPanels()
            brushSettingsLayout.visibility = View.VISIBLE

            photoEditor.setEraserMode(true)

            Toast.makeText(this, getString(R.string.eraser_on), Toast.LENGTH_SHORT).show()
        }

        btnText.setOnClickListener {
            hideAllSettingsPanels()
            textSettingsLayout.visibility = View.VISIBLE

            editingTextElement = null
            textInput.text.clear()
            setupDefaultTextAddButton()

            photoEditor.setBrushDrawingMode(false)
        }

        btnFilter.setOnClickListener {
            hideAllSettingsPanels()
            setupFilterRecyclerView()
            filterRecyclerView.visibility = View.VISIBLE

            photoEditor.setBrushDrawingMode(false)
        }

        btnCrop.setOnClickListener {
            photoEditor.setBrushDrawingMode(false)
            saveImageForCrop()
        }

        btnSave.setOnClickListener {
            photoEditor.setBrushDrawingMode(false)
            saveImage()
        }

        brushSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                photoEditor.brushSize = progress.toFloat()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        setupDefaultTextAddButton()
    }

    private fun setBrushColor(color: Int) {
        currentBrushColor = color
        photoEditor.brushColor = color
    }

    private fun setTextColor(color: Int) {
        currentTextColor = color
    }

    private fun hideAllSettingsPanels() {
        brushSettingsLayout.visibility = View.GONE
        textSettingsLayout.visibility = View.GONE
        filterRecyclerView.visibility = View.GONE

        photoEditor.setBrushDrawingMode(false)
    }

    private fun setupDefaultTextAddButton() {
        btnAddText.text = "Добавить текст"
        btnAddText.setOnClickListener {
            val text = textInput.text.toString()
            if (!TextUtils.isEmpty(text)) {
                Log.d("EditorActivity", "Добавление текста: '$text'")

                val textSize = 20f + textSizeSeekBar.progress.toFloat() * 0.8f

                Log.d("EditorActivity", "Размер текста: $textSize")

                val styleBuilder = TextStyleBuilder()
                    .withTextColor(currentTextColor)
                    .withTextSize(textSize)
                    .withTextFont(Typeface.DEFAULT_BOLD)

                try {
                    photoEditor.addText(text, styleBuilder)

                    textInput.text.clear()

                    hideAllSettingsPanels()

                    Toast.makeText(
                        this,
                        getString(R.string.text_success),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {

                    Log.e("EditorActivity", "Ошибка при добавлении текста: ${e.message}")
                    e.printStackTrace()

                    Toast.makeText(this, getString(R.string.text_fail, e.message), Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.input_text), Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showTextEditingMode(textElement: TextElement) {

        hideAllSettingsPanels()
        textSettingsLayout.visibility = View.VISIBLE

        editingTextElement = textElement

        textInput.setText(textElement.text)

        currentTextColor = textElement.color

        val progress = ((textElement.size - 20f) / 0.8f).toInt().coerceIn(0, 100)
        textSizeSeekBar.progress = progress

        btnAddText.text = getString(R.string.update_text)
        btnAddText.setOnClickListener {
            val newText = textInput.text.toString()
            if (!TextUtils.isEmpty(newText)) {
                try {
                    textElement.text = newText
                    textElement.color = currentTextColor
                    textElement.size = 20f + textSizeSeekBar.progress.toFloat() * 0.8f

                    photoEditor.updateTextElement(textElement)

                    textInput.text.clear()
                    editingTextElement = null

                    hideAllSettingsPanels()

                    setupDefaultTextAddButton()

                    Toast.makeText(this, getString(R.string.update_success), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("EditorActivity", "Ошибка при обновлении текста: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(this,
                        getString(R.string.update_fail, e.message), Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.input_text), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFilterRecyclerView() {
        val scaleFactor = 0.2f
        val previewBitmap = Bitmap.createScaledBitmap(
            originalBitmap,
            (originalBitmap.width * scaleFactor).toInt(),
            (originalBitmap.height * scaleFactor).toInt(),
            false
        )

        val filters = listOf(
            FilterAdapter.FilterItem(getString(R.string.original)) { it },
            FilterAdapter.FilterItem(getString(R.string.w_b)) { BitmapUtils.applyGrayscaleFilter(it) },
            FilterAdapter.FilterItem(getString(R.string.sepia)) { BitmapUtils.applySepiaFilter(it) },
            FilterAdapter.FilterItem(getString(R.string.inversion)) { BitmapUtils.applyInvertFilter(it) },
            FilterAdapter.FilterItem(getString(R.string.contrast)) { BitmapUtils.applyContrastFilter(it) },
            FilterAdapter.FilterItem(getString(R.string.bightness)) { BitmapUtils.applyBrightnessFilter(it) }
        )

        val filterAdapter = FilterAdapter(previewBitmap, filters) { filterItem ->
            val filteredBitmap = filterItem.filterApplier(originalBitmap)
            photoEditorView.source.setImageBitmap(filteredBitmap)
        }

        filterRecyclerView.adapter = filterAdapter
    }

    private fun saveImageForCrop() {
        try {
            val config = originalBitmap.config ?: Bitmap.Config.ARGB_8888
            val imageBitmap = originalBitmap.copy(config, true)

            if (imageBitmap != null) {
                val tempFile = File(cacheDir, "temp_image_for_crop.jpg")
                try {
                    FileOutputStream(tempFile).use { out ->
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    }

                    Toast.makeText(
                        this,
                        getString(R.string.waring),
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(this, CropActivity::class.java).apply {
                        putExtra(CropActivity.EXTRA_IMAGE_PATH, tempFile.absolutePath)
                    }
                    cropLauncher.launch(intent)

                    imageBitmap.recycle()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        this,
                        getString(R.string.e_prepare_img, e.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                            getString(R.string.e_prepare_img),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                getString(R.string.error, e.message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveImage() {
        try {
            val resultBitmap = photoEditor.getResultBitmap()

            if (resultBitmap != null) {
                val savedUri = BitmapUtils.saveBitmapToGallery(this, resultBitmap)
                if (savedUri != null) {
                    Toast.makeText(
                        this,
                        getString(R.string.saved),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.save_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                resultBitmap.recycle()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.img_create_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                getString(R.string.e_save, e.message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}