package com.xh.usb.accessory.mode;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArduinoManager extends Handler {
    private final String TAG = "ArduinoManager";

    private Context context;
    private final UsbManager usbManager;
    private Handler mainHandler;
    private PendingIntent pendingIntent;
    private IntentFilter filter;
    SerialReadBuffer readBuffer = new SerialReadBuffer();

    public ArduinoManager(Context context, Handler handler) {
        this.context = context;
        this.mainHandler = handler;
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        sendEmptyMessageDelayed(0, 500);//开始搜索

        pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);//连接响应
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);//断开响应
        context.registerReceiver(usbBroadcastReceiver, filter);//注册接收器
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                UsbAccessory device = searchDevice();
                if (device == null) {
                    this.sendEmptyMessageDelayed(0, 3000);//3秒后重新搜索
                }
                break;
            case 1:///receive data
                mainHandler.sendEmptyMessage(0);
                break;
        }
    }

    public static final String ACTION_USB_PERMISSION = "com.xh.serial.accessory.USB_PERMISSION";
    private BroadcastReceiver usbBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "receive permission request result");
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        connectAccessory(accessory);
                    } else {
                        Toast.makeText(context, "Deny USB Permission", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                Toast.makeText(context, "设备断线", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "usb detached");
                reset();
            }
        }
    };

    private void requestPermission(UsbAccessory accessory) {
        Log.d(TAG, "request permission");
        usbManager.requestPermission(accessory, pendingIntent);
    }

    public SerialReadBuffer getSerialReadBuffer() {
        return this.readBuffer;
    }

    public UsbAccessory searchDevice() {
        Log.d(TAG, "searching arduino........");

        if (inputStream != null && outputStream != null) {
            return null;
        }

        UsbAccessory[] accessories = usbManager.getAccessoryList();
        if (accessories != null) {
            Log.d(TAG, "usb accessory attach");
            Toast.makeText(context, "发现控制设备", Toast.LENGTH_SHORT).show();
        }
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {

            if(!accessory.getModel().contains("Arduino")){
                Toast.makeText(context, "控制设备型号不正确", Toast.LENGTH_SHORT).show();
                Log.w(TAG,"device not arduino");
                return null;
            }

            if (usbManager.hasPermission(accessory)) {
                connectAccessory(accessory);
            } else {
                requestPermission(accessory);
            }
        }
        return accessory;
    }

    ParcelFileDescriptor fileDescriptor;
    FileInputStream inputStream;
    FileOutputStream outputStream;
    SerialInputManager inputManager;
    private final ExecutorService mExecutor = Executors.newCachedThreadPool();

    public void connectAccessory(UsbAccessory accessory) {
        Log.d(TAG, "connecting");
        Toast.makeText(context, "连接设备", Toast.LENGTH_SHORT).show();
        fileDescriptor = usbManager.openAccessory(accessory);
        if (fileDescriptor != null) {
            FileDescriptor fd = fileDescriptor.getFileDescriptor();
            inputStream = new FileInputStream(fd);
            outputStream = new FileOutputStream(fd);
            inputManager = new SerialInputManager(inputStream, this, readBuffer);
            mExecutor.submit(inputManager);
        }
    }

    public void send(String s) {
        Log.i(TAG, "sending message:" + s);
        if (outputStream == null) {
            Log.w(TAG, "No FileOutputStream,不能输出");
            return;
        }
        byte[] b = s.getBytes();
        try {
            outputStream.write(b);
        } catch (IOException e) {
            Log.w(TAG, "send error, stop due to exception: " + e.getMessage(), e);
        }
    }

    public void reset() {
        Log.d(TAG, "restart");
        if(inputManager!=null){
            inputManager.stop();
        }
        inputManager = null;

        close();

        context.registerReceiver(usbBroadcastReceiver, filter);//注册接收器

        readBuffer.clean();
        //重新开始查找
        this.removeMessages(0);
        this.sendEmptyMessageDelayed(0, 500);
    }

    public void close() {
        try {
            if (fileDescriptor != null)
                fileDescriptor.close();
        } catch (IOException e) {
            Log.w(TAG, "close fail：" + e.getMessage(), e);
        }

        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            Log.w(TAG, "close fail：" + e.getMessage(), e);
        }

        try {
            if (outputStream != null)
                outputStream.close();
        } catch (IOException e) {
            Log.w(TAG, "close fail：" + e.getMessage(), e);
        }

        fileDescriptor = null;
        inputStream = null;
        outputStream = null;

        context.unregisterReceiver(usbBroadcastReceiver);
    }
}
