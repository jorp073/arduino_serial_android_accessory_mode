package xh.serial;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.os.Handler;
import android.os.Message;

import com.xh.arduino.accessory.mode.ArduinoManager;
import com.xh.arduino.accessory.mode.SerialReadBuffer;

public class MainActivityHandler extends Handler {
    private MainActivity activity;
    private SerialReadBuffer readBuffer;
    private ArduinoManager arduinoManager;
    private Context context;

    public MainActivityHandler(MainActivity activity){
        this.activity=activity;
        this.context=activity.getApplicationContext();
        this.arduinoManager = new ArduinoManager(context,this);
        readBuffer=arduinoManager.getSerialReadBuffer();
    }

    public ArduinoManager getArduinoManager(){
        return this.arduinoManager;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case R.id.handle_arduino_receive_data:
                arduinoReceiveData();
                break;
        }
    }

    private void arduinoReceiveData(){
        int size=readBuffer.size();
        for (int i = 0; i < size; i++) {
            activity.arduinoShowReceiveData(readBuffer.read());
        }
    }

}
