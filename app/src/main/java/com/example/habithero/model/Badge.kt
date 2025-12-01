package com.example.habithero.model

data class Badge(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "", // Changed from imageName
    val categoryKey: String = "",
    val sortOrder: Int = 0,
    val criteria: Map<String, Int> = emptyMap()
)
