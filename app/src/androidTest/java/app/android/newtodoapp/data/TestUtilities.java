package app.android.newtodoapp.data;

import android.content.ContentValues;
import android.os.SystemClock;

import java.sql.Timestamp;

import app.com.android.newtodoapp.data.ItemColumns;
import app.com.android.newtodoapp.data.ListColumns;

/**
 * Created by Asus1 on 6/24/2016.
 */
public class TestUtilities {

    public static ContentValues createValuesforList(){
        ContentValues cv = new ContentValues();
        Timestamp ts = new Timestamp(SystemClock.currentThreadTimeMillis());
        cv.put(ListColumns.TITLE, "This is a title");
        cv.put(ListColumns.DATE, ts.toString());
        return cv;
    }

    public static ContentValues[] createValuesforItems(int id){
        ContentValues[] values = new ContentValues[2];
        ContentValues cv1 = new ContentValues();
        cv1.put(ItemColumns.List_ID, id);
        cv1.put(ItemColumns.isDone, true);
        cv1.put(ItemColumns.todoText,"list1");
        values[0] = cv1;
        ContentValues cv2 = new ContentValues();
        cv2.put(ItemColumns.List_ID, id);
        cv2.put(ItemColumns.isDone, false);
        cv2.put(ItemColumns.todoText, "list2");
        values[1] = cv2;
        return values;
    }
}
