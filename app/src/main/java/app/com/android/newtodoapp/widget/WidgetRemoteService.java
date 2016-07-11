package app.com.android.newtodoapp.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import java.util.ArrayList;
import java.util.List;
import app.com.android.newtodoapp.R;
import app.com.android.newtodoapp.data.ListColumns;
import app.com.android.newtodoapp.data.TodoProvider;

/**
 * Author:Sharath Koday
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetRemoteService extends RemoteViewsService{

    private Cursor cursorData;
    List<WidgetItem> widgetItems;
    static final String CURRENT_FRAGMENT = "currentFragment";

    private String mTitle;
    private String mDate;
    private String mLocation;
    private String mBlank;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                widgetItems = new ArrayList<>();
                cursorData = getContentResolver().query(
                        TodoProvider.Lists.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);

                if (cursorData != null && cursorData.moveToFirst()) {
                    do {
                        WidgetItem newWidgetItem = new WidgetItem();
                        newWidgetItem.setTitle(cursorData.getString(cursorData.getColumnIndexOrThrow(ListColumns.TITLE)));
                        newWidgetItem.setLocation(cursorData.getString(cursorData.getColumnIndexOrThrow(ListColumns.LOCATION)));
                        newWidgetItem.setId(cursorData.getString(cursorData.getColumnIndexOrThrow(ListColumns._ID)));
                        newWidgetItem.setDate(cursorData.getString(cursorData.getColumnIndexOrThrow(ListColumns.DATE)));
                        widgetItems.add(newWidgetItem);
                    } while (cursorData.moveToNext());
                }
            }

            @Override
            public void onDestroy() {
                if(cursorData!=null){
                    cursorData.close();
                }
            }

            @Override
            public int getCount() {
                return widgetItems.size();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                WidgetItem currentItem = widgetItems.get(position);
                if(position == AdapterView.INVALID_POSITION || currentItem == null){
                    return null;
                }
                mBlank = getString(R.string.not_specified);
                mTitle = currentItem.getTitle().equals("")? mBlank:currentItem.getTitle();
                mDate  = currentItem.getDate().equals("")? mBlank:currentItem.getDate();
                mLocation  = currentItem.getLocation().equals("")? mBlank:currentItem.getLocation();
                RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.widget_item_layout);
                remoteViews.setTextViewText(R.id.widget_item_title,mTitle);
                remoteViews.setTextViewText(R.id.widget_date_tv,mDate);
                remoteViews.setTextViewText(R.id.widget_loc_tv,mLocation);
                Intent fillInIntent = new Intent();
                int listId = Integer.parseInt(currentItem.getId());
                fillInIntent.putExtra(CURRENT_FRAGMENT, listId);
                remoteViews.setOnClickFillInIntent(R.id.widget_list_view_item, fillInIntent);
                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.id.widget_list_view_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (widgetItems.get(position) != null) {
                    return Long.parseLong(widgetItems.get(position).getId());
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

        };
    }
}
