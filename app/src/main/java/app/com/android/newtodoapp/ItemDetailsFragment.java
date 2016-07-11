package app.com.android.newtodoapp;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Parcel;
import android.support.v4.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import app.com.android.newtodoapp.data.ItemColumns;
import app.com.android.newtodoapp.data.ListColumns;
import app.com.android.newtodoapp.data.TodoProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.Unbinder;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;

/**
 * Author : Sharath Koday on 6/25/2016.
 * This fragment displays the items of a list enabling editing. Validation is followed by insertion into the Database.
 * Shared preferences are used to store field information used by broadcast services.
 */
public class ItemDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks, onBackPressedListener,TimePickerDialog.OnTimeSetListener,DatePickerDialog.OnDateSetListener{

    final static int PLACE_PICKER_CODE = 10;
    final static int SCANNER_CODE = 1;
    private String LOG_TAG = getClass().getSimpleName();
    private static final int RESULT_OK = -1;
    private static final int ITEM_ALL = 10;
    private static final int ITEMS_FROM_LIST = 20;
    private static final int LIST_WITH_ID = 30;
    private ArrayList<CheckBoxItem> itemListArray;
    private RowItemAdapter mRowAdapter;
    private int mListId;
    private static int mLoaderCount;
    private TodoItem mTodoItem;
    private Unbinder unbinder;
    private static int mRowCounter;
    private int mMode;
    private Context mContext;
    private LocalDate mChosenDate;
    private LocalTime mStartTime;
    private LocalTime mEndTime;
    private int mSelection;
    private Place mChosenPlace;
    private long randomId;
    private boolean isRange;
    private boolean isSaveInstance;
    private static final LocalDateTime JAN_1_1970 = new LocalDateTime(1970, 1, 1, 0, 0);

    @BindView(R.id.item_list_view)ListView mListView;
    @BindView(R.id.title_ev)EditText mTitleView;
    @BindView(R.id.date_ev)EditText mDateView;
    @BindView(R.id.time_ev)EditText mTimeView;
    @BindView(R.id.location_tv)EditText mLocView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        isSaveInstance = false;
        if(savedInstanceState!=null){
            itemListArray = savedInstanceState.getParcelableArrayList(getString(R.string.parcel_key));
        }else{
            itemListArray = new ArrayList<>();
        }
        Random rand = new Random();
        randomId = rand.nextLong();
        mTodoItem = new TodoItem();
        mRowAdapter = new RowItemAdapter(getActivity(), itemListArray);
        unbinder = ButterKnife.bind(this, view);
        mListView.setAdapter(mRowAdapter);
        mListView.setFocusableInTouchMode(true);
        mListView.requestFocus();
        return view;
    }

    @Override
    public void onResume() {
        View current = getActivity().getCurrentFocus();
        if(current!=null)
            current.clearFocus();
        super.onResume();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_detail, menu);
        menu.findItem(R.id.scan_item).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan_item:
                startScannerActivity();
                break;
            default:
                return false;
        }
        return true;
    }

    public void startScannerActivity(){
        Context context = getActivity();
        Intent scanIntent = new Intent(context, ScannerActivity.class);
        startActivityForResult(scanIntent, SCANNER_CODE);
    }

    @OnClick(R.id.fab_action_item)
    void addEmptyRow(){
        fetchViewData();
        itemListArray.add(new CheckBoxItem(""));
        mRowAdapter.notifyDataSetChanged();
    }

    // Fetch the current view data.
    public void fetchViewData(){
        itemListArray.clear();
        mRowAdapter.notifyDataSetChanged();
        int initialPos = 0;
        int lastPos = mListView.getChildCount()-1;
        while(initialPos<=lastPos){
            RelativeLayout rowLayout = (RelativeLayout)mListView.getChildAt(initialPos);
            if(rowLayout!=null) {
                boolean checkBoxValue=false;
                String textValue = "";
                int innerChildCount = rowLayout.getChildCount();
                int index = 0;
                int whatMatters = 0;
                while(index < innerChildCount){
                    View innerView = rowLayout.getChildAt(index);
                    if(innerView instanceof CheckBox){
                        whatMatters++;
                        checkBoxValue = ((CheckBox)innerView).isChecked();
                    }else if(innerView instanceof EditText){
                        whatMatters++;
                        textValue = ((EditText)innerView).getText().toString();
                    }
                    if(whatMatters == 2) {
                        itemListArray.add(new CheckBoxItem(textValue, checkBoxValue));
                        break;
                    }
                    index++;
                }
            }
            initialPos++;
        }
    }

    public void addViewAndUpdateList(CheckBoxItem newItem){
        itemListArray.add(newItem);
        mRowAdapter.notifyDataSetChanged();
        mListView.smoothScrollToPosition(mRowCounter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        mListId = getActivity().getIntent().getIntExtra(getString(R.string.bundle_key), 0);
        mMode = getActivity().getIntent().getIntExtra(getString(R.string.mode),0);
        if(savedInstanceState == null) {
            if (mMode == TodoConstants.MODIFY) {
                // Init all the loader manager
                getLoaderManager().initLoader(LIST_WITH_ID, null, this);
                getLoaderManager().initLoader(ITEMS_FROM_LIST, null, this);
                getLoaderManager().initLoader(ITEM_ALL, null, this);
            } else {
                addViewAndUpdateList(new CheckBoxItem());
            }
        }
    }

    @OnFocusChange(R.id.date_ev)
    public void onFocusDateView(boolean hasFocus) {
        if(hasFocus) {
            openDatePicker();
        }
    }

    @OnClick(R.id.date_ev)
    public void onClickDateView(){
        openDatePicker();
    }

    public void openDatePicker(){
        Calendar now = Calendar.getInstance();
        DatePickerDialog dateDialog = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        dateDialog.show(getActivity().getFragmentManager(), "DatePickerDialog");
    }

    // Start place picker activity.
    @OnFocusChange(R.id.location_tv) void chooseLocationOnFocus(boolean hasFocus){
        if(hasFocus){
            startPlacePicker();
        }
    }

    // Open the location picker.
    @OnClick(R.id.location_tv) void chooseLocationOnClick(){
        startPlacePicker();
    }
    void startPlacePicker(){
        PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(intentBuilder.build(getActivity()), PLACE_PICKER_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    public Dialog createRangeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.Theme_AppCompat_CompactMenu));
        mSelection = -1;
        final String[] array = getResources().getStringArray(R.array.time_range_list);
        builder.setTitle(R.string.alert_title)
                .setSingleChoiceItems(R.array.time_range_list, 1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.time_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView listView = ((AlertDialog) dialog).getListView();
                        mSelection = listView.getCheckedItemPosition();
                        Pair<LocalTime, LocalTime> rangePair = Utils.getTimePairFromRange(mSelection);
                        mStartTime = rangePair.first;
                        mEndTime = rangePair.second;
                        isRange = true;
                        //if (validateDateTime()) {
                        mTimeView.setText(array[mSelection]);
                        //}
                    }
                })
                .setNegativeButton(R.string.time_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        return builder.create();
    }

    // Validate if the chosen date and time are valid.
    private boolean validateDateTime(){
        boolean isValid = true;
        LocalTime currentTime = LocalTime.now(DateTimeZone.getDefault());
        LocalDate currentDate = LocalDate.now(DateTimeZone.getDefault());
        if(mChosenDate!=null) {
            if (mChosenDate.isBefore(currentDate)) {
                Toast.makeText(getContext(), getString(R.string.error_date), Toast.LENGTH_SHORT).show();
                isValid = false;
            } else if (mChosenDate.equals(currentDate)) {
                if (mStartTime != null && mStartTime.isBefore(currentTime) &&
                        (isRange && (mEndTime.isBefore(currentTime)))) {
                    Toast.makeText(getContext(), getString(R.string.error_invalid_time), Toast.LENGTH_SHORT).show();
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    @OnFocusChange(R.id.time_ev)
    void onFocusRangePick(boolean hasFocus){
        if(hasFocus) {
          openTimeRangeDialog();
        }
    }

    @OnClick(R.id.time_ev)
    void onClickRangePick(){
       openTimeRangeDialog();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Loader detailLoader = null;
        switch(id){

            case ITEM_ALL:
                detailLoader = new CursorLoader(
                        getActivity(),
                        TodoProvider.Items.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);

                break;
            case ITEMS_FROM_LIST:
                detailLoader = new CursorLoader(
                        getActivity(),
                        TodoProvider.Items.fromList(mListId),
                        null,
                        ItemColumns.List_ID  + "=?",
                        new String[]{String.valueOf(mListId)},
                        null);
                break;
            case LIST_WITH_ID:
                detailLoader = new CursorLoader(
                        getActivity(),
                        TodoProvider.Lists.withID(mListId),
                        null,
                        ListColumns._ID + "=?",
                        new String[] {String.valueOf(mListId)},
                        null);
        }
        return detailLoader;
    }

    public void openTimeRangeDialog(){
        InputMethodManager manager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(manager!=null) {
            manager.hideSoftInputFromWindow(mTimeView.getWindowToken(), 0);
        }
        createRangeDialog().show();
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Cursor cursor = (Cursor) data;
        switch(loader.getId()){
            case LIST_WITH_ID:
                mLoaderCount++;
                String title = "";
                String date = "";
                String location = "";
                String time = "";
                if(cursor.moveToFirst()) {
                    title = cursor.getString(cursor.getColumnIndexOrThrow(ListColumns.TITLE));
                    date = cursor.getString(cursor.getColumnIndexOrThrow(ListColumns.DATE));
                    location = cursor.getString(cursor.getColumnIndexOrThrow(ListColumns.LOCATION));
                    time = cursor.getString(cursor.getColumnIndexOrThrow(ListColumns.TIME));
                }
                mTodoItem.setTodoTitle(title);
                mTodoItem.setDate(date);
                mTodoItem.setLocation(location);
                mTodoItem.setTime(time);
                break;
            case ITEMS_FROM_LIST:
                mLoaderCount++;
                List<CheckBoxItem> actionItemList = new ArrayList<>();
                if(!cursor.moveToFirst()){
                    actionItemList.add(new CheckBoxItem());
                }else {
                    do {
                        CheckBoxItem item = new CheckBoxItem();
                        item.setActionItem(cursor.getString(cursor.getColumnIndexOrThrow(ItemColumns.todoText)));
                        item.setIsSelected(Boolean.parseBoolean
                                (cursor.getString
                                        (cursor.getColumnIndexOrThrow
                                                (ItemColumns.isDone))));
                        actionItemList.add(item);
                    } while (cursor.moveToNext());
                }
                mRowCounter = actionItemList.size()>0?actionItemList.size():1;
                mTodoItem.setActionList(actionItemList);
        }

        if(mLoaderCount == 2) {
            mLoaderCount = 0;
            updateViews(mTodoItem);
        }
    }

    public void updateViews(TodoItem todoItem){
        mTitleView.setText(todoItem.getTodoTitle());
        mDateView.setText(todoItem.getDate());
        mLocView.setText(todoItem.getLocation());
        mTimeView.setText(todoItem.getTime());
        for(CheckBoxItem item:todoItem.getActionList())
            addViewAndUpdateList(item);
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    public void handlePickerActivityResult(Intent data){
        mChosenPlace = PlacePicker.getPlace(data, getActivity());
        try {
            String partialLocation = Utils.convertPlaceToLocality(getActivity(), mChosenPlace);
            mLocView.setText(partialLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        fetchViewData();
        isSaveInstance = true;
        mListView.requestFocus();
        outState.putParcelableArrayList(getString(R.string.parcel_key), itemListArray);
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onDetach() {
        mContext = getContext();
        if(!isSaveInstance) {
            insertUpdateValues();
        }
        super.onDetach();
    }

    private ContentValues populateDataForList(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ListColumns.TITLE, mTodoItem.getTodoTitle());
        contentValues.put(ListColumns.DATE, mTodoItem.getDate());
        contentValues.put(ListColumns.LOCATION, mTodoItem.getLocation());
        contentValues.put(ListColumns.TIME, mTodoItem.getTime());
        return contentValues;
    }

    private ContentValues[] populateDataForItems(int id){
        ContentValues[]  contentValues = new ContentValues[itemListArray.size()];
        for(int i=0;i<contentValues.length;i++){
            ContentValues value = new ContentValues();
            value.put(ItemColumns.List_ID,id);
            value.put(ItemColumns.todoText,itemListArray.get(i).getActionItem());
            value.put(ItemColumns.isDone,String.valueOf(itemListArray.get(i).isSelected()));
            contentValues[i] = value;
        }
        return contentValues;
    }

    public void insertActionItems(int id){
        ContentValues[] itemsCv = populateDataForItems(id);
        for (ContentValues value : itemsCv) {
            Uri uri = mContext.getContentResolver().insert(TodoProvider.Items.CONTENT_URI, value);
            if (uri == null) {
                Log.e(LOG_TAG, "Insertion: " + getString(R.string.URi_items_error) + value.get(ItemColumns._ID));
                throw new NullPointerException("Insertion:" + getString(R.string.URi_items_error));
            }
        }
    }

    // TODO:Insert values in database
    public void insertUpdateValues(){
        ContentValues values = populateDataForList();

        // If insert_mode then insert data in Lst and Items table.
        if(mMode == TodoConstants.ADD){
            Uri listUri = mContext.getContentResolver().insert(TodoProvider.Lists.CONTENT_URI,values);
            if(listUri == null){
                Log.e(LOG_TAG,"Insertion :"+getString(R.string.Uri_list_error)+values.get(ListColumns._ID));
                throw new NullPointerException("Insertion :"+getString(R.string.Uri_list_error));
            }else {
                mListId = (int) ContentUris.parseId(listUri);
                insertActionItems(mListId);
            }

            // If update_mode then delete data from Items table and update the List table and Delete + Insert into Items table
            // Delete+Insert has very little downside compared to Update.
        }else if(mMode == TodoConstants.MODIFY){
            int count = mContext.getContentResolver().update(TodoProvider.Lists.withID(mListId), values, null, null);
            if(count < 1){
                Log.e(LOG_TAG,"Update : "+getString(R.string.Uri_list_error)+" : "+ mListId);
            }else{
                // Delete all the items
                mContext.getContentResolver().delete(TodoProvider.Items.fromList(mListId),null,null);

                // Add all the items
                insertActionItems(mListId);
            }
        }

        // Hacky stuff :  If the mode is update, then mChosenDate or mStartTime can still be null, which means one of these fields were not edited from the previous value.
        // A change in either of them will change the schedule time. If the date has not changed, get the current date from the date view and if the time range
        // has not changed, use the string array to find out which one it is and get the start time. A more permanent solution should be to store date and time as LocalDate and LocalTime in DB
        if(mMode == TodoConstants.MODIFY &&
                (mChosenDate==null || mStartTime==null)) {
            mChosenDate = mChosenDate == null? Utils.getDateFromString(mTodoItem.getDate()):mChosenDate;
            mStartTime  = mStartTime == null ? Utils.getStartTimeFromString(getActivity(),mTodoItem.getTime()):mStartTime;
        }

        scheduleTimeReminder();

        // Store time,date
        writeToPrereferences();
        if(mChosenPlace!=null) {
            createAndSetGeofences(mMode);
        }
    }

    // Schedule reminder using Alarm manager
    public void scheduleTimeReminder(){
        if(mChosenDate!=null && !Utils.isNullOrEmpty(mChosenDate.toString())) {
            DateTimeZone dtz = DateTimeZone.getDefault();
            LocalDateTime localDateTime = mChosenDate.toLocalDateTime(mStartTime);

            // Get milliseconds since the epoch.
            Duration duration = new Duration(JAN_1_1970.toDateTime(dtz), localDateTime.toDateTime(dtz));
            long millis = duration.getMillis();
            Intent intentAlarm = new Intent(getActivity(), GeoAlarmReceiver.class);
            intentAlarm.putExtra(getString(R.string.alarm_id), mListId);
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(getActivity(), 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);

            // If modify then remove the existing pending and add the intent again.
            if (mMode == TodoConstants.MODIFY ||
                    mMode == TodoConstants.REMOVE) {
                alarmManager.cancel(alarmPendingIntent);
            }
            if (mMode == TodoConstants.ADD
                    || mMode == TodoConstants.MODIFY) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, millis, alarmPendingIntent);
            }
        }
    }

    public void writeToPrereferences(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.TODO_SHARED_PREF), 0);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        if (mStartTime != null) {
            prefsEditor.putString(mListId + getString(R.string.start_key), mStartTime.toString());
        }
        if (mEndTime != null) {
            prefsEditor.putString(mListId + getString(R.string.end_key), mEndTime.toString());
        }
        if (mChosenDate != null) {
            prefsEditor.putString(mListId + getString(R.string.date_key), mChosenDate.toString());
        }

        prefsEditor.apply();
    }

    public void createAndSetGeofences(int mMode){
        TodoGeofence newGeoFence = new TodoGeofence(mChosenPlace.getLatLng().longitude,
                mChosenPlace.getLatLng().latitude,
                TodoConstants.GEOFENCE_RADIUS,
                TodoConstants.EXPIRATION_PERIOD,
                String.valueOf(randomId));
        Intent intent = new Intent(getActivity(), GeofenceActivity.class);
        intent.putExtra(getString(R.string.location_obj_key), newGeoFence);
        intent.putExtra(getString(R.string.location_id_key), mListId);
        intent.putExtra(getString(R.string.location_request_key), mMode);
        startActivity(intent);
    }

    @Override
    public void OnBackPress() {
        fetchViewData();
        isSaveInstance = false;
        mTodoItem.setTodoTitle(mTitleView.getText().toString());
        mTodoItem.setLocation(mLocView.getText().toString());
        mTodoItem.setDate(mDateView.getText().toString());
        mTodoItem.setTime(mTimeView.getText().toString());
        checkDateTimeEntered();
    }

    // Date and time are mutual. It doesn't make sense to have date without time and vice versa. This validates it.
    public void checkDateTimeEntered(){
        String dateView = mDateView.getText().toString();
        String timeView = mTimeView.getText().toString();
        if(!dateView.equals("") && timeView.equals("")||
                dateView.equals("") && !timeView.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.builder_title))
                    .setMessage(getString(R.string.builder_message))
                    .setPositiveButton(R.string.time_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .create().show();
            ((DetailActivity) getActivity()).mDateTimeValid = false;
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        mChosenDate = new LocalDate(year,monthOfYear+1,dayOfMonth);
        if(validateDateTime()) {
            String friendlyDate = Utils.getFriendlyDateString(mChosenDate);
            mDateView.setText(friendlyDate);
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {

    }

    class FetchProductDetails extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {
            boolean foundError = false;
            XMLRPCClient client;
            HashMap result = null;
            String upcString = params[0];
            String productResult = "";
            try {
                client = new XMLRPCClient(new URL(getString(R.string.rpc_url)));
                Map<String,String> paramMap =  new HashMap<>();

                if(upcString.length() == TodoConstants.UPC_TYPE_E){
                    upcString = Utils.convertTypeEtoTypeA(upcString.substring(1,7));
                }else if(upcString.length() != TodoConstants.UPC_TYPE_A){
                    foundError = true;
                    Log.e(LOG_TAG,"invalid UPC:" + upcString);
                }

                paramMap.put(getString(R.string.RPC_KEY_TAG),getString(R.string.rpc_key));
                paramMap.put(getString(R.string.UPC_TAG), upcString);
                result = (HashMap)client.call(getString(R.string.LOOKUP_TAG),paramMap);
            } catch (XMLRPCException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            if(result!=null ) {
                Boolean found = (Boolean)result.get(getString(R.string.found_tag));
                if(!found){
                    foundError = true;
                }
                if(!foundError) {
                    productResult = result.get(getString(R.string.DESCRIPTION_TAG)).toString();
                }
            }
            return productResult;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!s.isEmpty()){
                addViewAndUpdateList(new CheckBoxItem(s));
            }else{
                Toast.makeText(getActivity(),getString(R.string.invalid_product),Toast.LENGTH_SHORT).show();
            }
        }
    }


    // Open the scanner activity to scan any product. Invalid/unrecognized products are met with an error message.
    public void handleScannerActivityResult(Intent data){
        if(data!=null){
            String upcString = data.getStringExtra(Intent.EXTRA_TEXT);
            FetchProductDetails fetchProductDetails = new FetchProductDetails();
            fetchProductDetails.execute(upcString);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean resultOk = resultCode == RESULT_OK;
        switch(requestCode){
            case PLACE_PICKER_CODE:
                if(resultOk) {
                    handlePickerActivityResult(data);
                }
                break;
            case SCANNER_CODE:
                if(resultOk){
                    handleScannerActivityResult(data);
                }
        }
    }
}
