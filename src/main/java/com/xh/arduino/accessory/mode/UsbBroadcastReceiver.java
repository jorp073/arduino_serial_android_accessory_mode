package com.xh.arduino.accessory.mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import xh.serial.MainActivity;

/**
 * Created by admin on 2016/3/26.
 */
public class UsbBroadcastReceiver extends BroadcastReceiver {
    private final String TAG="UsbBroadcastReceiver";

    public static final String ACTION_USB_PERMISSION ="com.xh.USB_PERMISSION";

    private ArduinoManager arduinoManager;

    public UsbBroadcastReceiver(ArduinoManager arduinoManager){
        this.arduinoManager=arduinoManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "broadcast");
        if (ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    Toast.makeText(context, "Allow USB Permission", Toast.LENGTH_SHORT).show();
                    arduinoManager.connectAccessory(accessory);
                } else {
                    Toast.makeText(context, "Deny USB Permission", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
            Toast.makeText(context, "USB detached", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "usb detached");
            arduinoManager.restart();
        }
    }
}
