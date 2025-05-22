package com.example.imageredactor.models

import android.net.Uri

data class ImageModel(
    val id: Long,
    val uri: Uri,
    val name: String = ""
)