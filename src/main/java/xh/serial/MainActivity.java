package xh.serial;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.xh.arduino.accessory.mode.ArduinoManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private final String TAG = "xh.serial.MainActivity";

    private EditText editText;
    private Button button;

    private ListView listView;
    private final int listViewMax = 10;
    private List<String> listViewData = new ArrayList<>();
    private ArrayAdapter<String> listViewAdapter;

    UsbManager usbManager;
    ArduinoManager arduinoManager;
    MainActivityHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(buttonOnClickListener);

        listView = (ListView) findViewById(R.id.listView);
        listViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listViewData);
        listView.setAdapter(listViewAdapter);

        handler=new MainActivityHandler(MainActivity.this);
        arduinoManager=handler.getArduinoManager();

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    }

    Button.OnClickListener buttonOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            String s = editText.getText().toString();
            if (s.isEmpty()) {
                // arduinoManager.write(ArduinoManager.Command.READ_DISTANCE.value());
                return;
            }

            arduinoManager.send(s);
        }
    };


    public void arduinoShowReceiveData(String s) {
        listViewData.add(0, s);
        if (listViewData.size() > listViewMax) {
            listViewData.remove(listViewMax);
        }
        listViewAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(arduinoManager!=null){
            arduinoManager.reset();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        arduinoManager.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
