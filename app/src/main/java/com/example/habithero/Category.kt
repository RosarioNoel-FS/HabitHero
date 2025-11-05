package com.example.habithero

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Category(
    val name: String = "",
    val habitList: List<String> = emptyList(),
    val iconUrl: String = ""
)
