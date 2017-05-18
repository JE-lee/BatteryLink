package com.lee.batterylink.battery.protocol;

import java.util.ArrayList;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;



/**
 * Created by lee on 2017/5/15.
 */

public class BatteryInformation {

    private static final BatteryInformation info = new BatteryInformation();
    private byte cellTemp_1;//电芯温度
    private byte cellTemp_2;
    private byte cellTemp_3;
    private byte pcbTemp;//PCB温度
    private byte DSGMosTemp;//放电MOS温度
    private byte CHGMosTemp;//充电MOS温度
    private byte prestartTemp;//预启动温度

    private int voltage;//电池包电压
    private int current;//电池包电流
    private float SOC;//电池容量百分比
    private float SOH;//电池健康状态百分比
    private int fullChargeCap;//满充容量
    private int usbCurrent;//usb 电流信息
    private ArrayList<String> batteryError = new ArrayList<>();//电池运行时错误信息
    private ArrayList<String> batteryWarning = new ArrayList<>();//电池运行时警告信息
    private int cycleCount ;//电池循环次数

    private int designCap;//设计容量
    private int designVoltage;//设计电压

    private BatteryVersion hardwareVersion = new BatteryVersion(0);//电池硬件版本号
    private BatteryVersion softwareVersion = new BatteryVersion(0);//电池软件版本号
    private String batteryIndex;//电池IDX

    private Calendar batteryManufactureDate = Calendar.getInstance();//电池生产日期
    private Calendar batteryRTC =Calendar.getInstance();//电池内部实时时钟

    private int fullChargeMinute;//电池充满时间 单位minute

    private String batteryManufacturer;//电池制造商
    private String batteryName;//电池名称
    private String cellModel;//电芯型号
    private String batteryBarcode;//电池条码

    private Map<Integer, Object> singleCellVoltage = new HashMap<>();  //单节电压数据,key从1到32

    //最值数据
    private int batteryMaxCellVoltage;//最大单节电压
    private int batteryMinCellVolatge;//最小单节电压
    private int batteryMaxDSGCurrent;//最大放电电流
    private int batteryMaxCHGCurrent;//最大充电电流
    private int packMaxTemp;//电池包最大温度
    private int packMinTemp;//电池包最小温度

    //单例模式
    private  BatteryInformation() {
    }
    public static  BatteryInformation  getInstance()
    {return info;}

    //清空
    public static void clear(){
        info.cellTemp_1 = 0;
        info.cellTemp_2 = 0;
        info.cellTemp_3 = 0;
        info.pcbTemp = 0;
        info.DSGMosTemp = 0;
        info.CHGMosTemp = 0;
        info.prestartTemp = 0;

        info.voltage = 0;
        info.current = 0;
        info.SOC = 0;
        info.SOH = 0;
        info.fullChargeCap = 0;
        info.usbCurrent = 0;
        info.batteryError.clear();
        info.batteryWarning.clear();

        info.cycleCount = 0;
        info.designCap = 0;
        info.designVoltage = 0;

        info.hardwareVersion.setVersion_High(0);
        info.hardwareVersion.setVersion_Low(0);
        info.softwareVersion.setVersion_High(0);
        info.softwareVersion.setVersion_Low(0);
        info.batteryIndex = "";
        info.batteryIndex = "";
        info.batteryManufactureDate.set(0,0,0,0,0,0);
        info.batteryRTC.set(0,0,0,0,0,0);
        info.fullChargeMinute = 0;
        info.batteryManufacturer = "";
        info.batteryName = "";
        info.cellModel = "";
        info.batteryBarcode = "";
        info.singleCellVoltage.clear();

        info.batteryMaxCellVoltage = 0;
        info.batteryMinCellVolatge = 0;
        info.batteryMaxDSGCurrent = 0;
        info.batteryMaxCHGCurrent = 0;
        info.packMaxTemp = 0;
        info.packMinTemp = 0;
    }

    //getter
    public byte getCellTemp_1() {
        return cellTemp_1;
    }

    public byte getCellTemp_2() {
        return cellTemp_2;
    }

    public byte getCellTemp_3() {
        return cellTemp_3;
    }

    public byte getPcbTemp() {
        return pcbTemp;
    }

    public byte getDSGMosTemp() {
        return DSGMosTemp;
    }

    public byte getCHGMosTemp() {
        return CHGMosTemp;
    }

    public byte getPrestartTemp() {
        return prestartTemp;
    }

    public int getVoltage() {
        return voltage;
    }

    public int getCurrent() {
        return current;
    }

    public float getSOC() {
        return SOC;
    }

    public float getSOH() {
        return SOH;
    }

    public int getFullChargeCap() {
        return fullChargeCap;
    }

    public int getUsbCurrent() {
        return usbCurrent;
    }

    public ArrayList<String> getBatteryError() {
        return batteryError;
    }

    public ArrayList<String> getBatteryWarning() {
        return batteryWarning;
    }

    public int getCycleCount() {
        return cycleCount;
    }

    public int getDesignCap() {
        return designCap;
    }

    public int getDesignVoltage() {
        return designVoltage;
    }

    public BatteryVersion getHardwareVersion() {
        return hardwareVersion;
    }

    public BatteryVersion getSoftwareVersion() {
        return softwareVersion;
    }

    public String getBatteryIndex() {
        return batteryIndex;
    }

    public Calendar getBatteryManufactureDate() {
        return batteryManufactureDate;
    }

    public Calendar getBatteryRTC() {
        return batteryRTC;
    }

    public int getFullChargeMinute() {
        return fullChargeMinute;
    }

    public String getBatteryManufacturer() {
        return batteryManufacturer;
    }

    public String getBatteryName() {
        return batteryName;
    }

    public String getCellModel() {
        return cellModel;
    }

    public String getBatteryBarcode() {
        return batteryBarcode;
    }

    public Map<Integer, Object> getSingleCellVoltage() {
        return singleCellVoltage;
    }

    public int getBatteryMaxCellVoltage() {
        return batteryMaxCellVoltage;
    }

    public int getBatteryMinCellVolatge() {
        return batteryMinCellVolatge;
    }

    public int getBatteryMaxDSGCurrent() {
        return batteryMaxDSGCurrent;
    }

    public int getBatteryMaxCHGCurrent() {
        return batteryMaxCHGCurrent;
    }

    public int getPackMaxTemp() {
        return packMaxTemp;
    }

    public int getPackMinTemp() {
        return packMinTemp;
    }


    //setter

    public void setCellTemp_1(byte cellTemp_1) {
        this.cellTemp_1 = cellTemp_1;
    }

    public void setCellTemp_2(byte cellTemp_2) {
        this.cellTemp_2 = cellTemp_2;
    }

    public void setCellTemp_3(byte cellTemp_3) {
        this.cellTemp_3 = cellTemp_3;
    }

    public void setPcbTemp(byte pcbTemp) {
        this.pcbTemp = pcbTemp;
    }

    public void setDSGMosTemp(byte DSGMosTemp) {
        this.DSGMosTemp = DSGMosTemp;
    }

    public void setCHGMosTemp(byte CHGMosTemp) {
        this.CHGMosTemp = CHGMosTemp;
    }

    public void setPrestartTemp(byte prestartTemp) {
        this.prestartTemp = prestartTemp;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public void setSOC(float SOC) {
        this.SOC = SOC;
    }

    public void setSOH(float SOH) {
        this.SOH = SOH;
    }

    public void setFullChargeCap(int fullChargeCap) {
        this.fullChargeCap = fullChargeCap;
    }

    public void setUsbCurrent(int usbCurrent) {
        this.usbCurrent = usbCurrent;
    }

    public void setBatteryError(ArrayList<String> batteryError) {
        this.batteryError = batteryError;
    }

    public void setBatteryWarning(ArrayList<String> batteryWarning) {
        this.batteryWarning = batteryWarning;
    }

    public void setCycleCount(int cycleCount) {
        this.cycleCount = cycleCount;
    }

    public void setDesignCap(int designCap) {
        this.designCap = designCap;
    }

    public void setDesignVoltage(int designVoltage) {
        this.designVoltage = designVoltage;
    }

    public void setHardwareVersion(BatteryVersion hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public void setSoftwareVersion(BatteryVersion softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public void setBatteryIndex(String batteryIndex) {
        this.batteryIndex = batteryIndex;
    }

    public void setBatteryManufactureDate(Calendar batteryManufactureDate) {
        this.batteryManufactureDate = batteryManufactureDate;
    }

    public void setBatteryRTC(Calendar batteryRTC) {
        this.batteryRTC = batteryRTC;
    }

    public void setFullChargeMinute(int fullChargeMinute) {
        this.fullChargeMinute = fullChargeMinute;
    }

    public void setBatteryManufacturer(String batteryManufacturer) {
        this.batteryManufacturer = batteryManufacturer;
    }

    public void setBatteryName(String batteryName) {
        this.batteryName = batteryName;
    }

    public void setCellModel(String cellModel) {
        this.cellModel = cellModel;
    }

    public void setBatteryBarcode(String batteryBarcode) {
        this.batteryBarcode = batteryBarcode;
    }

    public void setSingleCellVoltage(Map<Integer, Object> singleCellVoltage) {
        this.singleCellVoltage = singleCellVoltage;
    }

    public void setBatteryMaxCellVoltage(int batteryMaxCellVoltage) {
        this.batteryMaxCellVoltage = batteryMaxCellVoltage;
    }

    public void setBatteryMinCellVolatge(int batteryMinCellVolatge) {
        this.batteryMinCellVolatge = batteryMinCellVolatge;
    }

    public void setBatteryMaxDSGCurrent(int batteryMaxDSGCurrent) {
        this.batteryMaxDSGCurrent = batteryMaxDSGCurrent;
    }

    public void setBatteryMaxCHGCurrent(int batteryMaxCHGCurrent) {
        this.batteryMaxCHGCurrent = batteryMaxCHGCurrent;
    }

    public void setPackMaxTemp(int packMaxTemp) {
        this.packMaxTemp = packMaxTemp;
    }

    public void setPackMinTemp(int packMinTemp) {
        this.packMinTemp = packMinTemp;
    }
}
