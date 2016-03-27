package com.xh.arduino.accessory.mode;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import xh.serial.R;
public class UsbBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = "UsbBroadcastReceiver";

    public static final String ACTION_USB_PERMISSION = "com.xh.USB_PERMISSION";

    private Handler handler;

    public UsbBroadcastReceiver(Handler handler) {
        this.handler = handler;
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
                    Message message=handler.obtainMessage(R.id.handle_response_permission,accessory);
                    message.sendToTarget();
                } else {
                    Toast.makeText(context, "Deny USB Permission", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
            Toast.makeText(context, "USB detached", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "usb detached");
            handler.sendEmptyMessage(R.id.handle_reset_arduino);
        }
    }
}
