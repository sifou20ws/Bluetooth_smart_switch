package com.example.tabbedtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Settings extends AppCompatActivity {

    BluetoothAdapter BTAdapter;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    RecyclerView lvNewDevices;

    Toolbar myToolbar ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        lvNewDevices =  findViewById(R.id.devicelist);

        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        //mBTDevices = new ArrayList<>();
        if (!BTAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBT);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }

        // Get List of Paired Bluetooth Device
        Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
        List<Object> deviceList = new ArrayList<>();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                com.example.tabbedtest.DeviceInfoModel deviceInfoModel = new com.example.tabbedtest.DeviceInfoModel(deviceName,deviceHardwareAddress);
                deviceList.add(deviceInfoModel);
            }
            // Display paired device using recyclerView
            RecyclerView recyclerView = findViewById(R.id.devicelist);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            DeviceListAdapter deviceListAdapter = new DeviceListAdapter(this,deviceList);
            recyclerView.setAdapter(deviceListAdapter);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }


    }


    /** //////////////////////////////////////////////////////
     ON/OFF Broadcast    */
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(BTAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BTAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        //textView.setText("bluetooth off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //textView.setText("bluetooth turning off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        //textView.setText("bluetooth on");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        //textView.setText("bluetooth turning on");
                        break;
                }
            }
        }
    };

}