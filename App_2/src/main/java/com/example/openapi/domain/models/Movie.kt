package com.example.openapi.domain.models

data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val releaseDate: String,
    val rating: Float,
    val overview: String
)