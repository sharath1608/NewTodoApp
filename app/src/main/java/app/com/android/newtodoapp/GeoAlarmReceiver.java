package app.com.android.newtodoapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import java.util.Map;

/**
 * Author : Sharath Koday
 */
public class GeoAlarmReceiver extends BroadcastReceiver{

    private Map<Integer,ReminderLocation> globalGeoMap;
    Context mContext;
    int mAlarmId;
    private String LOGTAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mAlarmId = intent.getIntExtra(context.getString(R.string.alarm_id),0);
        globalGeoMap = Utils.getLocationMap(context);
        ReminderLocation location = globalGeoMap.get(mAlarmId);
        if(location == null){
            sendNotification(context,context.getString(R.string.time_reminder));
        }else {
            if (location.inGeofence) {
                sendNotification(context,context.getString(R.string.location_reminder));
            }
        }
    }

    private void sendNotification(Context context,String transitionString){
        Intent notificationIntent = new Intent(context,DetailActivity.class);
        notificationIntent.putExtra(context.getString(R.string.bundle_key),mAlarmId);
        notificationIntent.putExtra(context.getString(R.string.mode), TodoConstants.REQUEST_TYPE.MODIFY.ordinal());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(DetailActivity.class).addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_event_note_white_18dp)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_event_note_black_24dp))
                .setColor(context.getResources().getColor(R.color.vanilla_white))
                .setContentTitle(transitionString)
                .setContentText(context.getString(R.string.notify_message))
                .setContentIntent(notificationPendingIntent);

        builder.setAutoCancel(true);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(mAlarmId, builder.build());
    }
}
