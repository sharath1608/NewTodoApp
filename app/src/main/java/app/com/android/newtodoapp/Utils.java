package app.com.android.newtodoapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.util.Pair;
import com.google.android.gms.location.places.Place;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.Type;

/**
 * Author: Sharath Koday
 */
public class Utils {

    public static String convertLatLongToLocality(Context context, double latitude,double longitude) throws IOException {
        List<Address> address;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        address =  geocoder.getFromLocation(latitude,longitude,1);
        if(address.size()>0){
            return address.get(0).getAddressLine(0);
        }
        return null;
    }

    public static String getFriendlyDateString(LocalDate date){
        DateTimeFormatter fmt = DateTimeFormat.forPattern(TodoConstants.dateFormat);
        return date.toString(fmt);
    }

    public static LocalDate getDateFromString(String date){
        LocalDate convertedDate = null;
        if(!Utils.isNullOrEmpty(date)) {
            DateTimeFormatter fmt = DateTimeFormat.forPattern(TodoConstants.dateFormat);
            convertedDate = fmt.parseLocalDate(date);
        };
        return convertedDate;
    }

    public static String convertPlaceToLocality(Context context, Place place) throws IOException {
        StringBuilder localBuilder = new StringBuilder();
        String placeName = place.getName().toString();
        String pattern = "\\(.*\\)";
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(placeName);
        if(!matcher.matches()){
            localBuilder.append(placeName);
            localBuilder.append("@");
        }
        String primaryAddress = convertLatLongToLocality(context, place.getLatLng().latitude, place.getLatLng().longitude);
        localBuilder.append(primaryAddress);
        return localBuilder.toString();
    }

    public static LocalTime getStartTimeFromString(Context context,String timeString){
        if(timeString.equals("")){
            return null;
        }
        String[] timeRange =  context.getResources().getStringArray(R.array.time_range_list);
        int selection = 0;
        for(int i=0;i<timeRange.length;i++){
            if(timeString.equals(timeRange[i])){
                selection = i;
            }
        }
        return getTimePairFromRange(selection).first;
    }

    public static Pair<LocalTime,LocalTime>getTimePairFromRange(int selection){
        LocalTime startTime;
        LocalTime endTime;
        switch (selection){
            case 0: startTime = new LocalTime("06:00:00");
                endTime = new LocalTime("09:00:00");
                break;
            case 1: startTime = new LocalTime("09:00:00");
                endTime = new LocalTime("12:00:00");
                break;
            case 2: startTime = new LocalTime("12:00:00");
                endTime = new LocalTime("16:00:00");
                break;
            case 3: startTime = new LocalTime("16:00:00");
                endTime = new LocalTime("19:00:00");
                break;
            case 4: startTime = new LocalTime("19:00:00");
                endTime = new LocalTime("23:59:00");
                break;
            default:
                throw new IllegalArgumentException("Invalid selection. Valid range from 0-4.");
      }

        return Pair.create(startTime, endTime);
    }

    public static String convertTypeEtoTypeA(String upc) {
        char[] upcArray = upc.toCharArray();
        String manufCode;
        String itemCode;
        char pivot = upcArray[5];
        if (pivot == '0' || pivot == '1' || pivot == '2') {
            manufCode = upcArray[0] + upcArray[1] + upcArray[5] + "00";
            itemCode = "00" + upcArray[2] + upcArray[3] + upcArray[5];
        } else if(pivot == 3){
            manufCode = upcArray[0] + upcArray[1] + upcArray[2] + "00";
            itemCode = "000" + upcArray[3]+upcArray[4];
        } else if(pivot == 4){
            manufCode = upcArray[0] + upcArray[1] + upcArray[2] + upcArray[3] + "0";
            itemCode = "0000" + upcArray[4];
        } else{
            manufCode = "" + upcArray[0] + upcArray[1] + upcArray[2] + upcArray[3] + upcArray[4];
            itemCode =  "0000" + upcArray[5];
        }

        String upcWithoutCheck = "0" + manufCode + itemCode;
        char checkCode = computeCheckCode(upcWithoutCheck.toCharArray());
        return  upcWithoutCheck + checkCode;
    }

    // Get the type of the global location map.
    public static Type getLocationMapType(){
        return  new TypeToken<HashMap<Integer, ReminderLocation>>(){}.getType();
    }

    // Get shared preferences
    public static SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences(context.getString(R.string.TODO_SHARED_PREF),Context.MODE_PRIVATE);
    }

    // Get the global location map from shared preferences
    public static Map<Integer,ReminderLocation> getLocationMap(Context context){
        Gson gson = new Gson();
        Map<Integer,ReminderLocation> locationMap;
        SharedPreferences mSharedPreferences = Utils.getSharedPreferences(context);
        String locationMapString = mSharedPreferences.getString(context.getString(R.string.location_map_key), "");
        if(!locationMapString.equals("")){
            locationMap = gson.fromJson(locationMapString,Utils.getLocationMapType());
        }else{
            locationMap = new HashMap<>();
        }
        return locationMap;
    }

    public static boolean isNullOrEmpty(String string){
        return (string==null || string.isEmpty());
    }
    public static void writeLocationMapInPref(Context context,Map<Integer,ReminderLocation> newLocationMap){
        SharedPreferences prefs = Utils.getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String locationMapString = gson.toJson(newLocationMap);
        editor.putString(context.getString(R.string.location_map_key), locationMapString);
        editor.apply();
    }

    public static  char computeCheckCode(char[] upcString){
        char checkCode = '0';
        int sumOfeven  = 0;
        int sumOfOdd   = 0;
        for(int pos = 0; pos<upcString.length;pos++){
            if((pos+1)%2 == 0){
                sumOfeven += (upcString[pos] - 48);
            }else{
                sumOfOdd += (upcString[pos] - 48);
            }
        }
        sumOfOdd  *= 3;
        sumOfeven += sumOfOdd;
        int rem = sumOfeven%10;
        if(rem%10 > 0){
            checkCode = Character.forDigit(10-rem,10);
        }
        return  checkCode;
    }
}
