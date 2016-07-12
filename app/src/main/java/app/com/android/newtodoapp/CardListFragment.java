package app.com.android.newtodoapp;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.github.clans.fab.FloatingActionButton;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import app.com.android.newtodoapp.data.ItemColumns;
import app.com.android.newtodoapp.data.ListColumns;
import app.com.android.newtodoapp.data.TodoProvider;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * Author : Sharath Koday
 */

public class CardListFragment extends Fragment implements LoaderManager.LoaderCallbacks {

    CardArrayAdapter mCardAdapter;
    private final int LIST_ALL = 1;
    private List<TodoItem> rowItemList;
    private String[] LIST_PROJECTION = {ListColumns._ID, ListColumns.TITLE, ListColumns.LOCATION, ListColumns.DATE, ListColumns.TIME};
    Map<Integer, List<CheckBoxItem>> itemMap;
    private Context mContext;
    String LOG_TAG = getClass().getSimpleName();
    private CardListView mCardListView;
    ArrayList<Card> mCardList;
    private final String ACTION_DATA_UPDATED = getString(R.string.widget_date_update);
    boolean mAllDone;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card, container, false);
        rowItemList = new ArrayList<>();
        return view;
    }

    // Show notification if the location is turned off.
    public void sendLocationDisableNotification(){
            Intent notificationIntent = new Intent(getActivity(),DetailActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
            stackBuilder.addParentStack(DetailActivity.class).addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(notificationIntent);

            PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());
            builder.setSmallIcon(R.drawable.ic_event_note_white_18dp)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_event_note_black_24dp))
                    .setColor(getResources().getColor(R.color.vanilla_white))
                    .setContentTitle(getString(R.string.location_off_title))
                    .setContentText(getString(R.string.location_off_detail))
                    .setContentIntent(notificationPendingIntent);

            builder.setAutoCancel(true);
            NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(!Utils.isLocationEnabled(getActivity())){
            sendLocationDisableNotification();
        }

        mCardList = new ArrayList<>();
        mCardAdapter = new CardArrayAdapter(getActivity(), mCardList);
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDetailFragment(TodoConstants.DEFAULT_ID, TodoConstants.REQUEST_TYPE.ADD.ordinal());
            }
        });

        mCardListView = (CardListView) getActivity().findViewById(R.id.card_recycler_view);
        mCardListView.setAdapter(mCardAdapter);
        mCardListView.setEmptyView(getActivity().findViewById(R.id.empty));
        getLoaderManager().initLoader(LIST_ALL, null, this);
    }

    private void startDetailFragment(int id, int mode) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(getString(R.string.bundle_key), id);
        intent.putExtra(getString(R.string.mode), mode);
        startActivity(intent);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Loader cardLoader = null;
        switch (id) {
            case LIST_ALL:
                cardLoader = new CursorLoader(
                        getActivity(),
                        TodoProvider.Lists.CONTENT_URI,
                        LIST_PROJECTION,
                        null,
                        null,
                        null);
                break;

        }
        return cardLoader;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Cursor cursor = (Cursor) data;
        rowItemList.clear();

        switch (loader.getId()) {
            case LIST_ALL:
                itemMap = new HashMap<>();
                if (cursor!=null && cursor.moveToFirst()) {
                    do {
                        TodoItem singleItem = new TodoItem();
                        singleItem.setTodoTitle(cursor.getString(cursor.getColumnIndexOrThrow(ListColumns.TITLE)));
                        singleItem.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(ListColumns.LOCATION)));
                        singleItem.setDate(cursor.getString(cursor.getColumnIndexOrThrow(ListColumns.DATE)));
                        int listId = cursor.getInt(cursor.getColumnIndexOrThrow(ListColumns._ID));
                        singleItem.setId(listId);
                        List<CheckBoxItem> actionItems = loadActionItems(listId);
                        singleItem.setActionList(actionItems);
                        singleItem.setIsDone(mAllDone);

                        if (!Utils.isNullOrEmpty(singleItem.getDate()))
                            singleItem.setIsExpired(hasExpired(singleItem.getDate()));

                        rowItemList.add(singleItem);
                    } while (cursor.moveToNext());
                }
                updateListView(rowItemList);
                break;
        }
    }

    public boolean hasExpired(String date) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern(TodoConstants.dateFormat);
        LocalDate localDate = fmt.parseLocalDate(date);
        return localDate.isBefore(LocalDate.now());
    }

    public List<CheckBoxItem> loadActionItems(long listId) {
        List<CheckBoxItem> actionRowList = new ArrayList<>();
        Cursor cursor = getContext().getContentResolver().query(TodoProvider.Items.fromList(listId), null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            mAllDone = true;
            do {
                CheckBoxItem actionRow = new CheckBoxItem();
                actionRow.setActionItem(cursor.getString(cursor.getColumnIndexOrThrow(ItemColumns.todoText)));
                boolean isSelected = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndexOrThrow(ItemColumns.isDone)));
                if (!isSelected) {
                    mAllDone = false;
                }
                actionRow.setIsSelected(isSelected);
                actionRowList.add(actionRow);
            } while (cursor.moveToNext());
        }
        return actionRowList;
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    public void updateListView(List<TodoItem> rowItemList) {
        mCardList.clear();
        mCardAdapter.notifyDataSetChanged();
        for (TodoItem item : rowItemList) {
            TodoCard newCard = new TodoCard(getActivity(), item.getId(), item);
            newCard.setOnClickListener(new Card.OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    int id = ((TodoCard) card).getCardListId();
                    startDetailFragment(id, TodoConstants.REQUEST_TYPE.MODIFY.ordinal());
                }
            });
            newCard.init();
            newCard.setOnUndoHideSwipeListListener(new Card.OnUndoHideSwipeListListener() {
                @Override
                public void onUndoHideSwipe(final Card card) {
                    mContext = getContext();
                    final int cardId = Integer.parseInt(card.getId());
                    deleteCardWithId(cardId);

                }
            });
            mCardList.add(newCard);
        }
        mCardAdapter.setEnableUndo(true);
        mCardAdapter.notifyDataSetChanged();
        Intent broadcastIntent = new Intent(ACTION_DATA_UPDATED).setPackage(getActivity().getPackageName());
        getActivity().sendBroadcast(broadcastIntent);

    }

    public void deleteCardWithId(int id) {
        mContext.getContentResolver().delete(TodoProvider.Items.fromList(id), null, null);
        int count = mContext.getContentResolver().delete(TodoProvider.Lists.withID(id), null, null);
        if (count < 0) {
            Toast.makeText(mContext, getString(R.string.error_list_del), Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, getString(R.string.error_list_del) + id);
        }
        removeGeoLocations(id);
        removePendingAlarms(id);
    }

    public void removePendingAlarms(int id) {
        Intent intentAlarm = new Intent(getActivity(), GeoAlarmReceiver.class);
        intentAlarm.putExtra(getString(R.string.alarm_id), id);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(getActivity(), 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(alarmPendingIntent);
    }

    public void removeGeoLocations(int id) {
        Intent intent = new Intent(getActivity(), GeofenceActivity.class);
        intent.putExtra(getString(R.string.location_id_key), id);
        intent.putExtra(getString(R.string.location_request_key), TodoConstants.REQUEST_TYPE.REMOVE.ordinal());
        startActivity(intent);
    }

}