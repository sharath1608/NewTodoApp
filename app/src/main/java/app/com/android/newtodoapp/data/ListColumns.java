package app.com.android.newtodoapp.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by Asus1 on 6/23/2016.
 */
public interface ListColumns {

    @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement String _ID = "_id";
    @DataType(DataType.Type.TEXT) String TITLE = "title";
    @DataType(DataType.Type.TEXT) String LOCATION = "location";
    @DataType(DataType.Type.TEXT) String DATE = "date";
    @DataType(DataType.Type.TEXT) String TIME = "time";
}
