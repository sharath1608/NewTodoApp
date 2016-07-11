package app.com.android.newtodoapp.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import app.com.android.newtodoapp.MainActivity;
import app.com.android.newtodoapp.R;

/**
 * Author: Sharath Koday
 */
public class WidgetProvider extends AppWidgetProvider {

    private final String ACTION_DATA_UPDATED = "com.android.newtodoapp.DATA_UPDATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(ACTION_DATA_UPDATED.equals(intent.getAction())){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.widget_list_view);
        }
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int i=0;i<appWidgetIds.length;i++){
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            int widgetId = appWidgetIds[i];

            // Set the pending intent to navigate to the main screen when the widget header is clicked
            Intent mainIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,mainIntent,0);
            remoteViews.setOnClickPendingIntent(R.id.widget_header_layout, pendingIntent);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(mainIntent)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.widget_list_view,clickPendingIntentTemplate);

            // Set the remote adapter to let the remote service update the widget.
            remoteViews.setRemoteAdapter(R.id.widget_list_view, new Intent(context, WidgetRemoteService.class));
            remoteViews.setEmptyView(R.id.widget_list_view, R.id.widget_empty);
            appWidgetManager.updateAppWidget(widgetId,remoteViews);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }
}
