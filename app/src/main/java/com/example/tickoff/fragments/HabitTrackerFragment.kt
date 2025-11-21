package com.example.tickoff.fragments

import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tickoff.HabitWidgetProvider
import com.example.tickoff.R
import com.example.tickoff.adapters.HabitAdapter
import com.example.tickoff.databinding.FragmentHabitTrackerBinding
import com.example.tickoff.models.Habit
import com.example.tickoff.utils.HabitStorage
import java.text.SimpleDateFormat
import java.util.*

class HabitTrackerFragment : Fragment(R.layout.fragment_habit_tracker) {

    private var _binding: FragmentHabitTrackerBinding? = null
    private val binding get() = _binding!!

    private lateinit var storage: HabitStorage
    private lateinit var adapter: HabitAdapter
    private var habits = mutableListOf<Habit>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set today's date dynamically
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
        val todayDisplay = dateFormat.format(Date())
        binding.tvDate.text = todayDisplay

        // Initialize storage and load habits
        storage = HabitStorage(requireContext())
        habits = storage.getHabits().toMutableList()

        // Reset daily ticks - only once per day
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        // Only reset if lastUpdatedDate < today
        habits.forEach { habit ->
            val lastDate = habit.lastUpdatedDate
            if (lastDate != todayDate) {
                habit.isCompleted = false
                habit.lastUpdatedDate = todayDate
            }
        }
        storage.saveHabits(habits) // save reset ticks


        // Setup RecyclerView and adapter
        adapter = HabitAdapter(
            habits,
            onHabitChecked = { habit ->
                //Save updated habits
                storage.saveHabits(habits)
                //Update RecyclerView efficiently
                val index = habits.indexOf(habit)
                if (index != -1) adapter.notifyItemChanged(index) // efficient update

                // Notify the widget to update
                val intent = Intent(requireContext(), HabitWidgetProvider::class.java)
                intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(requireContext())
                    .getAppWidgetIds(
                        ComponentName(
                            requireContext(),
                            HabitWidgetProvider::class.java
                        )
                    )
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                requireContext().sendBroadcast(intent)
            },
            onEditHabit = { habit ->
                showEditHabitDialog(habit)
            },
            onDeleteHabit = { habit ->
                val index = habits.indexOf(habit)
                if (index != -1) {
                    habits.removeAt(index)
                    storage.saveHabits(habits)
                    adapter.notifyItemRemoved(index)
                }
            }
        )

        binding.rvHabits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHabits.adapter = adapter
        adapter.notifyDataSetChanged() // initial refresh

        // Add habit FAB
        binding.fabAddHabit.setOnClickListener { showAddHabitDialog() }
    }

    private fun showAddHabitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_habit, null)
        val etHabitName = dialogView.findViewById<EditText>(R.id.etHabitName)
        val btnAdd = dialogView.findViewById<View>(R.id.btnAdd)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)

        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundedDialog)
            .setView(dialogView)
            .create()

        btnAdd.setOnClickListener {
            val name = etHabitName.text.toString().trim()
            if (name.isNotEmpty()) {
                val habit = Habit(name = name)
                habits.add(habit)
                storage.saveHabits(habits)
                adapter.notifyItemInserted(habits.size - 1)
                dialog.dismiss()
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_habit, null)
        val etHabitName = dialogView.findViewById<EditText>(R.id.etHabitName)
        val btnAdd = dialogView.findViewById<View>(R.id.btnAdd)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)

        etHabitName.setText(habit.name)

        val dialog = AlertDialog.Builder(requireContext(), R.style.RoundedDialog)
            .setView(dialogView)
            .create()

        btnAdd.setOnClickListener {
            val newName = etHabitName.text.toString().trim()
            if (newName.isNotEmpty()) {
                habit.name = newName
                storage.saveHabits(habits)
                val index = habits.indexOf(habit)
                if (index != -1) adapter.notifyItemChanged(index)
                dialog.dismiss()
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
