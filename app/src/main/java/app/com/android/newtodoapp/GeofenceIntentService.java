package app.com.android.newtodoapp;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.gson.Gson;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import java.util.Map;

/**
 * Author : Sharath Koday
 * This service handles the result when a geofence transition occurs.
 */
public class GeofenceIntentService extends IntentService {


    private final String LOG_TAG = getClass().getSimpleName();
    private SharedPreferences mSharedPreferences;
    private int mLocationId;
    String mStartTimeString;
    String mEndTimeString;
    String mTargetDateString;

    public GeofenceIntentService(String name) {
        super(name);
    }


    public GeofenceIntentService(){
        super("GeofenceIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        String geoTransitionString = "";
        Gson gson = new Gson();
        mLocationId = intent.getIntExtra(getString(R.string.location_id_key), 0);
        mSharedPreferences = getSharedPreferences(getString(R.string.TODO_SHARED_PREF), MODE_PRIVATE);
        mStartTimeString = mSharedPreferences.getString(mLocationId + getString(R.string.start_key), "");
        mEndTimeString = mSharedPreferences.getString(mLocationId + getString(R.string.end_key),"");
        mTargetDateString = mSharedPreferences.getString(mLocationId + getString(R.string.date_key),"");

        if(geofencingEvent.hasError()){
            Log.e(LOG_TAG,"Error receiving geofence event");
        }

        int transitionType = geofencingEvent.getGeofenceTransition();
        if(transitionType == Geofence.GEOFENCE_TRANSITION_ENTER ||
                transitionType == Geofence.GEOFENCE_TRANSITION_DWELL){
            geoTransitionString = getString(R.string.location_reminder);
        }else if(transitionType == Geofence.GEOFENCE_TRANSITION_EXIT){
            Map<Integer,ReminderLocation> locationMap = Utils.getLocationMap(getApplicationContext());
            if(locationMap.size()>0) {
                ReminderLocation location = locationMap.get(mLocationId);
                location.setInGeofence(false);
                locationMap.put(mLocationId, location);
                Utils.writeLocationMapInPref(getApplicationContext(), locationMap);
            }
        }

        // If time is not specified, fire a notification immediately.
        if(mStartTimeString.equals("")){
            sendNotification(geoTransitionString);
        }else{
            if(isUnderTimeLimit()) {
                sendNotification(geoTransitionString);
            }else{
                Map<Integer,ReminderLocation> locationMap = Utils.getLocationMap(getApplicationContext());
                if(locationMap.size()>0) {
                    ReminderLocation location = locationMap.get(mLocationId);
                    location.setInGeofence(true);
                    locationMap.put(mLocationId, location);
                    Utils.writeLocationMapInPref(getApplicationContext(), locationMap);
                }
            }
        }
    }

    private void sendNotification(String transitionString){
        Intent notificationIntent = new Intent(getApplicationContext(),DetailActivity.class);
        notificationIntent.putExtra(getString(R.string.bundle_key),mLocationId);
        notificationIntent.putExtra(getString(R.string.mode), TodoConstants.REQUEST_TYPE.MODIFY.ordinal());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(DetailActivity.class).addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_event_note_white_18dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_event_note_black_24dp))
                .setColor(getResources().getColor(R.color.vanilla_white))
                .setContentTitle(transitionString)
                .setContentText(getString(R.string.notify_message))
                .setContentIntent(notificationPendingIntent);

        builder.setAutoCancel(true);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(mLocationId, builder.build());
    }

    // This method checks if the time criteria set by the user is met in the given location. If met then trigger the notification.
    // If range is specified, check the higher and lower range. If a specific time was chosen, use error margin to define the ceiling and floor values.
    private boolean isUnderTimeLimit(){
        boolean isUnderLimit = false;
        LocalTime startTime = new LocalTime(mStartTimeString);
        LocalDate targetDate = new LocalDate(mTargetDateString, DateTimeZone.getDefault());
        if(targetDate.isEqual(LocalDate.now()))
            if(!mEndTimeString.equals("")) {
                LocalTime endTime = new LocalTime(mEndTimeString);
                if(startTime.isBefore(LocalTime.now()) &&
                        endTime.isAfter(LocalTime.now())){
                    isUnderLimit = true;
                }
            }else{
                LocalTime floorTime = startTime.plusMinutes(TodoConstants.TIME_MARGIN);
                LocalTime ceilingTime = startTime.minusMinutes(TodoConstants.TIME_MARGIN);
                if(floorTime.isBefore((LocalTime.now())) &&
                        ceilingTime.isAfter(LocalTime.now())){
                    isUnderLimit = true;
                }
            }
        return isUnderLimit;
    }

}
