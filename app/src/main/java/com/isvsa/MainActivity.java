package com.isvsa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.SaveCallback;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.isvsa.min2phase.Search;

/**
 * Created by ifcan on 2017/12/14.
 */

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final boolean D = true;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final  int REQUEST_CUBE=3;

    private EditText mOutEditText;
    private Button mSendButton;
    private Button mScanButton;
    private Button mPicButton;

    private String mConnectedDeviceName = null;
    private String  Cube;

    private StringBuffer mOutStringBuffer;

    private BluetoothAdapter mBluetoothAdapter = null;

    private Service mChatService = null;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            mScanButton.setText("未连接");
            finish();
            return;
        }

        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mOutEditText.getText().toString();
                sendMessage(message);
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

        } else {
            if (mChatService == null)
                setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (mChatService != null) {
            if (mChatService.getState() == Service.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mOutEditText.getText().toString();
                sendMessage(message);
            }
        });
        mPicButton=(Button) findViewById(R.id.pic);
        mPicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,CameraEngineActivity.class);
                startActivityForResult(i,REQUEST_CUBE);
            }
        });
        mScanButton=(Button) findViewById(R.id.connect);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mChatService.getState() != Service.STATE_CONNECTED){
                Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent,REQUEST_CONNECT_DEVICE);
                }
                else
                {
                    mScanButton.setText("未连接");
                }
            }
        });

        mChatService = new Service(this, mHandler);
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mChatService != null)
            mChatService.stop();
    }

    private void sendMessage(String message) {

        if (mChatService.getState() != Service.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            mScanButton.setText("未连接");
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mChatService.write(send);
            mOutStringBuffer.setLength(0);//清空发送区
            mOutEditText.setText(mOutStringBuffer);
        }
    }
    private void setMessage(String message) {

        if (mChatService.getState() != Service.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            mScanButton.setText("未连接");
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mChatService.write(send);
            mOutStringBuffer.setLength(0);//清空发送区
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId,
                                      KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

    // The Handler that gets information back from the Service
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case Service.STATE_CONNECTED:
                            break;
                        case Service.STATE_CONNECTING:
                            break;
                        case Service.STATE_LISTEN:
                        case Service.STATE_NONE:
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if(readMessage.equals("shutdown")){
                        setMessage("ok");
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    mScanButton.setText("已连接");
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    mScanButton.setText("未连接");
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(
                    DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter
                            .getRemoteDevice(address);
                    // Attempt to connect to the device
                    mChatService.connect(device);
                }
                break;
            case REQUEST_CUBE:
                if (resultCode == Activity.RESULT_OK) {
                   Cube = data.getExtras().getString(
                            CameraEngineActivity.NCube);

                    StringBuffer s1=new StringBuffer(Cube);
                    s1.setCharAt(4, 'I');
                    s1.setCharAt(13, 'Y');
                    s1.setCharAt(22, 'W');
                    s1.setCharAt(31, 'R');
                    s1.setCharAt(40, 'G');
                    s1.setCharAt(49, 'O');

                    String s2=s1.toString();

                    s2=s2.replace('I','F');
                    s2=s2.replace('H','R');
                    s2=s2.replace('Y','U');
                    s2=s2.replace('W','D');
                    s2=s2.replace('G','B');
                    s2=s2.replace('O','L');

                    String F=s2.substring(0,9);
                    String U=s2.substring(9,18);
                    String D=s2.substring(18,27);
                    String R=s2.substring(27,36);
                    String B=s2.substring(36,45);
                    String L=s2.substring(45,54);

                    String state=U+R+F+D+L+B;
                    String result=solveCube(state);
                    mOutEditText.setText(result);

                    /*String s7=U.charAt(3)+""+F.charAt(1)+" "+
                            U.charAt(7)+""+R.charAt(1)+" "+
                            U.charAt(5)+""+B.charAt(1)+" "+
                            U.charAt(1)+""+L.charAt(1)+" "+
                            D.charAt(3)+""+F.charAt(7)+" "+
                            D.charAt(1)+""+R.charAt(7)+" "+
                            D.charAt(5)+""+B.charAt(7)+" "+
                            D.charAt(7)+""+L.charAt(7)+" "+
                            F.charAt(5)+""+R.charAt(3)+" "+
                            F.charAt(3)+""+L.charAt(5)+" "+
                            B.charAt(3)+""+R.charAt(5)+" "+
                            B.charAt(5)+""+L.charAt(3)+" "+
                            U.charAt(6)+""+F.charAt(2)+""+R.charAt(0)+" "+
                            U.charAt(8)+""+R.charAt(2)+""+B.charAt(0)+" "+
                            U.charAt(2)+""+B.charAt(2)+""+L.charAt(0)+" "+
                            U.charAt(0)+""+L.charAt(2)+""+F.charAt(0)+" "+
                            D.charAt(0)+""+R.charAt(6)+""+F.charAt(8)+" "+
                            D.charAt(6)+""+F.charAt(6)+""+L.charAt(8)+" "+
                            D.charAt(8)+""+L.charAt(6)+""+B.charAt(8)+" "+
                            D.charAt(2)+""+B.charAt(6)+""+R.charAt(8);
                    StringBuffer ss=new StringBuffer(Cube1.GetResult(s7));
                    StringBuffer sss=new StringBuffer();
                   for(int i=1;i<ss.length();i=i+2)//
                   {
                        for(int j=0;j<ss.charAt(i)-'0';j++)
                        {
                            sss.append(ss.charAt(i-1));
                        }
                   }
                    mOutEditText.setText('A'+sss.toString());
                    String message = mOutEditText.getText().toString();
                    sendMessage(message);*/
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    mScanButton.setText("未连接");
                    finish();
                }
        }
    }
    public String solveCube(String state) {

        int mask = 0;

        long t = System.nanoTime();

        Search search = new Search();
        if (!Search.isInited()) Search.init();

        int maxDepth=20;
        int maxTime=5;

        String result = search.solution(state, maxDepth, 100, 0, mask);
        long n_probe = search.numberOfProbes();
        // ++++++++++++++++++++++++ Call Search.solution method from package org.kociemba.twophase ++++++++++++++++++++++++
        while (result.startsWith("Error 8") && ((System.nanoTime() - t) < maxTime * 1.0e9)) {
            result = search.next(100, 0, mask);
            n_probe += search.numberOfProbes();
        }
        t = System.nanoTime() - t;

        // +++++++++++++++++++ Replace the error messages with more meaningful ones in your language ++++++++++++++++++++++
        if (result.contains("Error")) {
            switch (result.charAt(result.length() - 1)) {
                case '1':
                    result = "There are not exactly nine facelets of each color!";
                    break;
                case '2':
                    result = "Not all 12 edges exist exactly once!";
                    break;
                case '3':
                    result = "Flip error: One edge has to be flipped!";
                    break;
                case '4':
                    result = "Not all 8 corners exist exactly once!";
                    break;
                case '5':
                    result = "Twist error: One corner has to be twisted!";
                    break;
                case '6':
                    result = "Parity error: Two corners or two edges have to be exchanged!";
                    break;
                case '7':
                    result = "No solution exists for the given maximum move number!";
                    break;
                case '8':
                    result = "Timeout, no solution found within given maximum time!";
                    break;
            }
            return result;
        } else {
            StringBuffer temp=new StringBuffer(result);
            StringBuffer final_result=new StringBuffer();
            final_result.append('A');
            for(int i=0;i<temp.length();i++)//
            {
                switch (temp.charAt(i))
                {
                    case 'F':final_result.append('F');break;
                    case 'U':final_result.append('U');break;
                    case 'R':final_result.append('R');break;
                    case 'L':final_result.append('L');break;
                    case 'D':final_result.append('D');break;
                    case 'B':final_result.append('B');break;
                    case '\'':final_result.append(temp.charAt(i-1));final_result.append(temp.charAt(i-1));break;
                    case '2':final_result.append(temp.charAt(i-1));break;
                    default:break;
                }
            }
            return final_result.toString();
        }
        // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    }
}
