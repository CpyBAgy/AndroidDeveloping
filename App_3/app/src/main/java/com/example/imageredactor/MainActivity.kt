package com.example.imageredactor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.imageredactor.utils.PermissionUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var btnGallery: Button
    private lateinit var btnCamera: Button

    private var currentPhotoUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                startEditorWithImage(uri)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                startEditorWithImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnGallery = findViewById(R.id.btn_gallery)
        btnCamera = findViewById(R.id.btn_camera)

        if (!PermissionUtils.hasRequiredPermissions(this)) {
            PermissionUtils.requestPermissions(this)
        }

        btnGallery.setOnClickListener {
            if (PermissionUtils.hasRequiredPermissions(this)) {
                openGallery()
            } else {
                Toast.makeText(this, getString(R.string.permissions), Toast.LENGTH_SHORT).show()
                PermissionUtils.requestPermissions(this)
            }
        }

        btnCamera.setOnClickListener {
            if (PermissionUtils.hasRequiredPermissions(this)) {
                openCamera()
            } else {
                Toast.makeText(this, getString(R.string.permissions), Toast.LENGTH_SHORT).show()
                PermissionUtils.requestPermissions(this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PermissionUtils.PERMISSIONS_REQUEST_CODE) {
            if (PermissionUtils.hasRequiredPermissions(this)) {
                Toast.makeText(this, getString(R.string.nice_permissions), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.permissions), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(this, GalleryActivity::class.java)
        galleryLauncher.launch(intent)
    }

    private fun openCamera() {
        val photoFile = createImageFile()

        currentPhotoUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            photoFile
        )

        currentPhotoUri?.let {
            cameraLauncher.launch(it)
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"

        val storageDir = getExternalFilesDir(null)

        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }

    private fun startEditorWithImage(imageUri: Uri) {
        val intent = Intent(this, EditorActivity::class.java).apply {
            putExtra("imageUri", imageUri.toString())
        }
        startActivity(intent)
    }
}