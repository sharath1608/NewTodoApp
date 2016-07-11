package app.com.android.newtodoapp;

import com.google.android.gms.location.Geofence;

import java.io.Serializable;

/**
 *  Helper class to create a geofence.
 */
public class TodoGeofence implements Serializable{

    private final double mLongitude;
    private final double mLatitude;
    private final int mRadius;
    private final long mExpirationDuration;

    public String getmGeofenceId() {
        return mGeofenceId;
    }

    private final String mGeofenceId;

    public TodoGeofence(double longitude, double latitude, int radius, long expirationDuration, String id){
        mLongitude = longitude;
        mLatitude  = latitude;
        mRadius    = radius;
        mExpirationDuration = expirationDuration;
        mGeofenceId = id;
    }

    public Geofence toGeoFence(){
        return new Geofence.Builder()
                .setCircularRegion(mLatitude,mLongitude,mRadius)
                .setExpirationDuration(mExpirationDuration)
                .setRequestId(mGeofenceId)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
    }

    public double getmLongitude() {
        return mLongitude;
    }

    public double getmLatitude() {
        return mLatitude;
    }

}
