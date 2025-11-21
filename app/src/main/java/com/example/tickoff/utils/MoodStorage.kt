package com.example.tickoff.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.tickoff.models.MoodEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MoodStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveMoods(moods: List<MoodEntry>) {
        val json = gson.toJson(moods)
        prefs.edit().putString("mood_list", json).apply()
    }

    fun getMoods(): MutableList<MoodEntry> {
        val json = prefs.getString("mood_list", null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
}
