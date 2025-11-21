package com.example.tickoff.models

import java.util.Date
import java.util.UUID

data class MoodEntry(

    val id: String = UUID.randomUUID().toString(),
    val moodEmoji: String,
    val notes: String = "",
    val dateTime: String = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).
    format(Date()),
    val moodLevel: Int = 3 // 1 = Bad, 2 = Neutral, 3 = Happy, 4 = Excited, 5 = Very Happy
)

