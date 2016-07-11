package app.com.android.newtodoapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Author : Sharath Koday
 */
public class ScannerActivity extends Activity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    public static int SCAN_OK = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        Intent scanResultIntent;
        scanResultIntent = new Intent();
        scanResultIntent.setType("text/plain");
        scanResultIntent.putExtra(Intent.EXTRA_TEXT,result.getText());
        setResult(SCAN_OK, scanResultIntent);
        finish();
    }
}
