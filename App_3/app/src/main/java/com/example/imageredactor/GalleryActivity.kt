package com.example.imageredactor

import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.imageredactor.adapters.GalleryAdapter
import com.example.imageredactor.models.ImageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GalleryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView

    private val adapter = GalleryAdapter { imageModel ->
        val resultIntent = Intent().apply {
            data = imageModel.uri
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private val pageSize = 24
    private var loading = false
    private var hasMoreImages = true
    private var offset = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        recyclerView = findViewById(R.id.gallery_recycler_view)
        progressBar = findViewById(R.id.loading_progress)
        emptyView = findViewById(R.id.empty_view)

        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!loading && hasMoreImages) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= pageSize
                    ) {
                        loadImages()
                    }
                }
            }
        })

        loadImages()
    }

    private fun loadImages() {
        loading = true
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            val images = loadImagesFromMediaStore()

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                loading = false

                if (images.isEmpty() && offset == 0) {
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.addImages(images)

                    hasMoreImages = images.size == pageSize
                    offset += pageSize
                }
            }
        }
    }

    private fun loadImagesFromMediaStore(): List<ImageModel> {
        val images = mutableListOf<ImageModel>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        val selection = null
        val selectionArgs = null
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            if (cursor.moveToPosition(offset)) {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

                var count = 0
                do {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    images.add(ImageModel(id, contentUri, name))
                    count++
                } while (cursor.moveToNext() && count < pageSize)
            }
        }

        return images
    }
}