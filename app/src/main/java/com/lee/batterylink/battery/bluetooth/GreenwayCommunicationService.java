/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lee.batterylink.battery.bluetooth;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import com.lee.batterylink.battery.Factory.Factory;
import com.lee.batterylink.battery.protocol.BatteryInformation;
import com.lee.batterylink.battery.Definition.Definition;
import com.lee.batterylink.battery.protocol.GreenwayProtocol;


import java.util.Map;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import android.os.Binder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by lee on 2017/5/16.
 */

public class GreenwayCommunicationService extends Service{

    private static final class connectState{
        public static final int STATE_DISCONNECTED = 0;
        public static final int STATE_CONNECTING = 1;
        public static final int STATE_CONNECTED = 2;
    }

    private static final class orderType{
        public static final byte nullcode = 0;//无
        public static final byte IICwrite_Firstcode = 1;//IIC解锁
        public static  final byte IICwrite_Secondcode = 2;//IIC写数据
        public static final byte IICread_Firstcode = 3;//IIC一段 读
        public static final byte IICread_secondcode =4;//IIC写二段回
        public static final byte RamReadCode = 5;//Ram操作
        public static final byte RamWriteCode = 6;//Ram操作
        public static final byte FlashWriteCode =  7;//Flash 写
        public static final int wait = 100;//命令的超时时间
        public static final byte ackFrameHead = Definition.GREENWAYACKFRAMEHEAD;//回复的帧头
    }
    //博力威IIC操作命令
    private static final class orderFrame{
        private final static  byte[] Unlock = {0x46,0x16,0x00,0x00,0x04,0x01,0x5a,0x4d,0x46,0x4e};//IIC写解锁
        private final static  byte[] UnlockAck = {0x47,0x16,0x00,0x00,0x00,0x5d};//IIC写解锁下位机回复
        private final static byte[] IIC_writeSecondAck = {0x47,0x16,0x00,0x77,0x00,(byte)0xd4};//IIC第二段写回复
        private final static byte[]IIC_readFirstAck = {0x47,0x16,0x00,0x77,0x00,(byte)0xd4};//IIC读第一段回复
        private final static byte[] Flash_writeAck = {0x41,0x43,0x4b};//Flash 写回复命令
    }
    private static final String TAG = "GBatteryBLEService";
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    //将蓝牙的读写信道单独记录下来，其他的信道依然存入特性地址集合中
    private final static String ReadUUID = "0000ffe4-0000-1000-8000-00805f9b34fb";//规定的读特征
    private final static String WriteUUID = "0000ffe9-0000-1000-8000-00805f9b34fb";//规定的写特征
    private BluetoothGattCharacteristic readCharacteristic;
    private BluetoothGattCharacteristic sendCharacteristic;

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);//心率计

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = connectState.STATE_DISCONNECTED;

    //特性地址集合
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();//特性地址集合
    //命令操作
    private byte currentOrdercode = orderType.nullcode;//指示当前的命令操作类型
    private  byte[] sendbuff= new byte[1024];//发送数据缓冲区
    //private  boolean IICOpetation_Islock = false;//对IIC命令加锁，因为IIC操作需要两步
    private int sendIndex = 0;//发送游标
    private int sendLength = 0;//发送长度
    private boolean IsSendFinish = false;//指示20个byte的数据是否发送完成//注意此时并未对GattCallback的数据进行检验
    private boolean Operation_SuccessFlag = false;//指示命令是否成功，注意每一条IIC命令都有两次来回的应答过程。

    //命令地址，与命令TYPE一起构成命令的识别
    private int orderAddr = 0;

    // 0:无 Unlockcode：解锁 IICwrite_datacode： IIC 二段写命令
    private byte[] IICWritedataBuff =new byte[256];//存储IIC写的时候的数据
    private int IICWritedataLength = 0;//写的数据的长度
    private byte[] IICReadOrderBuff =new byte[6];//存储IIC读第二段命令
    private byte[] IICReadDatabuff = new byte[256];//IIC读第二段返回的数据
    private int ReadIndex = 0;//用来记录已经读了或者写了多少个数据，因为BLE的一帧只有20个byte数据

    private final int  MSGCODE  = 1000;//指示检测超时Handler里的消息Code代码
    private boolean IsIdle = true;//指示蓝牙是否空闲，是否可以发送命令
    //回调函数
    //连接时的回调函数
    public interface ConnectCallback{
        public void callback(boolean connectState);
    }
    private ConnectCallback connectCallback = null;
    //命令的回调函数
    public interface OrderCallback{
        public void callback(String orderType,int orderAddr,byte[] data,boolean IsSuccess);
    }
    private OrderCallback orderCallback = null;
    public static final class TYPE{
        public static final String TYPE_RAM_READ = "type_ram_read";
        public static final String TYPE_RAM_WRITE = "type_ram_write";
        public static final String TYPE_IIC_READ = "type_iic_read";
        public static final String TYPE_IIC_WRITE = "type_iic_write";
        public static final String TYPE_FLASH_WRITE = "type_flash_write";

    }

    public static final class GreenwayFrame{
        //IIC操作
        public  final static String WRITE =
                "com.example.bluetooth.le.type_write";//命令的类型
        public final static String READ =
                "com.example.bluetooth.le.type_read";
    }

    private GreenwayBatteryBinder mBinder = new GreenwayBatteryBinder();
    //代理接口
    public class GreenwayBatteryBinder extends Binder{
        //连接到指定地址的BLE设备
        public boolean _connect(final String address){
            if(connect(address)){
                return true;
            }
            return false;
        }
        //断开BLE连接
        public void _disconnect(){
            disconnect();
        }
        //设置连接监听
        public void _SetConnectListener(ConnectCallback callback){
            SetConnectListener(callback);
        }

        //命令函数
        public boolean _StartReadRam(byte address,byte datalength,OrderCallback callback){
            return StartReadRam( address, datalength,callback);
        }

        public boolean _StartWriteRam(byte address,byte[] writedata,OrderCallback callback){
            return  StartWriteRam(address,writedata,callback);
        }

        public boolean _StartReadIIC(int address,byte datalength,OrderCallback callback){
            return StartReadIIC( address, datalength,callback);
        }

        public boolean _StartWriteIIC(int address,byte data[],OrderCallback callback){
            return StartWriteIIC( address, data,callback);
        }

        public boolean _StartWriteFlash(byte data[],OrderCallback callback){
            return StartWriteFlash( data,callback);
        }

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        new BatteryRamDataThread("Read battery info thread").start();
        Log.d(TAG,"Read battery info thread start");
        return mBinder;
    }

    //蓝牙连接的回调函数
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            //连接成功
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //开始发现服务
                mBluetoothGatt.discoverServices();//
                mConnectionState = connectState.STATE_CONNECTED;
                if(connectCallback != null)
                    connectCallback.callback(true);
            } //连接失败
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //设置回调函数来通知应该做什么
                mConnectionState = connectState.STATE_DISCONNECTED;
                if(connectCallback != null)
                    connectCallback.callback(false);

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //开始扫描特征
            //开始扫描特性
            List<BluetoothGattService> gattServices = getSupportedGattServices();
            if (gattServices == null) return;
            String uuid = null;
           // String unknownServiceString = getResources().getString(R.string.unknown_service);
            String unknownServiceString = "Unknown Service";
           // String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
            String unknownCharaString = "Unknown Characteristic";
            ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
            ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                    = new ArrayList<ArrayList<HashMap<String, String>>>();
            mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

            // Loops through available GATT Services.
            for (BluetoothGattService gattService : gattServices) {
                HashMap<String, String> currentServiceData = new HashMap<String, String>();
                uuid = gattService.getUuid().toString();
                currentServiceData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
                currentServiceData.put(LIST_UUID, uuid);
                gattServiceData.add(currentServiceData);

                ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                        new ArrayList<HashMap<String, String>>();
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> charas =
                        new ArrayList<BluetoothGattCharacteristic>();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    charas.add(gattCharacteristic);
                    HashMap<String, String> currentCharaData = new HashMap<String, String>();
                    uuid = gattCharacteristic.getUuid().toString();
                    currentCharaData.put(
                            LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                    currentCharaData.put(LIST_UUID, uuid);
                    gattCharacteristicGroupData.add(currentCharaData);
                    if (uuid.equals(ReadUUID)) {
                        readCharacteristic = gattCharacteristic;
                    } else if (uuid.equals(WriteUUID)) {
                        sendCharacteristic = gattCharacteristic;
                    }
                }
                mGattCharacteristics.add(charas);
                gattCharacteristicData.add(gattCharacteristicGroupData);


            }
            //监听读characteristic
            if (readCharacteristic != null) {
                setCharacteristicNotification(
                        readCharacteristic, true);
            } else {
                Log.d(TAG, "NO readCharacteristic");
            }
            if (sendCharacteristic == null) {
                Log.d(TAG,"NO writeCharacteristic");
            }

            //开启发送数据的线程
            if(sendCharacteristic != null && readCharacteristic != null)
            {
                new DataThread().start();
                Log.d(TAG,"DataThread start");
            }
            else
            {
                Log.d(TAG,"读写信道未发现，将不能操作");
                return;
            }
            //服务发现成功
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                //设置回调函数来
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
        /**
         * Retrieves a list of supported GATT services on the connected device. This should be
         * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
         *
         * @return A {@code List} of supported services.
         */
        /**
         * Retrieves a list of supported GATT services on the connected device. This should be
         * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
         *
         * @return A {@code List} of supported services.
         */
        private List<BluetoothGattService> getSupportedGattServices() {
            if (mBluetoothGatt == null)
                return null;
            return mBluetoothGatt.getServices();

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        //setCharacteristicNotifition callback
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.v(TAG,"onCharacteristicChanged read data:"  + Factory.bytesToHexString(characteristic.getValue()));
            if(currentOrdercode == orderType.IICwrite_Firstcode)//解锁
            {
                final byte[] data = characteristic.getValue();
                GreenwayProtocol.SolveByteArray(orderType.ackFrameHead,data);
                boolean IsnotEqual = false;
                for(int i = 0;i<orderFrame.UnlockAck.length;i++)
                {
                    if(data[i] != orderFrame.UnlockAck[i])
                    {
                        IsnotEqual = true;
                        break;
                    }
                }
                if(IsnotEqual)
                    Operation_SuccessFlag = false;//等待waitAckHandler 复位
                else{
                    //写第二段数据
                    currentOrdercode =  orderType.IICwrite_Secondcode;
                    senddata(IICWritedataBuff,IICWritedataLength);
                }

            }else if (currentOrdercode == orderType.IICwrite_Secondcode)//IIC写第二段
            {

                final byte[] data = characteristic.getValue();
                GreenwayProtocol.SolveByteArray(orderType.ackFrameHead,data);
                boolean IsnotEqual = false;
                for(int i = 0;i<orderFrame.IIC_writeSecondAck.length;i++)
                {
                    if(data[i] != orderFrame.IIC_writeSecondAck[i])
                    {
                        IsnotEqual = true;
                        break;
                    }
                }
                if(!IsnotEqual)
                {
                    Operation_SuccessFlag = true;
                    waitAckHandler.removeMessages(MSGCODE);
                }
                else
                {
                    Operation_SuccessFlag = false;
                }

                IsIdle = true;//将蓝牙立刻置为空闲状态
                // broadcastUpdate(ACTION_IIC_ORDER, characteristic);
                //设置回调函数来执行应该干什么
                if(orderCallback != null)
                    orderCallback.callback(TYPE.TYPE_IIC_WRITE,orderAddr,data,Operation_SuccessFlag);
            }
            else if (currentOrdercode == orderType.IICread_Firstcode)//IIC 读第一段
            {
                final byte[] data = characteristic.getValue();
                GreenwayProtocol.SolveByteArray(orderType.ackFrameHead,data);
                boolean IsnotEqual = false;
                for(int i = 0;i<orderFrame.IIC_readFirstAck.length;i++)
                {
                    if(data[i] != orderFrame.IIC_readFirstAck[i])
                    {
                        IsnotEqual = true;
                        break;
                    }
                }
                if(IsnotEqual)
                    Operation_SuccessFlag  = false;//等待waitAckHandler 复位
                else{
                    //写第二段数据
                    currentOrdercode = orderType.IICread_secondcode;
                    senddata(IICReadOrderBuff,IICReadOrderBuff.length);
                }
            }
            else if ((currentOrdercode == orderType.IICread_secondcode) ||(currentOrdercode == orderType.RamReadCode) )//IIC读的第二段
            {
                final byte[] data = characteristic.getValue();
                for(int i = 0;i<data.length;i++)
                {
                    IICReadDatabuff[ReadIndex] = data[i];
                    ReadIndex++;
                }
                ReadIndex -= GreenwayProtocol.SolveByteArray(orderType.ackFrameHead,IICReadDatabuff);
                if(ReadIndex >= IICReadDatabuff[4] + 6)//判断是否接收完一帧
                {
                    boolean IsavaliableFrame = false;
                    byte check = 0;
                    for (int i = 0; i < ReadIndex -1; i++) {
                        check += IICReadDatabuff[i];
                    }
                    if (check == IICReadDatabuff[ReadIndex - 1]) IsavaliableFrame = true;
                    if (IsavaliableFrame) {
                        Operation_SuccessFlag = true;
                        //回复的数据有效则清楚消息队列中的消息
                        waitAckHandler.removeMessages(MSGCODE);
                    } else {
                        Operation_SuccessFlag = false;
                    }
                    IsIdle = true;//将蓝牙立刻置为空闲状态
                    //设置回到函数来通知应该干什么
                    byte returnData[] = new byte[ReadIndex+1];
                    System.arraycopy(IICReadDatabuff,0,returnData,0,ReadIndex+1);
                    if(orderCallback != null){
                        if(currentOrdercode == orderType.IICread_secondcode){
                            orderCallback.callback(TYPE.TYPE_IIC_READ,orderAddr,returnData,Operation_SuccessFlag);
                        }
                        else{
                            orderCallback.callback(TYPE.TYPE_RAM_READ,orderAddr,returnData,Operation_SuccessFlag);
                            Log.d(TAG,"call ordercallback");
                        }

                    }else{
                        Log.d(TAG,"orderCallback == null");
                    }


                }
            } else if (currentOrdercode == orderType.RamWriteCode) {//Ram写数据

            }else if (currentOrdercode == orderType.FlashWriteCode){//Flash 写数据
                final byte[] data = characteristic.getValue();
                boolean IsnotEqual = false;
                for(int i =0;i<orderFrame.Flash_writeAck.length;i++){
                    if(data[i] != orderFrame.Flash_writeAck[i]){
                        IsnotEqual = true;
                        break;
                    }
                }
                if(!IsnotEqual){
                    Operation_SuccessFlag = true;
                }else{
                    Operation_SuccessFlag = false;
                }
                IsIdle = true;//将蓝牙立刻置为空闲状态
                //设置回到函数来通知应该干什么
                if(orderCallback != null)
                    orderCallback.callback(TYPE.TYPE_FLASH_WRITE,orderAddr,data,Operation_SuccessFlag);
            }
            else if (currentOrdercode == orderType.nullcode)//无操作命令正常监听数据
            {
                //设置回到函数来通知应该干什么
            }


        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            byte[] writedata = characteristic.getValue();
            //已经写完一帧数据，发送广播//并未对发送的数据做检验
            Log.d(TAG,"发送数据"+ Factory.bytesToHexString(writedata));
            IsSendFinish = true;
            if(sendLength == sendIndex )//发送完数据
            {
                //设置回调函数来通知应该干什么
            }
        }
    };

    //连接到指定地址的蓝牙设备
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    private boolean connect(final String address ){
        // For API level 18 and above, get a reference to BluetoothAdapter through
        if((mConnectionState == connectState.STATE_CONNECTING) || ((mConnectionState == connectState.STATE_CONNECTED))){
            return true;
        }
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.d(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        //连接到指定地址
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {//这个是重新连接函数
                mConnectionState = connectState.STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);//连接到指定的蓝牙设备
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = connectState.STATE_CONNECTING;
        return true;

    }

    //断开蓝牙连接
    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    private void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }
    //关闭服务
    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    private void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    //监听信道



    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                               boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    //发送数据//这个函数应该线程同步，防止多线程操作扰乱数据
    //普通的发送数据
    //sendLength为发送数据的长度
    //IsSendFinish = true；
    //sendIndex:指示已经发送多少个数据
    //sendbuff[]：发送数据存储区
    private boolean senddata(byte[] data,int length) {
        synchronized(this){
            IsIdle = false;
            sendLength =length;
            IsSendFinish = true;
            sendIndex = 0;
            for (int i = 0; i < sendLength; i++) {
                sendbuff[i] = data[i];
            }
            return true;
        }
    }
    //Greenway 命令操作函数
    //Ram读
    private  boolean StartReadRam(byte address,byte datalength,OrderCallback callback){
        if(IsIdle){
            this.orderCallback  = callback;
            ReadIndex = 0;
            orderAddr = address;
            byte[] data = new byte[6];
            data[0] = Definition.GREENWAYREQUESTFRAMEHEAD;
            data[1] = Definition.BATTERYSLAVEADDR;
            data[2] = Definition.READ;
            data[3] = address;
            data[4] = datalength;
            byte sum = 0;
            for(int i =0;i<data.length-1;i++){
                sum += data[i];
            }
            data[5] = sum;
            currentOrdercode = orderType.RamReadCode;
            senddata(data,data.length);
            waitAckHandler.sendEmptyMessageDelayed(MSGCODE, orderType.wait);//IIC写为250s延时
            return true;
        }else{
            return false;
        }

    }
    //Ram 写
    private boolean StartWriteRam(byte address,byte[] writedata,OrderCallback callback){
        if(IsIdle)
        {
            this.orderCallback  = callback;
            orderAddr = address;
            byte[]  data= new byte[6];
            data[0] = Definition.GREENWAYREQUESTFRAMEHEAD;
            data[1] = Definition.BATTERYSLAVEADDR;
            data[2] = Definition.WRITE;
            data[3] = address;
            data[4] = (byte)writedata.length;
            for(int i =0;i<writedata.length;i++){
                data[5 +i] = writedata[i];
            }
            byte sum = 0;
            for(int i =0;i<data.length-1;i++){
                sum += data[i];
            }
            data[data.length-1] = sum;
            currentOrdercode = orderType.RamWriteCode;
            senddata(data,data.length);
            waitAckHandler.sendEmptyMessageDelayed(MSGCODE, orderType.wait);//IIC写为250s延时
            return true;
        }else{
            return false;
        }


    }
    //IIC读
    private boolean StartReadIIC(int address,byte datalength,OrderCallback callback){
        if(IsIdle){
            this.orderCallback = callback;
            orderAddr = address;
            currentOrdercode = orderType.IICread_Firstcode;
            ReadIndex = 0;
            IICWritedataBuff[0] = Definition.GREENWAYREQUESTFRAMEHEAD;
            IICWritedataBuff[1] = Definition.BATTERYSLAVEADDR;
            IICWritedataBuff[2] = Definition.WRITE;
            IICWritedataBuff[3] =  Definition.IICWRITE;
            IICWritedataBuff[4] = 0x05;
            IICWritedataBuff[5] = 0x02;
            IICWritedataBuff[6] =(byte) 0xA1;
            IICWritedataBuff[7] = (byte)address;
            IICWritedataBuff[8] = (byte)(address>>8);
            IICWritedataBuff[9] = datalength;
            byte sum =0;
            for(int i =0;i<10;i++)
            {
                sum += IICWritedataBuff[i];
            }
            IICWritedataBuff[10] = sum;
            IICWritedataLength = 11;

            IICReadOrderBuff[0] = Definition.GREENWAYREQUESTFRAMEHEAD;
            IICReadOrderBuff[1] = Definition.BATTERYSLAVEADDR;
            IICReadOrderBuff[2] = Definition.READ;
            IICReadOrderBuff[3] = Definition.IICREAD;
            IICReadOrderBuff[4] =  datalength;
            sum =0;
            for(int i =0;i<5;i++)
            {
                sum += IICReadOrderBuff[i];
            }
            IICReadOrderBuff[5] = sum;
            Operation_SuccessFlag= false;
            currentOrdercode = orderType.IICread_Firstcode;//当前操作类型为一段读
            senddata(IICWritedataBuff,IICWritedataLength);
            waitAckHandler.sendEmptyMessageDelayed(MSGCODE, orderType.wait);//IIC写为250s延时
            return true;
        }
        else
        {
            return false;
        }

    }
    //IIC写
    private boolean StartWriteIIC(int address,byte data[],OrderCallback callback){
        if(IsIdle){
            this.orderCallback = callback;
            orderAddr = address;
            ReadIndex = 0;
            IICWritedataBuff[0] = Definition.GREENWAYREQUESTFRAMEHEAD;
            IICWritedataBuff[1] = Definition.BATTERYSLAVEADDR;
            IICWritedataBuff[2] = Definition.WRITE;
            IICWritedataBuff[3] = Definition.IICWRITE;
            IICWritedataBuff[4] = (byte) (data.length + 5);
            IICWritedataBuff[5] = (byte)0x02;
            IICWritedataBuff[6] = (byte)0xa0;
            IICWritedataBuff[7] =(byte) ((address & 0xff ));
            IICWritedataBuff[8] =(byte) ((address & 0xff )>> 8);
            IICWritedataBuff[9] = (byte)data.length;

            for(int i =0;i<data.length;i++)
            {
                IICWritedataBuff[i + 10] = data[i];
            }
            IICWritedataLength = data.length + 11;//发送数据长度
            //和校验
            int checksum = 0;
            for(int i =0;i< data.length + 10;i++)
            {
                checksum += IICWritedataBuff[i];
            }
            IICWritedataBuff[IICWritedataLength-1] = (byte)checksum;

            Operation_SuccessFlag= false;
            currentOrdercode = orderType.IICwrite_Firstcode;
            senddata(orderFrame.Unlock,orderFrame.Unlock.length);
            waitAckHandler.sendEmptyMessageDelayed(MSGCODE, orderType.wait);//IIC写为250s延时
            return true;

        }else{
            return false;
        }
    }
    //Flash编程
    private boolean StartWriteFlash(byte data[],OrderCallback callback){
        if(IsIdle){
            this.orderCallback = callback;
            orderAddr = 0;
            ReadIndex = 0;
            Operation_SuccessFlag= false;
            currentOrdercode = orderType.FlashWriteCode;
            senddata(data,data.length);
            waitAckHandler.sendEmptyMessageDelayed(MSGCODE, orderType.wait);//IIC写为250s延时
            return true;
        }else{
            return false;
        }

    }
    //设置连接监听
    private void SetConnectListener(ConnectCallback mConnectCallback){
        if(mConnectCallback != null){
            connectCallback = mConnectCallback;
        }
    }
    //设置命令监听
    private void SetOrderCallback(OrderCallback mOrderCallback){
        if(mOrderCallback != null){
            orderCallback = mOrderCallback;
        }
    }

    //发送线程
    private class DataThread extends Thread{
        @Override
        public void run() {
            super.run();
            while(true)
            {
                if((sendLength > sendIndex ) && IsSendFinish && sendCharacteristic != null)
                {
                    if((sendLength - sendIndex ) >= 20)
                    {
                        byte[] data = new byte[20];
                        for(int i =0;i<20;i++)
                        {
                            data[i] = sendbuff[sendIndex + i];
                        }
                        IsSendFinish = false;
                        sendCharacteristic.setValue(data);
                        writeCharacteristic(sendCharacteristic);
                        sendIndex += 20;

                    }
                    else if (((sendLength - sendIndex ) < 20) && (sendLength - sendIndex ) >0)
                    {
                        int length = sendLength - sendIndex ;
                        byte[] data = new byte[length];
                        for(int i =0;i<length;i++)
                        {
                            data[i] = sendbuff[sendIndex + i];
                        }
                        IsSendFinish = false;
                        if(sendCharacteristic != null)
                            sendCharacteristic.setValue(data);
                        else
                        {
                            Log.d("TAG","sendCharacteristic is null in " + Thread.currentThread());
                        }
                        writeCharacteristic(sendCharacteristic);
                        sendIndex += length;
                    }
                }else
                {
                    try {
                        sleep(1);//立即放弃CPU
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    //发送服务
    private void  writeCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    private Handler waitAckHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what)
            {
                case MSGCODE:
                    if(!Operation_SuccessFlag) // 没有成功0
                    {
                        Operation_SuccessFlag = false;
                        IsIdle = true;
                        //设置回调函数来通知应该干什么
                        if(orderCallback != null)
                            orderCallback.callback(null,orderAddr,null,Operation_SuccessFlag);
                    }
                    break;

            }

        }
    };

    /*读取ram数据线程类*/
    private ramdataHandler handler = new ramdataHandler();
   BatteryRamDataThread batteryRamDataThread = new BatteryRamDataThread("[readBatteryRamThread]");
   public class ramdataHandler extends Handler{
        public ramdataHandler() {
        }

        public ramdataHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what)
            {
                case Definition.RAMDATATIMEOUTHANDLER://读取Ram数据单条命令超时
                    batteryRamDataThread.continute();//继续循环下一条指令
                    break;
            }
        }
    }

    public class BatteryRamDataThread extends Thread {
        private Object ramdata_lock = new Object();//读取Ram数据单条命令锁
        private boolean isContinute = true;
        public void exit() {
            isContinute = false;
        }//退出当前线程
        public void continute()
        {
            synchronized (ramdata_lock){
                ramdata_lock.notify();
            }

        }//继续读取Ram数据
        public BatteryRamDataThread(String threadName) {
            super(threadName);
        }

        @Override
        public void run() {
            super.run();
            while(isContinute)
            {
                synchronized(ramdata_lock){
                       /* 发送单条命令并且检测超时是否回复*/
                    try {
                        BatteryInformation.getInstance().clear();//开始一个轮询周期前，清空电池数据
                        for (Map.Entry<Byte, Integer> entry : Definition.getDefinition().orderMap.entrySet()) {

                            //发送出去
                            //等待超时或者回复
                            while(!StartReadRam(entry.getKey(), (byte) entry.getValue().intValue(), new OrderCallback() {
                                @Override
                                public void callback(String orderType, int orderAddr, byte[] data, boolean IsSuccess) {
                                    //处理数据，放入BatteryInfo单例中
                                    Log.i(TAG,"接收数据" + Factory.bytesToHexString(data));
                                    GreenwayProtocol.getFrameData(data, BatteryInformation.getInstance());
                                    continute();//继续轮询
                                }
                            })){
                                //如果没有开始成功，就等待100ms再次开始
                                sleep(100);
                            }
                            handler.sendEmptyMessageDelayed(Definition.RAMDATATIMEOUTHANDLER,Definition.RAMDATATIMEOUT);
                            ramdata_lock.wait();

                        }
                        //轮询周期3s//这不是精确的值
                        Log.d(TAG,"轮询周期结束");
                        Log.d(TAG,"ordermap count: "+ Definition.getDefinition().orderMap.size());
                        sleep(3000);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        }
    }
}
