package app.com.android.newtodoapp.data;

/**
 * Created by Asus1 on 6/24/2016.
 */

import android.content.ContentValues;
import android.net.Uri;
import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.NotifyInsert;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = TodoProvider.AUTHORITY,
        packageName = "app.com.android.newtodoapp.provider",
        database = TodoDatabase.class)
public class TodoProvider {

    public static final String AUTHORITY = "app.com.android.newtodoapp.TodoProvider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path{
        String LISTS = "lists";
        String ITEMS = "items";
        String FROM_LIST = "fromList";

    }
    private static Uri buildUri(String... paths){
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path:paths){
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table=TodoDatabase.Tables.Lists)public static class Lists{

        @ContentUri(
                path = Path.LISTS,
                type = "vnd.android.cursor.dir/list",
                defaultSort = ListColumns._ID + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.LISTS);

        @InexactContentUri(
                path = Path.LISTS + "/#",
                name = "LIST_ID",
                type = "vnd.android.cursor.dir/list",
                whereColumn = ListColumns._ID,
                pathSegment = 1
        )
        public static final Uri withID(long id){
            return buildUri(Path.LISTS,String.valueOf(id));
        }
    }

    @TableEndpoint(table=TodoDatabase.Tables.Items) public static class Items{

        @ContentUri(
                path = Path.ITEMS,
                type = "vnd.android.cursor.dir/item"
        )
        public static final Uri CONTENT_URI = buildUri(Path.ITEMS);

        @InexactContentUri(
                name = "ITEMS_ID",
                path = Path.ITEMS + "/#",
                type = "vnd.android.cursor.dir/item",
                whereColumn = ItemColumns._ID,
                pathSegment =  1)
        public static final Uri withID(long id){
            return buildUri(Path.ITEMS,String.valueOf(id));
        }

        @InexactContentUri(
                name = "ITEMS_FROM_LIST",
                path = Path.ITEMS + "/" + Path.FROM_LIST + "/#",
                type = "vnd.android.cursor.dir/list",
                whereColumn = ItemColumns.List_ID,
                pathSegment = 2)
        public static final Uri fromList(long listId){
            return buildUri(Path.ITEMS,Path.FROM_LIST,String.valueOf(listId));
        }

        @NotifyInsert(paths = Path.ITEMS)
        public static Uri[] onInsert(ContentValues values){
            final long listId = values.getAsLong(ItemColumns.List_ID);
            return new Uri[]{
                    Lists.withID(listId),fromList(listId)
            };
        }
    }
}
