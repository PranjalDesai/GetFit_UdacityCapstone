package com.pranjaldesai.getfit;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;

/**
 * Implementation of App Widget functionality.
 */
public class TotalPushupsWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                String result, int appWidgetId) {

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.total_pushups_widget);

        if(result!=null && !result.isEmpty()){
            views.setTextViewText(R.id.widget_total_push_up, result);
            views.setOnClickPendingIntent(R.id.widget_pushup_view, pendingIntent);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        updateWidget(context);
    }

    private void updateWidget(Context context){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String oldPushup= sharedPreferences.getString(
                context.getString(R.string.widget_pushup),null);
        if(oldPushup!=null){
            PushUpWidgetService.startActionPushUp(context, oldPushup);
        } else {
            PushUpWidgetService.startActionPushUp(context, "0");
        }
    }

    public static void updatePushUpsWidgets(Context context, AppWidgetManager appWidgetManager,
                                               String result, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, result, appWidgetId);
        }
    }
    @Override
    public void onEnabled(Context context) {
        updateWidget(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

