package com.example.greencart

data class Prize(
    val id: String,
    val title: String,
    val pointsRequired: Int,
    val imageResId: Int? = null
)

