package com.lee.batterylink.battery.activity;


import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lee.batterylink.R;
import com.lee.batterylink.battery.bluetooth.GreenwayCommunicationService;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class scanActivity extends AppCompatActivity {

    private static final String VIEW = "scanActityView";
    private static final String SCAN = "scanDevice";
    private static final int REQUESTBLECODE = 1;

    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.btn_add_bar)
    Button btnAddBar;
    @BindView(R.id.rl_title)
    RelativeLayout rlTitle;
    @BindView(R.id.iv_scan_1)
    ImageView ivScan1;
    @BindView(R.id.iv_scan_2)
    ImageView ivScan2;
    @BindView(R.id.iv_scan_scan)
    ImageView ivScanScan;
    @BindView(R.id.tv_scan)
    TextView tvScan;
    @BindView(R.id.iv_device)
    ImageView ivDevice;
    @BindView(R.id.tv_device)
    TextView tvDevice;
    @BindView(R.id.lv_device)
    ListView lvDevice;

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Handler mHandler = new Handler();
    private Runnable stopRunnable = null;
    private boolean mScanning = false;
    private int SCAN_PERIOD = 20000;
    private ObjectAnimator rotateAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        ButterKnife.bind(this);
        //查看是否支持BLE，不支持就finish
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(scanActivity.this,"BLE is not supported in your phone",Toast.LENGTH_LONG).show();
            Log.d(VIEW,"BLE is not supported in your phone");
            finish();
        }
        //蓝牙管理器
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        initView();//初始化界面
        setAnimation();//设置扫描动画属性
        Log.d(VIEW,"onCreate finish");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUESTBLECODE);
            }
            changeState();
            return;
        }
        //rotateAnimator.start();
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        lvDevice.setAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);

        super.onResume();
        Log.d(VIEW,"onResume finish");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(VIEW,"onStop");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUESTBLECODE && resultCode == RESULT_CANCELED){
            Log.d(VIEW,"用户选择不打开蓝牙");
            finish();
        }
    }

    private void initView() {
        btnBack.setVisibility(View.VISIBLE);
        btnAddBar.setVisibility(View.GONE);
        tvTitle.setText(R.string.scandevices);
        ivDevice.setVisibility(View.VISIBLE);
        tvDevice.setVisibility(View.VISIBLE);
        lvDevice.setVisibility(View.GONE);

        tvScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stopRunnable!=null)
                    mHandler.removeCallbacks(stopRunnable);
                if (mScanning) {
                    //正在搜索,于是停止
                    scanLeDevice(false);
                } else {
                    scanLeDevice(true);
                }
            }
        });

        lvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device == null)
                {
                    Log.d(VIEW,"device == null");
                    return;
                }
               // gotoMainActivity(device);
                //关闭搜索
                if(mScanning)
                  scanLeDevice(false);
               //跳转到Info Activity
                Intent intent = InfoActivity.makeIntent(scanActivity.this,device.getName(),device.getAddress());
                startActivity(intent);
                Log.d(VIEW,"start infoActivity intent");
            }
        });

    }
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //LogFactory.createLog().d("find");
                            Log.d(SCAN,"find device : name:" + device.getName() + "address:" + device.getAddress());
                            if (lvDevice.getVisibility() != View.VISIBLE) {
                                ivDevice.setVisibility(View.GONE);
                                tvDevice.setVisibility(View.GONE);
                                lvDevice.setVisibility(View.VISIBLE);
                            }
                            mLeDeviceListAdapter.addDevice(device);
                            Log.d(SCAN,"mLeDeviceListAdapter count : " +mLeDeviceListAdapter.getCount() );
                            mLeDeviceListAdapter.notifyDataSetChanged();
                          //  if(type == Comments.type_haveb){
                             //   if(sn.equals(device.getAddress()) || sn.equals(device.getName())){
//                                    TipsToastUtil.smile(CustomApplication.getContext(),
//                                            getResources().getString(R.string.finddevice));
                                    //gotoMainActivity(device);

                              //  }
                          //  }
                        }
                    });
                }
            };
    //true为开始搜索
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            stopRunnable = new Runnable() {
                @Override
                public void run() {
                    if(scanActivity.this.isFinishing()){
                        return;
                    }
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    //invalidateOptionsMenu();
                    changeState();
                   // if(type==Comments.type_haveb){
                       // TipsToastUtil.error(BatteryScanActivity.this,getResources().getString(R.string.nofind));
                   // }
                }
            };
            mHandler.postDelayed(stopRunnable, SCAN_PERIOD);//扫描SCAN_PERIOD
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            Log.d(SCAN,"start scan ble device");
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        changeState();
    }
    void changeState() {
        if (!mScanning) {
            tvScan.setText(R.string.startscan);
            rotateAnimator.cancel();
            ivScan1.setVisibility(View.VISIBLE);
            ivScan2.setVisibility(View.GONE);
        } else {
            //正在搜索,
            tvScan.setText(R.string.stopscan);
            rotateAnimator.start();
            ivScan1.setVisibility(View.GONE);
            ivScan2.setVisibility(View.VISIBLE);
        }
    }



    void setAnimation() {
        rotateAnimator = ObjectAnimator.ofFloat(ivScanScan, "rotation", 0, 360f);
        rotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        rotateAnimator.setRepeatMode(ObjectAnimator.RESTART);
        rotateAnimator.setDuration(1000);
    }


    //ListView Adapter
    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = scanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);

            }else{

                int de = mLeDevices.indexOf(device);
                if(de != -1){
                    mLeDevices.set(de,device);
                }
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.list_item_historydevices, null);
                viewHolder = new ViewHolder();
                viewHolder.tv_device_name = (TextView) view.findViewById(R.id.tv_device_name);
                viewHolder.tv_last_connect = (TextView) view.findViewById(R.id.tv_last_connect);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.tv_device_name.setText(deviceName);
            else
                viewHolder.tv_device_name.setText(R.string.unknown_device);
            viewHolder.tv_last_connect.setText(device.getAddress());

            return view;
        }
    }

    static class ViewHolder {
        TextView tv_device_name;
        TextView tv_last_connect;
    }
}
