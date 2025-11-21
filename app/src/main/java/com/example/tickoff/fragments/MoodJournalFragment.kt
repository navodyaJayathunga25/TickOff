package com.example.tickoff.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tickoff.R
import com.example.tickoff.adapters.MoodAdapter
import com.example.tickoff.databinding.FragmentMoodJournalBinding
import com.example.tickoff.models.MoodEntry
import com.example.tickoff.utils.MoodStorage
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt

class MoodJournalFragment : Fragment(R.layout.fragment_mood_journal) {

    private var _binding: FragmentMoodJournalBinding? = null
    private val binding get() = _binding!!

    private lateinit var storage: MoodStorage
    private lateinit var adapter: MoodAdapter
    private var moods = mutableListOf<MoodEntry>()        // Full stored list
    private var displayedMoods = mutableListOf<MoodEntry>() // List currently shown in RecyclerView

    private var selectedMood = "" // Stores both emoji + text

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMoodJournalBinding.bind(view)

        val sdf = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
        binding.tvMoodDate.text = sdf.format(Date())

        storage = MoodStorage(requireContext())
        moods = storage.getMoods()

        setupRecyclerView()
        setupFab()
        setupViewPreviousButton()
    }

    private fun setupRecyclerView() {
        // Initialize displayedMoods with today’s moods
        displayedMoods.clear()
        displayedMoods.addAll(moods.filter { isToday(it.dateTime) })

        adapter = MoodAdapter(
            displayedMoods,
            onEdit = { mood -> showEditMoodDialog(mood) },
            onDelete = { mood -> deleteMood(mood) }
        )
        binding.rvMoods.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMoods.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddMood.setOnClickListener {
            showAddMoodDialog()
        }
    }

    private fun setupViewPreviousButton() {
        binding.btnViewPrevious.setOnClickListener {
            if (binding.btnViewPrevious.text == "View Previous Moods") {
                showAllMoods()
                binding.btnViewPrevious.text = "Show Today Only"
                binding.tvMoodIntro.text = "All Moods"
            } else {
                showTodayMoodsOnly()
                binding.btnViewPrevious.text = "View Previous Moods"
                binding.tvMoodIntro.text = getString(R.string.today_i_am)
            }
        }
    }

    private fun isToday(dateTime: String): Boolean {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val today = sdf.format(Date())
        return dateTime.startsWith(today)
    }

    private fun showTodayMoodsOnly() {
        displayedMoods.clear()
        displayedMoods.addAll(moods.filter { isToday(it.dateTime) })
        adapter.notifyDataSetChanged()
    }

    private fun showAllMoods() {
        displayedMoods.clear()
        displayedMoods.addAll(moods)
        adapter.notifyDataSetChanged()
    }

    private fun showAddMoodDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_mood, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val etNote = dialogView.findViewById<EditText>(R.id.etMoodNote)
        val saveButton = dialogView.findViewById<Button>(R.id.btnSaveMood)

        val moodButtons = listOf(
            dialogView.findViewById<Button>(R.id.btnHappy) to "😊 Happy",
            dialogView.findViewById<Button>(R.id.btnSad) to "😢 Sad",
            dialogView.findViewById<Button>(R.id.btnAngry) to "😡 Angry",
            dialogView.findViewById<Button>(R.id.btnExcited) to "🤩 Excited",
            dialogView.findViewById<Button>(R.id.btnNeutral) to "😐 Neutral",
            dialogView.findViewById<Button>(R.id.btnBad) to "☹️ Bad"
        )

        moodButtons.forEach { (button, moodText) ->
            button.setOnClickListener {
                selectedMood = moodText
                moodButtons.forEach { (b, _) -> b.setBackgroundColor(Color.TRANSPARENT) }
                button.setBackgroundColor("#DDDDDD".toColorInt())
            }
        }

        saveButton.setOnClickListener {
            if (selectedMood.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a mood", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val currentDate = sdf.format(Date())
            val note = etNote.text.toString().trim()

            val entry = MoodEntry(
                dateTime = currentDate,
                moodEmoji = selectedMood,
                notes = note
            )

            // Add to full list
            moods.add(entry)
            storage.saveMoods(moods)

            // Add to displayed list based on current filter
            if (binding.btnViewPrevious.text == "Show Today Only") {
                if (isToday(entry.dateTime)) {
                    displayedMoods.add(entry)
                    adapter.notifyItemInserted(displayedMoods.size - 1)
                }
            } else {
                displayedMoods.add(entry)
                adapter.notifyItemInserted(displayedMoods.size - 1)
            }

            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditMoodDialog(mood: MoodEntry) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_mood, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val etNote = dialogView.findViewById<EditText>(R.id.etMoodNote)
        val saveButton = dialogView.findViewById<Button>(R.id.btnSaveMood)
        val cancelButton = dialogView.findViewById<Button>(R.id.btnCancel)

        etNote.setText(mood.notes)

        val moodButtons = listOf(
            dialogView.findViewById<Button>(R.id.btnHappy) to "😊 Happy",
            dialogView.findViewById<Button>(R.id.btnSad) to "😢 Sad",
            dialogView.findViewById<Button>(R.id.btnAngry) to "😡 Angry",
            dialogView.findViewById<Button>(R.id.btnExcited) to "🤩 Excited",
            dialogView.findViewById<Button>(R.id.btnNeutral) to "😐 Neutral",
            dialogView.findViewById<Button>(R.id.btnBad) to "☹️ Bad"
        )

        selectedMood = mood.moodEmoji

        moodButtons.forEach { (button, moodText) ->
            if (moodText == selectedMood) {
                button.setBackgroundColor("#DDDDDD".toColorInt())
            }
            button.setOnClickListener {
                selectedMood = moodText
                moodButtons.forEach { (b, _) -> b.setBackgroundColor(Color.TRANSPARENT) }
                button.setBackgroundColor("#DDDDDD".toColorInt())
            }
        }

        saveButton.setOnClickListener {
            if (selectedMood.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a mood", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val note = etNote.text.toString().trim()
            val index = moods.indexOf(mood)
            if (index != -1) {
                moods[index] = mood.copy(moodEmoji = selectedMood, notes = note)
                storage.saveMoods(moods)

                // Update displayed list
                val displayIndex = displayedMoods.indexOf(mood)
                if (displayIndex != -1) {
                    displayedMoods[displayIndex] = moods[index]
                    adapter.notifyItemChanged(displayIndex)
                }
            }
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteMood(mood: MoodEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Yes") { _, _ ->
                val index = moods.indexOf(mood)
                if (index != -1) {
                    moods.removeAt(index)
                    storage.saveMoods(moods)

                    val displayIndex = displayedMoods.indexOf(mood)
                    if (displayIndex != -1) {
                        displayedMoods.removeAt(displayIndex)
                        adapter.notifyItemRemoved(displayIndex)
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
