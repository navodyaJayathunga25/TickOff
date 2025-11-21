package com.example.tickoff.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.tickoff.models.Habit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.*

class HabitStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val HABIT_KEY = "habit_list"

    // Save full habit list
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        prefs.edit { putString(HABIT_KEY, json) }
    }

    // Get all habits, reset only if new day
    fun getHabits(): MutableList<Habit> {
        val json = prefs.getString(HABIT_KEY, null)
        val type = object : TypeToken<MutableList<Habit>>() {}.type
        val list: MutableList<Habit> = if (json != null) gson.fromJson(json, type) else mutableListOf()

        //Tick mark refresh
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        list.forEach { habit ->
            if (habit.lastUpdatedDate != today) {
                habit.isCompleted = false
                habit.lastUpdatedDate = today
            }
        }

        return list
    }

    fun addHabit(habit: Habit) {
        val habits = getHabits()
        habits.add(habit)
        saveHabits(habits)
    }

    fun updateHabit(habit: Habit) {
        val habits = getHabits()
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habits[index] = habit
            saveHabits(habits)
        }
    }

    fun deleteHabit(habitId: String) {
        val habits = getHabits()
        saveHabits(habits.filter { it.id != habitId })
    }
}
