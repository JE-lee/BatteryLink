package com.lee.batterylink.battery.Definition;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lee on 2017/5/15.
 */
/*
定义了本工程所用的常量
* */

public class Definition {


    public static final byte GREENWAYACKFRAMEHEAD = 0x47;//回复帧头
    public static final byte GREENWAYREQUESTFRAMEHEAD = 0X46;//询问帧头
    public static final byte BATTERYSLAVEADDR = 0x16;//从机地址

    public static final byte READ = 0x01;
    public static final byte WRITE = 0x00;



    public static final byte TEMP = 0X08;
    public static final int TEMP_length = 0x04;
    public static final byte VOLTAGE = 0x09;
    public static final int VOLTAGE_length = 0x04;

    public static final byte CURRENT = 0x0a;
    public static final int CURRENT_length = 0x04;
    public static final byte SOC = 0x0d;
    public static final int SOC_length = 0X04;
    public static final byte SOH = 0x0e;
    public static final int SOH_length = 0X04;

    public static final byte FULLCHARGECAP = 0x10;
    public static final int FULLCHARGECAP_length = 0X04;
    public static final byte USBCURRENT = 0x14;
    public static final int USBCURRENT_length  = 0X04;
    public static final byte BATTERYSTATUS = 0X16;
    public static final int BATTERYSTATUS_length = 16;

    public class BatteryStatus{
        //error
        public static final String  ProtectionChipError = "Protection Chip Error";
        public static final String  CellDropError = "Cell Drop Error";
        public static final String  Imbanlance = "Imbanlance";
        public static final String  EstimateError = "Estimate Error";
        public static final String  RecordError = "Record Error";
        public static final String  RTCError = "RTC Error";
        public static final String  DischargingMosfetError = "Discharging Mosfet Error";
        public static final String  ChargingMosfetError = "Charging Mosfet Error";

        public static final String  OverCharge = "Over Charge";
        public static final String  PrimaryOverDischarge = "Primary Over Discharge";
        public static final String  SecondaryOverDischarge = "Secondary Over Discharge";
        public static final String  PrimaryOverCurrent = "Primary Over Current";
        public static final String  SecondaryOverCurrent  = "Secondary Over Current ";
        public static final String  OverChargeCurrent = "Over Charge Current";
        public static final String  PreStartFail = "Pre-Start Fail";
        public static final String  PreChargeOvertime = "Pre-Charge Over time";

        public static final String  MOSTemperatureSensorError = "MOS Temperature Sensor Error";
        public static final String  CellTemperatureSensorError = "Cell Temperature Sensor Error";
        public static final String  OverDischargeTemperature = "Over Discharge Temperature";
        public static final String  OverChargeTemperature = "Over Charge Temperature";
        public static final String  UnderDischargeTemperature = "Under Discharge Temperature";
        public static final String  UnderChargeTemperature = "Under Charge Temperature";
        public static final String  OverTemperatureofDischargeMosfet = "Over Temperature of Discharge Mosfet";
        public static final String  OvertemperatureofChargeMosfet = "Over temperature of Charge Mosfet";

        public static final String  OvertemperatureofPreStartcircuit = "Over temperature of Pre-Start circuit";
        public static final String  ROMError = "ROM Error";
        public static final String  DischargeFuseBurned = "Discharge Fuse Burned";
        public static final String  ChargingFuseBurned = "Charging Fuse Burned";
        public static final String  ThirdOverCurrent = "Third Over Current";
        public static final String  ForthOverCurrent = "Forth Over Current";
        //warning
        public static final String  ProtectionChipWarning = "Protection Chip Warning";
        public static final String  CellDropWarning = "Cell Drop Warning";
        public static final String  ImbanlanceWarning = "Imbanlance Warning";
        public static final String  EstimateWarning = "Estimate Warning";
        public static final String  RecordWarning = "Record Warning";
        public static final String  RTCWarning = "RTC Warning";

        public static final String  OverChargeWarning = "Over Charge Warning";
        public static final String  PrimaryOverDischargeWarning = "Primary Over Discharge Warning";
        public static final String  PrimaryOverCurrentWarning = "Primary Over Current Warning";
        public static final String  OverChargeCurrentWarning = "Over Charge Current Warning";
        public static final String  PreChargeOvertimeWarning = "Pre-Charge Over time Warning";

        public static final String  MOSTemperatureSensorWarning = "MOS Temperature Sensor Warning";
        public static final String  CellTemperatureSensorWarning = "Cell Temperature Sensor Warning";
        public static final String  OverDischargeTemperatureWarning = "Over Discharge Temperature Warning";
        public static final String  OverChargeTemperatureWarning = "Over Charge Temperature Warning";
        public static final String  UnderDischargeTemperatureWarning = "Under Discharge Temperature Warning";
        public static final String  UnderChargeTemperatureWarning = "Under Charge Temperature Warning";
        public static final String  OverTemperatureofDischargeMosfetWarning = "Over Temperature of Discharge Mosfet Warning";
        public static final String  OvertemperatureofChargeMosfetWarning = "Over temperature of Charge Mosfet Warning";

        public static final String  OvertemperatureofPreStartcircuitWarning = "Over temperature of Pre-Start circuit Warning";
        public static final String  ROMErrorWarning = "ROM Error Warning";












    }

    public static final byte CYCLECOUNT = 0x17;
    public static final int CYCLECOUNT_length = 4;
    public static final byte DESIGNCAP = 0x18;
    public static final int DESIGNCAP_length  = 4;
    public static final byte DESIGNVOLTAGE = 0x19;
    public static final int DESIGNVOLTAGE_length = 4;

    public static final byte VERSION = 0x1a;
    public static final int VERSION_length  = 8;

    public static final byte BATTERYMANUFACTUREDATE = 0x1b;
    public static final int BATTERYMANUFACTUREDATE_length  = 6;
    public static final byte BATTERYRTC = 0x1d;
    public static final int BATTERYRTC_length  = 6;
    public static final byte FULLCHARGEMINUTE = 0x1e;
    public static final int FULLCHARGEMINUTE_length  = 4;

    public static final byte BATTERYMANUFACTER  = 0x20;
    public static final int BATTERYMANUFACTER_length  = 16;
    public static final byte BATTERYNAME  = 0x21;
    public static final int BATTERYNAME_length  = 32;
    public static final byte CELLMODEL  = 0x22;
    public static final int CELLMODEL_length  = 16;
    public static final byte BATTERYBARCODE  = 0x23;
    public static final int BATTERYBARCODE_length  = 32;

    public static final byte LOWSINGLECELLVOLTAGE = 0x24;
    public static final int LOWSINGLECELLVOLTAGE_length  = 32;
    public static final byte HIGHSINGLECELLVOLTAGE = 0x25;
    public static final int HIGHSINGLECELLVOLTAGE_length  = 32;

    public static final byte LIFTTIME = 0x26;
    public static final int LIFTTIME_length  = 14;

    private static final Definition definition = new Definition();
    public static final Map<Byte,Integer> orderMap = new HashMap<>();
    private static boolean isFirstInit = true;
    public static Definition getDefinition() {
        if(isFirstInit){
            definition.orderMap.put(TEMP,TEMP_length);
            definition.orderMap.put(VOLTAGE,VOLTAGE_length);
            definition.orderMap.put(CURRENT,CURRENT_length);
            definition.orderMap.put(SOC,SOC_length);
            definition.orderMap.put(SOH,SOH_length);
            definition.orderMap.put(FULLCHARGECAP,FULLCHARGECAP_length);
            definition. orderMap.put(USBCURRENT,USBCURRENT_length);
            definition.orderMap.put(BATTERYSTATUS,BATTERYSTATUS_length);
            definition.orderMap.put(CYCLECOUNT,CYCLECOUNT_length);
            definition.orderMap.put(DESIGNCAP,DESIGNCAP_length);
            definition.orderMap.put(DESIGNVOLTAGE,DESIGNVOLTAGE_length);
            definition.orderMap.put(VERSION,VERSION_length);
            definition.orderMap.put(BATTERYMANUFACTUREDATE,BATTERYMANUFACTUREDATE_length);
            definition.orderMap.put(BATTERYRTC,BATTERYRTC_length);
            definition. orderMap.put(FULLCHARGEMINUTE,FULLCHARGEMINUTE_length);
            definition. orderMap.put(BATTERYMANUFACTER,BATTERYMANUFACTER_length);
            definition.orderMap.put(BATTERYNAME,BATTERYNAME_length);
            definition. orderMap.put(CELLMODEL,CELLMODEL_length);
            definition.orderMap.put(BATTERYBARCODE,BATTERYBARCODE_length);
            definition.orderMap.put(LOWSINGLECELLVOLTAGE,LOWSINGLECELLVOLTAGE_length);
            definition.orderMap.put(HIGHSINGLECELLVOLTAGE,HIGHSINGLECELLVOLTAGE_length);
            definition.orderMap.put(LIFTTIME,LIFTTIME_length);
        }
        isFirstInit = false;
        return definition;

    }

    public static final byte IICWRITE = 0x77;
    public static final byte IICREAD = 0x78;
    //handler 的message类型
    public static final int RAMDATATIMEOUTHANDLER = 1;//读取Ram数据超时信息类型
    public static final int RAMDATATIMEOUT = 200;//读取Ram数据超时时间200ms











}
