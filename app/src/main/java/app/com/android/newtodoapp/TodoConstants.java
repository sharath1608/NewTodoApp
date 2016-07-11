package app.com.android.newtodoapp;

import com.google.android.gms.location.Geofence;

/**
 * Created by Asus1 on 6/20/2016.
 */
public class TodoConstants {

    public static final int GEOFENCE_RADIUS = 300;
    public static final long EXPIRATION_PERIOD = Geofence.NEVER_EXPIRE;
    public enum REQUEST_TYPE {ADD,MODIFY,REMOVE}
    public static final int UPC_TYPE_E = 8;
    public static final int UPC_TYPE_A = 12;
    public static final int DEFAULT_ID = 0;
    public static final int ADD = REQUEST_TYPE.ADD.ordinal();
    public static final int MODIFY = REQUEST_TYPE.MODIFY.ordinal();
    public static final int REMOVE = REQUEST_TYPE.REMOVE.ordinal();
    public static final String dateFormat = "MMM d, YYYY";
    public static final int TIME_MARGIN = 10;
}
