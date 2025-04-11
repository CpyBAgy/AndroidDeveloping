package com.example.profile.models

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val avatarUrl: String,
    val followersCount: Int,
    val followingCount: Int,
    val postsCount: Int,
    val isFollowing: Boolean = false,
    val additionalInfo: String? = null
)