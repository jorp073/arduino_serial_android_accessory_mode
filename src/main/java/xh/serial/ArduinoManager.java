package xh.serial;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by pc on 2016/2/1.
 */
public class ArduinoManager extends Handler {
    private final String TAG = "ArduinoManager";
    private Context context;
    private final UsbManager usbManager;
    private final MainActivity activity;
    SerialReadBuffer readBuffer = new SerialReadBuffer();

    public ArduinoManager(MainActivity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        sendEmptyMessage(R.id.arduino_search_device);//开始搜索
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case R.id.arduino_search_device:
                UsbAccessory device = searchDevice();
                if (device == null) {
                    this.sendEmptyMessageDelayed(R.id.arduino_search_device, 3000);//3秒后重新搜索
                }
                break;
            case R.id.arduino_receive_data:
                int size = readBuffer.size();
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        activity.arduinoShowReceiveData(readBuffer.read());
                    }
                }
                break;
        }
    }

    public UsbAccessory searchDevice() {
        Log.d(TAG, "searching arduino........");

        if (inputStream != null && outputStream != null) {
            return null;
        }

        UsbAccessory[] accessories = usbManager.getAccessoryList();
        if (accessories != null) {
            Log.d(TAG, "usb accessory attach");
            Toast.makeText(context, "Accessory Attached", Toast.LENGTH_SHORT).show();
        }
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            //TODO:在这里加入设备判断

            ///String Manufacturer=accessory.getManufacturer();

            if (usbManager.hasPermission(accessory)) {
                connectAccessory(accessory);
            } else {
                requestUsbPermission(accessory);
            }
        }
        return accessory;
    }

    public void requestUsbPermission(UsbAccessory device) {
        activity.getUsbPermission(device);
    }

    ParcelFileDescriptor fileDescriptor;
    FileInputStream inputStream;
    FileOutputStream outputStream;
    SerialInputManager inputManager;
    private final ExecutorService mExecutor = Executors.newCachedThreadPool();

    public void connectAccessory(UsbAccessory accessory) {
        Log.d(TAG, "connecting");
        fileDescriptor = usbManager.openAccessory(accessory);
        if (fileDescriptor != null) {
            FileDescriptor fd = fileDescriptor.getFileDescriptor();
            inputStream = new FileInputStream(fd);
            outputStream = new FileOutputStream(fd);
            if (inputStream == null || outputStream == null) {
                Log.w(TAG, "no io found");
                return;
            }
            inputManager = new SerialInputManager(inputStream, this, readBuffer);
            mExecutor.submit(inputManager);
        }
    }

    public void send(String s) {
        Log.i(TAG,"sending message:"+s);
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

    public void restart() {
        Log.d(TAG, "restart");
        inputManager.stop();
        inputManager = null;

        close();

        readBuffer.clean();
        //重新开始查找
        this.removeMessages(R.id.arduino_search_device);
        this.sendEmptyMessage(R.id.arduino_search_device);
    }

    public void close() {
        try {
            if (fileDescriptor != null)
                fileDescriptor.close();
        } catch (IOException e) {
        }

        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
        }

        try {
            if (outputStream != null)
                outputStream.close();

        } catch (IOException e) {
        }

        fileDescriptor = null;
        inputStream = null;
        outputStream = null;
    }
}
