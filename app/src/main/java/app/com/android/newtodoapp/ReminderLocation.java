package app.com.android.newtodoapp;


/**
 * Created by Asus1 on 7/5/2016.
 */
public class ReminderLocation {

    TodoGeofence geofence;
    boolean inGeofence;

    public ReminderLocation(TodoGeofence geofence){
        this.geofence = geofence;
    }

    public void setInGeofence(boolean inGeofence){
        this.inGeofence = inGeofence;
    }

    public TodoGeofence getGeofence(){
        return geofence;
    }
}
