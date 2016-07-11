package app.com.android.newtodoapp.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;

/**
 * Created by Asus1 on 6/23/2016.
 */
public interface ItemColumns {

    @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement String _ID = " _id";
    @DataType(DataType.Type.INTEGER) @References(table = TodoDatabase.Tables.Lists, column= ListColumns._ID) String List_ID = "listId";
    @DataType(DataType.Type.TEXT)String todoText = "todoText";
    @DataType(DataType.Type.TEXT) String isDone = "isDone";
}

