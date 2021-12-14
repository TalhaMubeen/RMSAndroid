package com.innv.rmsgateway.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.Result;
import com.innv.rmsgateway.classes.Globals;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrCodeScanner extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        Globals.setScreenOrientation(this);
        super.onCreate(state);
        // Programmatically initialize the scanner view
        mScannerView = new ZXingScannerView(this);
        // Set the scanner view as the content view
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        // Start camera on resume
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        Intent intent = new Intent();
        intent.putExtra("SCAN_RESULT", rawResult.getText());
        setResult(RESULT_OK, intent);
        finish();
    }
}