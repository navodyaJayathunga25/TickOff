package com.example.tickoff.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tickoff.R
import com.example.tickoff.models.Habit
import java.text.SimpleDateFormat
import java.util.*

class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val onHabitChecked: (Habit) -> Unit,
    private val onEditHabit: (Habit) -> Unit,
    private val onDeleteHabit: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    //ViewHolder
    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbCompleted: CheckBox = itemView.findViewById(R.id.cbCompleted)
        val tvHabitName: TextView = itemView.findViewById(R.id.tvHabitName)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val layoutHabitItem: View = itemView.findViewById(R.id.layoutHabitItem)
    }

    //One row creation
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    //Update
    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.tvHabitName.text = habit.name

        //Old tick do not carry
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (habit.lastUpdatedDate != today) {
            habit.isCompleted = false
            habit.lastUpdatedDate = today
        }

        // Remove previous listener before setting isChecked
        holder.cbCompleted.setOnCheckedChangeListener(null)
        holder.cbCompleted.isChecked = habit.isCompleted

        // Set background based on tick
        holder.layoutHabitItem.setBackgroundResource(
            if (habit.isCompleted) R.drawable.bottom_shadow else android.R.color.white
        )

        // Attach listener safely
        holder.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
            habit.isCompleted = isChecked
            habit.lastUpdatedDate = today
            onHabitChecked(habit)

            // Update background
            holder.layoutHabitItem.setBackgroundResource(
                if (isChecked) R.drawable.bottom_shadow else android.R.color.white
            )
        }

        // Edit and Delete buttons
        holder.btnEdit.setOnClickListener { onEditHabit(habit) }
        holder.btnDelete.setOnClickListener { onDeleteHabit(habit) }
    }

    override fun getItemCount() = habits.size
}
