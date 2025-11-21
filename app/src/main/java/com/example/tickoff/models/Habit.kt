package com.example.tickoff.models

import java.text.SimpleDateFormat
import java.util.UUID
import java.util.Date
import java.util.Locale

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var isCompleted: Boolean = false,
    var lastUpdatedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
)





