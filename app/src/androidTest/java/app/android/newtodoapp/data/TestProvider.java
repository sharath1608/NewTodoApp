package app.android.newtodoapp.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import junit.framework.Assert;

import net.simonvt.schematic.annotation.ContentUri;

import app.com.android.newtodoapp.data.ItemColumns;
import app.com.android.newtodoapp.data.ListColumns;
import app.com.android.newtodoapp.data.TodoProvider;

/**
 * Created by Asus1 on 6/24/2016.
 */
public class TestProvider extends AndroidTestCase{

    public void testInsertContentValues(){
        ContentValues cv = TestUtilities.createValuesforList();
        Uri resulUri = getContext().getContentResolver().insert(TodoProvider.Lists.CONTENT_URI, cv);

        if(resulUri == null){
            Log.e(getClass().getSimpleName(),"Error inserting list");
        }
        int id = (int)ContentUris.parseId(resulUri);
        ContentValues[] itemsCv = TestUtilities.createValuesforItems(id);
        for(ContentValues contentValues:itemsCv) {
           Uri uri =  getContext().getContentResolver().insert(TodoProvider.Items.CONTENT_URI, contentValues);
           if(uri==null){
               Log.e(getClass().getSimpleName(),"Error inserting items");
           }
        }
    }
    public void testEditItemList(){
        int id = 1;
        String[] LIST_PROJECTION = {ListColumns._ID,
                ListColumns.DATE,
                ListColumns.TITLE};
        String selection_list = ListColumns._ID + "=?";
        String[] selection_args_list = {String.valueOf(id)};
        Cursor cursor = getContext().getContentResolver().query(TodoProvider.Lists.withID(0), LIST_PROJECTION, null, null, null);
        assertTrue("Empty Cursor returned", cursor.moveToFirst());
        int columnIndex = cursor.getColumnIndexOrThrow(ListColumns._ID);
        int listid  = Integer.parseInt(cursor.getString(columnIndex));
        cursor.close();
        String[] ITEMS_PROJECTION = {ItemColumns.todoText, ItemColumns.isDone};
        String selection_items = ItemColumns.List_ID;
        String[] selection_items_args = {String.valueOf(listid)};
        Cursor cursor1 = getContext().getContentResolver().query(TodoProvider.Items.withID(listid),ITEMS_PROJECTION,selection_items,selection_items_args,null);
        assertTrue("Empty Cursor returned", cursor.moveToFirst());
    }

    public void testGetOneQuery(){
        testInsertContentValues();
        Cursor cursor = getContext().getContentResolver().query(TodoProvider.Lists.CONTENT_URI,null,null,null,null);
        if(cursor.moveToFirst()){
            do{

            }while(cursor.moveToNext());
        }
    }
}
