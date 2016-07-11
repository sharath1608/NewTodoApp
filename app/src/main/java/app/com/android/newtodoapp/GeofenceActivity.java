package app.com.android.newtodoapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: Sharath Koday
 */
public class GeofenceActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private Map<Integer,ReminderLocation> locationIdMap;
    List<Geofence> mGeofenceList;
    private int mRequestType;
    private String LOG_TAG = getClass().getSimpleName();
    ReminderLocation mPassedLocation;
    TodoGeofence mGeofence;
    private int mListId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRequestType = getIntent().getIntExtra(getString(R.string.location_request_key), 0);
        if(mRequestType != TodoConstants.REMOVE) {
            mGeofence = (TodoGeofence) getIntent().getSerializableExtra(getString(R.string.location_obj_key));
        }
        mListId = getIntent().getIntExtra(getString(R.string.location_id_key),0);
        mPassedLocation = new ReminderLocation(mGeofence);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationIdMap = Utils.getLocationMap(this);
        mGoogleApiClient.connect();
    }

    public ReminderLocation getExistingLocation(int id){
        Map<Integer,ReminderLocation> locationMap = Utils.getLocationMap(getApplicationContext());
        return locationMap.get(id);
    }

    @Override
    public void onConnected(Bundle bundle) {
        PendingIntent transitionIntent = getGeofenceIntent(mListId);
        mGeofenceList = new ArrayList<>();

        // If the mode is modify/remove cancel the geofence and update the map.
        if (mRequestType == TodoConstants.REMOVE ||
                mRequestType == TodoConstants.MODIFY) {
            ReminderLocation location = getExistingLocation(mListId);
            if(location == null){
                Log.e(LOG_TAG,"location fetch error for id "+ mListId);
            }else {
                final String geoFenceId = location.getGeofence().getmGeofenceId();
                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, new ArrayList<String>() {{
                    add(geoFenceId);
                }});
                locationIdMap.remove(mListId);
            }
        }

        // If the mode is add/modify, set the geofence and update the locationMap.
        if (mRequestType == TodoConstants.ADD||
                mRequestType == TodoConstants.MODIFY) {
            mGeofenceList.add(mGeofence.toGeoFence());
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, mGeofenceList, transitionIntent);
            locationIdMap.put(mListId, mPassedLocation);
        }
        Utils.writeLocationMapInPref(getApplicationContext(),locationIdMap);
        finish();
    }

    private PendingIntent getGeofenceIntent(int id){
        Intent intent = new Intent(this,GeofenceIntentService.class);
        intent.setAction(getString(R.string.dummy_intent)+id);
        intent.putExtra(getString(R.string.location_id_key),id);
        return  PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();
        Toast.makeText(this,getString(R.string.google_play_error),Toast.LENGTH_SHORT).show();
        Log.e(getClass().getSimpleName(), getString(R.string.google_play_error) + " with errorcode:" + errorCode);
    }
}
