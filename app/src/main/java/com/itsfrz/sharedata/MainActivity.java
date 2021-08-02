package com.itsfrz.sharedata;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    TextView currentStatus,currentFileName;
    RelativeLayout bluetoothLayout;

    ToggleButton bluetoothToggleButton;
    private int REQUEST_ENABLE_BT;
    ListView listOfDevices;
    Button listDevices, audioSelect, imageSelect,resetButton,listen;

    BluetoothAdapter myBluetoothAdapter;
    BluetoothDevice[] btArray;
    Intent enableBluetoothIntent;
    int requestCodeForEnable;


    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int DISCOVER_DURATION = 300;
    private static final int REQUEST_BLU = 1;
    String path;
    private static final String APP_NAME = "ShareData";
    private static final UUID MY_UUID = UUID.fromString("5459ccef-90c8-490c-a74c-e74b4c3f857b");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestCodeForEnable = 1;


        findViewByAllId();
        implementListeners();

    }

    private void implementListeners() {


        bluetoothToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothToggleButton.isChecked()){
                    if(myBluetoothAdapter == null){
                        Toast.makeText(MainActivity.this, "Bluetooth is not supported in these device", Toast.LENGTH_SHORT).show();
                    }else{
                        if(!myBluetoothAdapter.isEnabled()){
                            startActivityForResult(enableBluetoothIntent,requestCodeForEnable);
                        }
                    }
                }else{
                    if(myBluetoothAdapter.isEnabled()){
                        myBluetoothAdapter.disable();
                    }
                }
            }
        });
        listDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothLayout.setVisibility(View.VISIBLE);
                Set<BluetoothDevice> bt = myBluetoothAdapter.getBondedDevices();
                String[] strings = new String[bt.size()];
                btArray = new BluetoothDevice[bt.size()];
                int index = 0;
                if(bt.size() > 0){
                    for (BluetoothDevice device : bt){
                        btArray[index] = device;
                        strings[index] = device.getName();
                        index+=1;
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,strings);
                    listOfDevices.setAdapter(arrayAdapter);
                }
            }
        });
        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerClass serverClass = new ServerClass();
                serverClass.start();
            }
        });
        listOfDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClientClass clientClass = new ClientClass(btArray[position]);
                clientClass.start();

                currentStatus.setText("Connecting");
            }
        });
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what){
                case STATE_LISTENING:
                    currentStatus.setText("Listening");
                    break;

                case STATE_CONNECTING:
                    currentStatus.setText("Connecting");
                    break;

                case STATE_CONNECTED:
                    currentStatus.setText("Connected");
                    break;

                case STATE_CONNECTION_FAILED:
                    currentStatus.setText("Connection Failed");
                    break;

                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[])msg.obj;
                    String tempMsg = new String(readBuff,0,msg.arg1);

                    break;
            }

            return true;
        }
    });

    private void findViewByAllId() {

        listOfDevices = (ListView) findViewById(R.id.listOfDevices);
        bluetoothLayout = (RelativeLayout) findViewById(R.id.bluetoothAnimate);
        listDevices = (Button) findViewById(R.id.listDevices);
        audioSelect = (Button) findViewById(R.id.audioSelect);
        imageSelect = (Button) findViewById(R.id.imageSelect);
        resetButton = (Button) findViewById(R.id.resetButton);
        listen = (Button) findViewById(R.id.listenDevices);
        currentFileName = (TextView) findViewById(R.id.currentFileName);
        currentStatus = (TextView) findViewById(R.id.currentStatus);
        bluetoothToggleButton = (ToggleButton) findViewById(R.id.bluetoothToggleButton);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestCodeForEnable) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth is Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth Enabling Cancelled", Toast.LENGTH_SHORT).show();
            }
        }



    }




    public void sendAllData(View view) {
        if (path == null) {
            Toast.makeText(this, "Please select file first", Toast.LENGTH_SHORT).show();
            return;
        }



    }

    public void audioSelect(View view) {

        Intent mediaIntent = new Intent(Intent.ACTION_GET_CONTENT);
        mediaIntent.setType("audio/*"); //set mime type as per requirement
        startActivityForResult(mediaIntent, 1001);

    }

    public void imageSelect(View view) {
        Intent mediaIntent = new Intent(Intent.ACTION_GET_CONTENT);
        mediaIntent.setType("image/*"); //set mime type as per requirement
        startActivityForResult(mediaIntent, 1002);
    }


    private class ServerClass extends Thread{
        private BluetoothServerSocket serverSocket;

        public ServerClass(){
            try {
                serverSocket = myBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME,MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run(){
            BluetoothSocket socket = null;

            while (socket == null){
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();

                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);


                }

                if(socket != null)
                {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);


                    //write code for send receive
                    break;
                }
            }
        }

    }

    private class ClientClass extends Thread{

        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass (BluetoothDevice device1){
            device = device1;

            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run(){
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();

                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }

    }

    private class SendReceive extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket){
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;

        }

        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;
            while (true){
                try {
                    bytes =  inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



}

