package app.com.android.newtodoapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.ExecOnCreate;
import net.simonvt.schematic.annotation.IfNotExists;
import net.simonvt.schematic.annotation.OnConfigure;
import net.simonvt.schematic.annotation.OnCreate;
import net.simonvt.schematic.annotation.OnUpgrade;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by Asus1 on 6/23/2016.
 */

@Database(version = TodoDatabase.VERSION,
        packageName = "app.com.android.newtodoapp.provider")
public class TodoDatabase {

    public static final int VERSION = 1;
    private TodoDatabase(){

    }

    public static class Tables{
        @Table(ListColumns.class) @IfNotExists public static final String Lists = "lists";
        @Table(ItemColumns.class) public static final String Items = "items";
    }

    @OnCreate public static void OnCreate(Context context,SQLiteDatabase db){

    }

    @OnUpgrade public static void onUpgrade(Context context,SQLiteDatabase db,int oldVersion, int newVersion){

    }

    @OnConfigure public static void onConfigure(SQLiteDatabase db){

    }

    @ExecOnCreate public static final String Exec_on_create = "SELECT * FROM " + Tables.Items;
}
