package com.example.profile.models

data class Post(
    val id: Int,
    val userId: Int,
    val text: String,
    val imageUrl: String? = null,
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean = false
)