package app.com.android.newtodoapp;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Author : Sharath Koday
 */


interface onBackPressedListener{
    void OnBackPress();
}

public class DetailActivity extends AppCompatActivity {
    onBackPressedListener backPressedListener;
    public boolean mDateTimeValid;
    private InterstitialAd annoyingAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDateTimeValid = true;
        JodaTimeAndroid.init(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.appbar_color)));
        annoyingAd = new InterstitialAd(this);
        actionBar.setDisplayHomeAsUpEnabled(true);
        annoyingAd.setAdUnitId(getString(R.string.admob_id));
        requestInterstitialAd();
        ItemDetailsFragment fragment = (ItemDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.detail_fragment_container);

        if (fragment == null) {
            fragment =  new ItemDetailsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.detail_fragment_container, fragment).commit();
        }
        backPressedListener = fragment;
        setContentView(R.layout.activity_detail);
    }

    public boolean checkForErrorAndReturn(){
        mDateTimeValid = true;
        backPressedListener.OnBackPress();
        if(mDateTimeValid) {
            if (annoyingAd.isLoaded()) {
                annoyingAd.show();
            }
        }
        return mDateTimeValid;
    }

    public void requestInterstitialAd(){
        String deviceId = getString(R.string.dev_id);
        // Uncomment the below line if using an emulator
        //deviceId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        AdRequest inAdequest = new AdRequest.Builder()
                .addTestDevice(deviceId)
                .build();

        annoyingAd.loadAd(inAdequest);
    }


    @Override
    public void onBackPressed() {
        if(checkForErrorAndReturn()){
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(checkForErrorAndReturn()) {
                    NavUtils.navigateUpFromSameTask(this);
                }
                break;
        }
        return(super.onOptionsItemSelected(item));
    }
}
