package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class WeatherWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetDataUpdater.updateWeatherWidget(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetDataUpdater.updateWeatherWidget(context)
    }
}
