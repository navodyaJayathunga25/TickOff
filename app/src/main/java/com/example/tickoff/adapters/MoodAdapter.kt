package com.example.tickoff.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tickoff.R
import com.example.tickoff.models.MoodEntry

class MoodAdapter(
    private val moods: MutableList<MoodEntry>,
    private val onEdit: (MoodEntry) -> Unit,
    private val onDelete: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    //ViewHolder
    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji: TextView = itemView.findViewById(R.id.tvEmoji)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvNote: TextView = itemView.findViewById(R.id.tvNote)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditMood)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteMood)
    }

    //One row
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    //Update
    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position]

        // Bind mood data
        holder.tvEmoji.text = mood.moodEmoji
        holder.tvDate.text = mood.dateTime
        holder.tvNote.text = mood.notes

        // Edit/Delete event handlers
        holder.btnEdit.setOnClickListener { onEdit(mood) }
        holder.btnDelete.setOnClickListener { onDelete(mood) }
    }

    override fun getItemCount(): Int = moods.size
}
