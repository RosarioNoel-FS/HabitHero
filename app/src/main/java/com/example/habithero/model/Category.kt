package com.example.habithero.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Category(
    val name: String = "",
    val habitList: List<String> = emptyList(),
    val iconUrl: String = ""
)