package com.bitalinograph;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import info.plux.pluxapi.Communication;
import info.plux.pluxapi.Constants;
import info.plux.pluxapi.bitalino.BITalinoCommunication;
import info.plux.pluxapi.bitalino.BITalinoCommunicationFactory;
import info.plux.pluxapi.bitalino.BITalinoDescription;
import info.plux.pluxapi.bitalino.BITalinoException;
import info.plux.pluxapi.bitalino.BITalinoFrame;
import info.plux.pluxapi.bitalino.BITalinoState;
import info.plux.pluxapi.bitalino.bth.OnBITalinoDataAvailable;

import static android.os.Looper.getMainLooper;


public class BITalinoMethods implements OnBITalinoDataAvailable {

    public final static String FRAME = "info.plux.pluxapi.sampleapp.DeviceActivity.Frame";

    private String TAG = "BialinoState", macAddress;
    private int sampleRate;
    private int[] analogChannels;
    private Activity activity;
    private Context contextMain;
    private TextView textView_value, textView_state;
    private Handler handler;
    private BITalinoCommunication bitalino;
    private boolean isConnected = false, isStarted = false;
    private Button bt_connect , bt_start;
    private Boolean isUpdateReceiverRegistered = false;


    public BITalinoMethods(Activity _activity, String macAddress, int[] analogChannels, int sampleRate) {
        this.activity = _activity;
        this.contextMain = _activity.getBaseContext();
        this.macAddress = macAddress;
        this.sampleRate = sampleRate;
        this.analogChannels = analogChannels;
        textView_value = _activity.findViewById(R.id.textView_value);
        textView_state = _activity.findViewById(R.id.textView_state);
        bt_connect = _activity.findViewById(R.id.button_connect);
        bt_start = _activity.findViewById(R.id.button_start);
        bitalino = new BITalinoCommunicationFactory().getCommunication(Communication.BTH, activity.getBaseContext(), this);
        startHandler();
    }

    private void startHandler(){
        handler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                BITalinoFrame frame = bundle.getParcelable(FRAME);
                if(frame != null) {
                    textView_value.setText(frame.getAnalog(1) + "");
                    MainActivity.sendToWebView(frame.getAnalog(1));
                }
            }
        };
    }

    private void regist(){
        bitalino = new BITalinoCommunicationFactory().getCommunication(Communication.BTH, activity.getBaseContext(), this);
    }

    public void connect(){
        try {
            if(!isConnected){
                bitalino.connect(macAddress);
                bt_connect.setText(activity.getString(R.string.button_disconnect));
                isConnected = true;
            } else {
                bitalino.disconnect();
                bt_connect.setText(activity.getString(R.string.button_connect));
                isConnected = false;
            }
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        try {
            if(!isStarted){
                bitalino.start(analogChannels, sampleRate);
                bt_start.setText(activity.getString(R.string.button_stop));
                isStarted = true;
            } else {
                bitalino.stop();
                bt_start.setText(activity.getString(R.string.button_start));
                isStarted = false;
            }
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }

    public void bitalinoOnStop(){
        try {
            bitalino.disconnect();
            bitalino.stop();
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }

    public void bitalinoOnResume(){
        contextMain.registerReceiver(updateReceiver, updateIntentFilter());
        isUpdateReceiverRegistered = true;
    }

    public void bitalinoOnDestroy(){
        contextMain.unregisterReceiver(updateReceiver);
        isUpdateReceiverRegistered = true;
        if(bitalino != null){
            bitalino.closeReceivers();
            try {
                bitalino.disconnect();
            } catch (BITalinoException e) {
                e.printStackTrace();
            }
        }
    }

    public final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Constants.ACTION_STATE_CHANGED.equals(action)) {
                String identifier = intent.getStringExtra(Constants.IDENTIFIER);
                Constants.States state = Constants.States.getStates(intent.getIntExtra(Constants.EXTRA_STATE_CHANGED,0));
                textView_state.setText(state.name());
                Log.i(TAG, "Device " + identifier + ": " + state.name());
            } else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {
                BITalinoFrame frame = intent.getParcelableExtra(Constants.EXTRA_DATA);
                Log.d(TAG, "BITalinoFrame: " + frame.toString());
            } else if (Constants.ACTION_COMMAND_REPLY.equals(action)) {
                String identifier = intent.getStringExtra(Constants.IDENTIFIER);
                Parcelable parcelable = intent.getParcelableExtra(Constants.EXTRA_COMMAND_REPLY);
                if(parcelable.getClass().equals(BITalinoState.class)){
                    Log.d(TAG, "BITalinoState: " + parcelable.toString());
                } else if(parcelable.getClass().equals(BITalinoDescription.class)){
                    Log.d(TAG, "BITalinoDescription: isBITalino2: " +
                            ((BITalinoDescription)parcelable).isBITalino2() + "; FwVersion:" + String.valueOf(((BITalinoDescription)parcelable).getFwVersion()));
                }
            } else if (Constants.ACTION_MESSAGE_SCAN.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(Constants.EXTRA_DEVICE_SCAN);
            }
        }
    };

    protected static IntentFilter updateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_STATE_CHANGED);
        intentFilter.addAction(Constants.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(Constants.ACTION_COMMAND_REPLY);
        intentFilter.addAction(Constants.ACTION_MESSAGE_SCAN);
        return intentFilter;
    }

    @Override
    public void onBITalinoDataAvailable(BITalinoFrame bitalinoFrame) {
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putParcelable(FRAME, bitalinoFrame);
        message.setData(bundle);
        handler.sendMessage(message);
    }
}
