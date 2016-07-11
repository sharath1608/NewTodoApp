package app.com.android.newtodoapp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Asus1 on 7/7/2016.
 */
public class GeoFenceBootReceiver extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mGoogleApiClient;
    private Map<Integer,ReminderLocation> globalGeoMap;
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        globalGeoMap = Utils.getLocationMap(context);
        mGoogleApiClient.connect();
    }

    private PendingIntent getGeofenceIntent(int id){
        Intent newIntent = new Intent(mContext,GeofenceIntentService.class);
        newIntent.setAction("dummy action to seperate the intents" + id);
        newIntent.putExtra(mContext.getString(R.string.location_id_key), id);
        PendingIntent mGeoPendingIntent = PendingIntent.getService(mContext,0,newIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeoPendingIntent;
    }

    @Override
    public void onConnected(Bundle bundle) {
        List<Geofence> mGeofenceList;
        for(int id:globalGeoMap.keySet()){
            PendingIntent transitionIntent = getGeofenceIntent(id);
            mGeofenceList = new ArrayList<>();
            TodoGeofence todogeofence = globalGeoMap.get(id).getGeofence();
            mGeofenceList.add(todogeofence.toGeoFence());
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, mGeofenceList, transitionIntent);
            Log.v(getClass().getSimpleName(),"Added geofence :"+id + " after reboot");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
