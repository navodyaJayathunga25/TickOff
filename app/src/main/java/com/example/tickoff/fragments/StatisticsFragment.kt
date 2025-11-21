package com.example.tickoff.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tickoff.R
import com.example.tickoff.databinding.FragmentStatisticsBinding
import com.example.tickoff.utils.HabitStorage
import com.example.tickoff.utils.MoodStorage
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.*

class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var habitStorage: HabitStorage
    private lateinit var moodStorage: MoodStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        habitStorage = HabitStorage(requireContext())
        moodStorage = MoodStorage(requireContext())

        showTodayOverview()
        showMoodTrendChart()
    }

    private fun showTodayOverview() {
        val habits = habitStorage.getHabits()
        val total = habits.size
        val completed = habits.count { it.isCompleted }
        val percent = if (total > 0) completed * 100 / total else 0
        binding.tvHabitPercent.text = "Habit Completion: $percent%"

        val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val todayMoods = moodStorage.getMoods().filter { it.dateTime.startsWith(today) }
        val averageMood = if (todayMoods.isNotEmpty()) {
            val avg: Double = todayMoods.map { it.moodLevel }.average() // avg is Double
            when {
                avg >= 4.0 -> "😄"
                avg >= 3.0 -> "😊"
                avg >= 2.0 -> "😐"
                else -> "☹️"
            }
        } else "No mood data"

        binding.tvAverageMood.text = "Average Mood: $averageMood"
    }

    private fun showMoodTrendChart() {
        val moods = moodStorage.getMoods()
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val entries = mutableListOf<Entry>()
        val dateLabels = mutableListOf<String>()

        // Get last 7 days
        for (i: Int in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date: String = sdf.format(calendar.time)
            dateLabels.add(date.substring(0, 6))

            val dailyMoods = moods.filter { mood -> mood.dateTime.startsWith(date) }
            val avgMood: Float = if (dailyMoods.isNotEmpty()) {
                dailyMoods.map { mood -> mood.moodLevel }.average().toFloat()
            } else 0f
            entries.add(Entry((6 - i).toFloat(), avgMood))
        }


        val dataSet = LineDataSet(entries, "Mood Level (1-5)")
        dataSet.color = Color.parseColor("#9C27B0")
        dataSet.circleRadius = 5f
        dataSet.setCircleColor(Color.parseColor("#4A148C"))
        dataSet.lineWidth = 2f
        dataSet.valueTextColor = Color.BLACK
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData
        binding.lineChart.axisRight.isEnabled = false
        binding.lineChart.axisLeft.axisMinimum = 0f
        binding.lineChart.axisLeft.axisMaximum = 5f
        binding.lineChart.description.text = ""
        binding.lineChart.legend.isEnabled = false

        val xAxis = binding.lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(dateLabels)

        binding.lineChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
