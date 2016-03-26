package com.xh.arduino.accessory.mode;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

import xh.serial.R;

public class SerialInputManager implements Runnable {
    private static final String TAG = "SerialInputManager";

    private byte[] byteBuffer = new byte[128];
    private int bytes = 0;
    private boolean isStop = false;
    FileInputStream inputStream;
    SerialReadBuffer readBuffer;
    Handler handler;

    public void stop() {
        isStop = true;
    }

    public SerialInputManager(FileInputStream inputStream, Handler handler,SerialReadBuffer readBuffer) {
        this.inputStream = inputStream;
        this.handler=handler;
        this.readBuffer=readBuffer;
    }

    @Override
    public void run() {
        Log.d(TAG, "Input Thread Running ..");
        while (this.bytes >= 0) {
            if (isStop) {
                break;
            }
            if(inputStream==null){
                Log.d(TAG,"input stream is null");
                break;
            }
            try {
                this.bytes = inputStream.read(byteBuffer);
                if (this.bytes == 0) {
                    SystemClock.sleep(50);
                }else{
                    readBuffer.append(byteBuffer,bytes);
                    handler.sendEmptyMessage(R.id.arduino_receive_data);
                }
            } catch (IOException e) {
                this.bytes = 0;
                Log.w(TAG, "Run ending due to exception: " + e.getMessage(), e);
                break;
            }
        }
    }
}
