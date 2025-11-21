package com.example.tickoff

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.tickoff.utils.HabitStorage

class HabitWidgetProvider : AppWidgetProvider() {

    //Get data
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val storage = HabitStorage(context)
        val habits = storage.getHabits()
        val total = habits.size
        val completed = habits.count { it.isCompleted }

        //Cal percentage
        val percentage = if (total > 0) (completed * 100 / total) else 0

        //Motivational message
        val motivation = when {
            percentage == 0 -> "Let's start small today 💪"
            percentage in 1..49 -> "Good start, keep it up 🌱"
            percentage in 50..89 -> "You’re more than halfway there! 🚀"
            else -> "Amazing! You nailed it! 🎉"
        }

        //Refresh
        appWidgetIds.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_habit_progress)

            views.setTextViewText(R.id.tvWidgetPercentage, "$percentage%")
            views.setTextViewText(R.id.tvWidgetMotivation, motivation)

            // Launch app when clicked
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tvWidgetPercentage, pendingIntent)
            views.setOnClickPendingIntent(R.id.tvWidgetMotivation, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, HabitWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            val intent = Intent(context, HabitWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}
