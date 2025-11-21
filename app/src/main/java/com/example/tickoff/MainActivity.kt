package com.example.tickoff

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.tickoff.databinding.ActivityMainBinding
import com.example.tickoff.fragments.HabitTrackerFragment
import com.example.tickoff.fragments.MoodJournalFragment
import com.example.tickoff.fragments.SettingsFragment
import com.example.tickoff.fragments.StatisticsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Default fragment
        replaceFragment(HabitTrackerFragment())

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.habitTrackerFragment -> replaceFragment(HabitTrackerFragment())
                R.id.moodJournalFragment -> replaceFragment(MoodJournalFragment())
                R.id.statisticsFragment -> replaceFragment(StatisticsFragment())
                R.id.settingsFragment -> replaceFragment(SettingsFragment())
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
