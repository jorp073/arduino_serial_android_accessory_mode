package xh.serial;

import android.content.Context;
import android.nfc.Tag;
import android.util.Log;

import com.xh.usb.accessory.mode.ArduinoManager;

public class MainActivityHandler {
    private final String TAG=MainActivityHandler.class.getSimpleName();
    private MainActivity activity;
    private ArduinoManager arduinoManager;
    private Context context;

    public MainActivityHandler(MainActivity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.arduinoManager = new ArduinoManager(context, listener);
    }

    private final ArduinoManager.Listener listener =
            new ArduinoManager.Listener() {
                @Override
                public void onReceiveData() {
                    String s = arduinoManager.read();
                    while (s != null) {
                        activity.arduinoShowReceiveData(s);
                        s = arduinoManager.read();
                    }
                }
            };

    public void send(String s) {
        arduinoManager.send(s);
    }

    public void destroy() {
        arduinoManager.destroy();
    }
}
