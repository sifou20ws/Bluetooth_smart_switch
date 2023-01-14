package com.example.tabbedtest;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;
import com.example.tabbedtest.Prevelent.Prevelent;
import com.google.android.material.tabs.TabLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import io.paperdb.Paper;
import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements Manual.SendDataInterface , Auto.SendDataInterfaceAuto {
    BluetoothAdapter BTAdapter;
    public boolean ConnectedState = false;
    public String deviceName = null;
    public String arduinoMsg , feedbackMsg;
    public String deviceAddress;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;
    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private MyFragmentAdapter adapter;

    Toolbar myToolbar ;
    Dialog myDialog,myDialog2 ;

    private volatile boolean stopThread = false;

    boolean mode = true , onetime= true;
    String status="" ;

    void id(){
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        myToolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewPager2);
    }

    String statePopUp="" ;

    String[] state = new String[7];
    String[] startTime = new String[7];
    String[] stopTime = new String[7];

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        id();
        setSupportActionBar(myToolbar);
        myDialog = new Dialog(this);
        myDialog2 = new Dialog(this);

        startThread();

        /* **********************Tabs frag */
        tabLayout.addTab(tabLayout.newTab().setText("Manual"));
        tabLayout.addTab(tabLayout.newTab().setText("Auto"));
        FragmentManager fragmentManager = getSupportFragmentManager();
        adapter = new MyFragmentAdapter(fragmentManager , getLifecycle());
        viewPager2.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
        /* ******************************* */
        Paper.init(this);

        if (!BTAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBT);
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }

        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        deviceAddress = getIntent().getStringExtra("device");

        if (deviceName != null){
            ShowPopup2("connecting to "+deviceName+" ..." , "Please wait!");
            deviceAddress = getIntent().getStringExtra("device");
            Toast.makeText(MainActivity.this,"Connecting to " + deviceName + "...",Toast.LENGTH_SHORT).show();

            //call a new thread to create a bluetooth connection to the selected device
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter,deviceAddress);
            createConnectThread.start();
        }

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case CONNECTING_STATUS:
                        switch(msg.arg1){
                            case 1:
                                Toast.makeText(MainActivity.this,getString(R.string.cnnected_to) + deviceName,Toast.LENGTH_SHORT).show();
                                ConnectedState = true ;
                                Prevelent.bool = true;
                                myDialog2.dismiss();
                                ShowPopup(getString(R.string.cnnected_to)+deviceName+" ..." , getString(R.string.press_to_exit));
                                break;
                            case -1:
                                Toast.makeText(MainActivity.this,getString(R.string.device_fails_to_connect) + deviceName,Toast.LENGTH_SHORT).show();
                                ConnectedState = false ;
                                Prevelent.bool = false;
                                myDialog2.dismiss();
                                ShowPopup(getString(R.string.device_fails_to_connect), getString(R.string.try_again));
                                break;
                        }
                        break;
                    case MESSAGE_READ:
                        status="";
                        arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        //Toast.makeText(MainActivity.this,arduinoMsg,Toast.LENGTH_SHORT).show();
                        if (arduinoMsg.length()<3){
                            String condition = arduinoMsg.substring(0,1);
                            switch (condition) {
                                case "2":
                                    myDialog2.dismiss();
                                    ShowPopup(getString(R.string.update_successfully), feedbackMsg);
                                    break;
                                case "0":
                                    mode = false;
                                    if (onetime) {
                                        onetime = false;
                                        ShowPopup3(getString(R.string.warning), getString(R.string.manual_mode));
                                    }
                                    break;
                                case "1":  // Manual off, app run
                                    if (!mode) {
                                        onetime = true;
                                        mode = true;
                                        ShowPopup(getString(R.string.app_enabled), getString(R.string.you_can_use_app));
                                    }
                                    break;
                            }
                        } else if (arduinoMsg.length()>3 && arduinoMsg.length()<6){
                            statePopUp = motorDecode(arduinoMsg);
                            ShowPopup("State" , statePopUp);
                        }else if(arduinoMsg.length()>6) {
                            int j=0 , w=4, y=8;
                            for (int i=0 ; i<7 ; i++){
                                state[i]=arduinoMsg.substring(j,j+4);
                                Prevelent.statee[i]=state[i];
                                j=j+12;
                                startTime[i]=arduinoMsg.substring(w,w+4);
                                Prevelent.startTimee[i]=startTime[i];
                                w=w+12;
                                stopTime[i] = arduinoMsg.substring(y,y+4);
                                Prevelent.stopTimee[i]=stopTime[i];
                                y=y+12;
                            }
                            ShowTablePopup();
                        }
                        break;
                }
            }
        };
    }
    String motorDecode(String msg){
        String data = msg.substring(0,4);
        //Toast.makeText(MainActivity.this,data,Toast.LENGTH_SHORT).show();
        if (data.equals("0000")) status = getString(R.string.all_motors_are_off) ;
        else if (data.equals("1111")) status = getString(R.string.all_motors_are_on);
        else{
            for (int i=0  ; i<4 ; i++){
                String tmp = data.substring(i,i+1);
                if (tmp.equals("1")) status=status+getString(R.string.motor)+(i+1) ;
            }
            status = status + getString(R.string.are_on);
        }
        return status;
    }

    @Override
    public void SendData(String data , String feedback) {
        if (ConnectedState){
            feedbackMsg = feedback ;
            if (mode){
                connectedThread.write(data);
                ShowPopup2(getString(R.string.updating_data) , getString(R.string.please_wait));
            }else{
                //ShowPopup3("Manual mode only" , "you cant use the app");
                ShowPopup3(getString(R.string.warning), getString(R.string.manual_mode));
            }
        }else {
            if (mode){
                Toast.makeText(MainActivity.this,getString(R.string.connect_first),Toast.LENGTH_SHORT).show();
            }else{
                //ShowPopup3("Manual mode only" , "you cant use the app");
                ShowPopup3(getString(R.string.warning), getString(R.string.manual_mode));
            }
        }
    }

    @Override
    public void SendDataAuto(String data , String feedback) {
        if (ConnectedState){
            feedbackMsg = feedback ;
            if (mode){
                connectedThread.write(data);
                ShowPopup2(getString(R.string.updating_data) , getString(R.string.please_wait));
            }else{
                //ShowPopup2("Manual mode only" , "you cant use the app");
                ShowPopup3(getString(R.string.warning), getString(R.string.manual_mode));
            }
        }else {
            if (mode){
                Toast.makeText(MainActivity.this,getString(R.string.connect_first),Toast.LENGTH_SHORT).show();
            }else{
                //ShowPopup2("Manual mode only" , "you cant use the app");
                ShowPopup3(getString(R.string.warning), getString(R.string.manual_mode));
            }
        }
    }
    public void startThread() {
        stopThread = false;
        CheckThread thread = new CheckThread(3);
        thread.start();
    }
    public void ShowPopup(String title , String content) {
        TextView ppTitle , ppContent;
        Button ppBtn;
        myDialog.setContentView(R.layout.costume_popup);
        ppTitle = myDialog.findViewById(R.id.popupTitle);
        ppTitle.setText(title);
        ppContent = myDialog.findViewById(R.id.popupContent);
        ppContent.setText(content);
        ppBtn = myDialog.findViewById(R.id.popupButton);
        ppBtn.setOnClickListener(v -> myDialog.dismiss());
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }
    public void ShowPopup3(String title , String content) {
        TextView ppTitle , ppContent;
        myDialog.setContentView(R.layout.costume_popup3);
        ppTitle = myDialog.findViewById(R.id.popup3Title);
        ppTitle.setText(title);
        ppContent = myDialog.findViewById(R.id.popup3Content);
        ppContent.setText(content);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }
    public void ShowPopup2(String title , String content) {
        TextView ppContent , ppTitle;
        myDialog2.setContentView(R.layout.costume_popup2);
        ppContent = myDialog2.findViewById(R.id.popup2Content);
        ppContent.setText(content);
        ppTitle = myDialog2.findViewById(R.id.popup2Title);
        ppTitle.setText(title);

        myDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog2.show();
    }
    public void ShowTablePopup() {
        myDialog.setContentView(R.layout.table_popup);
        myDialog.setTitle(R.string.State);
        id2();
        String[] days = Prevelent.statee;
        if (!days[0].equals("0000")) {
            String day0m1 = Prevelent.startTimee[0].substring(0,2)+":"+Prevelent.startTimee[0].substring(2,4)+"\n"+Prevelent.stopTimee[0].substring(0,2)+":"+Prevelent.stopTimee[0].substring(2,4);
            if (!days[0].startsWith("0"))
                d1m1.setText(day0m1);
            if (!days[0].startsWith("0", 1))
                d1m2.setText(day0m1);
            if (!days[0].startsWith("0", 2))
                d1m3.setText(day0m1);
            if (!days[0].startsWith("0", 3))
                d1m4.setText(day0m1);
        }
        if (!days[1].equals("0000")) {
            String day0m1 = Prevelent.startTimee[1].substring(0,2)+":"+Prevelent.startTimee[1].substring(2,4)+"\n"+Prevelent.stopTimee[1].substring(0,2)+":"+Prevelent.stopTimee[1].substring(2,4);
            if (!days[1].startsWith("0"))
                d2m1.setText(day0m1);
            if (!days[1].startsWith("0", 1))
                d2m2.setText(day0m1);
            if (!days[1].startsWith("0", 2))
                d2m3.setText(day0m1);
            if (!days[1].startsWith("0", 3))
                d2m4.setText(day0m1);
        }
        if (!days[2].equals("0000")) {
            String day0m1 = Prevelent.startTimee[2].substring(0,2)+":"+Prevelent.startTimee[2].substring(2,4)+"\n"+Prevelent.stopTimee[2].substring(0,2)+":"+Prevelent.stopTimee[2].substring(2,4);
            if (!days[2].startsWith("0"))
                d3m1.setText(day0m1);
            if (!days[2].startsWith("0", 1))
                d3m2.setText(day0m1);
            if (!days[1].startsWith("0", 2))
                d3m3.setText(day0m1);
            if (!days[1].startsWith("0", 3))
                d3m4.setText(day0m1);
        }
        if (!days[3].equals("0000")) {
            String day0m1 = Prevelent.startTimee[3].substring(0,2)+":"+Prevelent.startTimee[3].substring(2,4)+"\n"+Prevelent.stopTimee[3].substring(0,2)+":"+Prevelent.stopTimee[3].substring(2,4);
            if (!days[3].startsWith("0"))
                d4m1.setText(day0m1);
            if (!days[3].startsWith("0", 1))
                d4m2.setText(day0m1);
            if (!days[3].startsWith("0", 2))
                d4m3.setText(day0m1);
            if (!days[3].startsWith("0", 3))
                d4m4.setText(day0m1);
        }
        if (!days[4].equals("0000")) {
            String day0m1 = Prevelent.startTimee[4].substring(0,2)+":"+Prevelent.startTimee[4].substring(2,4)+"\n"+Prevelent.stopTimee[4].substring(0,2)+":"+Prevelent.stopTimee[4].substring(2,4);
            if (!days[4].startsWith("0"))
                d5m1.setText(day0m1);
            if (!days[4].startsWith("0", 1))
                d5m2.setText(day0m1);
            if (!days[4].startsWith("0", 2))
                d5m3.setText(day0m1);
            if (!days[4].startsWith("0", 3))
                d5m4.setText(day0m1);
        }
        if (!days[5].equals("0000")) {
            String day0m1 = Prevelent.startTimee[5].substring(0,2)+":"+Prevelent.startTimee[5].substring(2,4)+"\n"+Prevelent.stopTimee[5].substring(0,2)+":"+Prevelent.stopTimee[5].substring(2,4);
            if (!days[5].startsWith("0"))
                d6m1.setText(day0m1);
            if (!days[4].startsWith("0", 1))
                d6m2.setText(day0m1);
            if (!days[4].startsWith("0", 2))
                d6m3.setText(day0m1);
            if (!days[4].startsWith("0", 3))
                d6m4.setText(day0m1);
        }
        if (!days[6].equals("0000")) {
            String day0m1 = Prevelent.startTimee[6].substring(0,2)+":"+Prevelent.startTimee[6].substring(2,4)+"\n"+Prevelent.stopTimee[6].substring(0,2)+":"+Prevelent.stopTimee[6].substring(2,4);
            if (!days[6].startsWith("0"))
                d7m1.setText(day0m1);
            if (!days[6].startsWith("0", 1))
                d7m2.setText(day0m1);
            if (!days[6].startsWith("0", 2))
                d7m3.setText(day0m1);
            if (!days[6].startsWith("0", 3))
                d7m4.setText(day0m1);
        }
        ok.setOnClickListener(v -> myDialog.dismiss());
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    Button ok ;
    TextView d1m1 , d1m2 , d1m3,d1m4,d2m1,d2m2,d2m3,d2m4,d3m1,d3m2,d3m3,d3m4,d4m1,d4m2,d4m3,d4m4,d5m1,d5m2,d5m3,d5m4,d6m1,d6m2,d6m3,d6m4,d7m1,d7m2,d7m3,d7m4;

    void id2(){
        ok = myDialog.findViewById(R.id.TableButton);
        d1m1= myDialog.findViewById(R.id.d1m1);
        d1m2= myDialog.findViewById(R.id.d1m2);
        d1m3= myDialog.findViewById(R.id.d1m3);
        d1m4= myDialog.findViewById(R.id.d1m4);
        d2m1= myDialog.findViewById(R.id.d2m1);
        d2m2= myDialog.findViewById(R.id.d2m2);
        d2m3= myDialog.findViewById(R.id.d2m3);
        d2m4= myDialog.findViewById(R.id.d2m4);
        d3m1= myDialog.findViewById(R.id.d3m1);
        d3m2= myDialog.findViewById(R.id.d3m2);
        d3m3= myDialog.findViewById(R.id.d3m3);
        d3m4= myDialog.findViewById(R.id.d3m4);
        d4m1= myDialog.findViewById(R.id.d4m1);
        d4m2= myDialog.findViewById(R.id.d4m2);
        d4m3= myDialog.findViewById(R.id.d4m3);
        d4m4= myDialog.findViewById(R.id.d4m4);
        d5m1= myDialog.findViewById(R.id.d5m1);
        d5m2= myDialog.findViewById(R.id.d5m2);
        d5m3= myDialog.findViewById(R.id.d5m3);
        d5m4= myDialog.findViewById(R.id.d5m4);
        d6m1= myDialog.findViewById(R.id.d6m1);
        d6m2= myDialog.findViewById(R.id.d6m2);
        d6m3= myDialog.findViewById(R.id.d6m3);
        d6m4= myDialog.findViewById(R.id.d6m4);
        d7m1= myDialog.findViewById(R.id.d7m1);
        d7m2= myDialog.findViewById(R.id.d7m2);
        d7m3= myDialog.findViewById(R.id.d7m3);
        d7m4= myDialog.findViewById(R.id.d7m4);

    }
    /**============================ Menu =================================== */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =  getMenuInflater();
        inflater.inflate(R.menu.menu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.settings :
                Intent i = new Intent(MainActivity.this , Settings.class);
                startActivity(i);
                //Toast.makeText(MainActivity.this,"...",Toast.LENGTH_SHORT).show();
                return true ;
            case R.id.notif :
                //if (!statePopUp.equals("")) ShowPopup("State" , statePopUp);
                //else Toast.makeText(MainActivity.this,"No data to show",Toast.LENGTH_SHORT).show();
                ShowTablePopup();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** check threaad */
    class CheckThread extends Thread {
        int seconds;

        CheckThread(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public void run() {
            for (int i = 0; i < seconds; i++) {
                for (;;){
                    if (ConnectedState){
                        connectedThread.write("c");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
                        try {
                            Thread.sleep(3000);
                        }catch(InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }
    }

    /** ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /** =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n'){
                        readMessage = new String(buffer,0,bytes);
                        Log.e("Arduino Message",readMessage);
                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    /** =============================== ON/OFF Broadcast  ===============================  */
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

    int count=0;
    @Override
    public void onBackPressed() {
        count++;
        if(count ==1){
            Toast.makeText(MainActivity.this,"press twice",Toast.LENGTH_SHORT).show();
        }
        if(count ==2){
            //super.onBackPressed();
            finish();
        }

    }
}