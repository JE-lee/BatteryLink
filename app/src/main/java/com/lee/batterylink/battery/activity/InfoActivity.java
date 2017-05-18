package com.lee.batterylink.battery.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.lee.batterylink.R;
import com.lee.batterylink.battery.bluetooth.GreenwayCommunicationService;

public class InfoActivity extends AppCompatActivity {
    private static final String CONNECT = "connectDevice";
    private String bluetoothDeviceName = null;
    private String bluetoothDeviceAddr = null;
    public static final String EXTRA_NAME = "device name";
    public static final String EXTRA_ADDR = "device addr";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        final Intent intent = getIntent();
        bluetoothDeviceName = intent.getStringExtra(EXTRA_NAME);
        bluetoothDeviceAddr = intent.getStringExtra(EXTRA_ADDR);
        //连接设备，绑定服务
        Intent connectBLEIntent = new Intent(InfoActivity.this, GreenwayCommunicationService.class);
        bindService(connectBLEIntent,mServiceConnection,BIND_AUTO_CREATE);
        Log.d(CONNECT,"start connect device ...");
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(CONNECT,"service connected");
            //设置蓝牙状态监听
            GreenwayCommunicationService.GreenwayBatteryBinder mBinder = (GreenwayCommunicationService.GreenwayBatteryBinder)service;
            mBinder._SetConnectListener(new GreenwayCommunicationService.ConnectCallback() {
                @Override
                public void callback(boolean connectState) {
                    if(connectState)//连接上蓝牙
                    {
                        Log.d(CONNECT,"连接上蓝牙");


                    }else{//蓝牙断开
                        Log.d(CONNECT,"蓝牙断开");

                    }
                }
            });
            //连接到指定的设备
            if(bluetoothDeviceAddr == null){
                Log.d(CONNECT,"device addr null");
                return;
            }
            Log.d(CONNECT,"conntecting");
            if(mBinder._connect(bluetoothDeviceAddr)){
                Log.d(CONNECT,"connect success");
            }else{
                Log.d(CONNECT,"connect fail");
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(CONNECT,"service disconnected");

        }
    };

    public static Intent makeIntent(Context context,String name, String addr){
        Intent intent = new Intent(context,InfoActivity.class);
        intent.putExtra(EXTRA_NAME,name);
        intent.putExtra(EXTRA_ADDR,addr);
        return intent;
    }

}
