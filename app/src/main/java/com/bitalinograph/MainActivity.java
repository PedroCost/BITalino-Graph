package com.bitalinograph;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button button_connect, button_start;
    static WebView myWebView;
    BITalinoMethods bitMethods;
    static String TAG = "BIT";
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkForBluetooth();
        findItemsId();
        setElements();
    }

    private void checkForBluetooth() {
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Error - Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    private void findItemsId() {
        button_connect = findViewById(R.id.button_connect);
        button_start = findViewById(R.id.button_start);
        myWebView = findViewById(R.id.WebView);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setElements() {
        button_connect.setOnClickListener(this);
        button_start.setOnClickListener(this);
        bitMethods = new BITalinoMethods(this, "20:15:05:29:21:00", new int[]{1}, 100);

        WebSettings webSettings = myWebView.getSettings();
        WebView.setWebContentsDebuggingEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                android.util.Log.d("WebView", consoleMessage.message());
                return true;
            }
        });

        myWebView.addJavascriptInterface(new WebAppInterface(this, myWebView), "Android");
        myWebView.loadUrl("file:///android_asset/main.html");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_connect:
                bitMethods.connect();
                break;
            case  R.id.button_start:
                bitMethods.start();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        bitMethods.bitalinoOnStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bitMethods.bitalinoOnDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bitMethods.bitalinoOnResume();
    }

    public static void sendToWebView(int value){
        try{
            myWebView.post(() -> myWebView.loadUrl("javascript: valueFromBitalino("+value+");"));
        }catch (Exception e) {
            Log.e(TAG, "Error: " + e);
        }
    }


}