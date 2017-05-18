package com.lee.batterylink.battery.protocol;

import com.lee.batterylink.battery.Definition.Definition;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by lee on 2017/5/15.
 */

public class GreenwayProtocol {

    //返回值：是否成功得到帧数据
    public static boolean getFrameData(byte[] data,BatteryInformation info)
    {
        if(data == null)
            return false;
        //先判断是否为有效帧
        Map<Integer,Object> single = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        if((Definition.GREENWAYACKFRAMEHEAD != data[0]) || isAvalidFrame(data))
            return false;
        //将数据映射到info中
        switch(data[2])//读写方向
        {
            case 0://写
                break;
            case 1://读
                switch(data[3])//命令地址
                {
                    case Definition.TEMP://0x08
                        info.setCellTemp_1(data[5]);
                        info.setCellTemp_2(data[6]);
                        info.setCellTemp_3(data[7]);
                        info.setPcbTemp(data[8]);
                        info.setDSGMosTemp(data[9]);
                        info.setCHGMosTemp(data[10]);
                        info.setPrestartTemp(data[11]);
                        break;
                    case Definition.VOLTAGE://0x09
                        info.setVoltage((data[5] & 0xff) | ((data[6] << 8) & 0xffff) | ((data[7] << 16) & 0xffffff) | ((data[8] << 24) & 0xffffffff));
                        break;
                    case Definition.CURRENT://0x0a
                        info.setCurrent((data[5] & 0xff) | ((data[6] << 8) & 0xffff) | ((data[7] << 16) & 0xffffff) | ((data[8] << 24) & 0xffffffff));
                        break;
                    case Definition.SOC://0x0d
                        info.setSOC(((int)data[5]&0xff)/100);
                        break;
                    case Definition.SOH://0x0e
                        info.setSOH(((int)data[5]&0xff)/100);
                        break;
                    case Definition.FULLCHARGECAP:
                        info.setFullChargeCap((data[5] & 0xff) | ((data[6] << 8) & 0xffff) | ((data[7] << 16) & 0xffffff) | ((data[8] << 24) & 0xffffffff));
                        break;
                    case Definition.USBCURRENT:
                        info.setUsbCurrent((data[5] & 0xff) | ((data[6] << 8) & 0xffff) );
                        break;
                    case Definition.BATTERYSTATUS:
                        info.setBatteryError(getBatteryErrorFromFrame(data));
                        info.setBatteryWarning(getBatteryErrorFromFrame(data));
                        break;
                    case Definition.CYCLECOUNT:
                        info.setCycleCount((data[5] & 0xff) | ((data[6] << 8) & 0xffff) | ((data[7] << 16) & 0xffffff) | ((data[8] << 24) & 0xffffffff));
                        break;
                    case Definition.DESIGNCAP:
                        info.setDesignCap((data[5] & 0xff) | ((data[6] << 8) & 0xffff) | ((data[7] << 16) & 0xffffff) | ((data[8] << 24) & 0xffffffff));
                        break;
                    case Definition.DESIGNVOLTAGE:
                        info.setDesignVoltage((data[5] & 0xff) | ((data[6] << 8) & 0xffff) | ((data[7] << 16) & 0xffffff) | ((data[8] << 24) & 0xffffffff));
                        break;
                    case Definition.VERSION:
                        BatteryVersion version = new BatteryVersion(0);
                        version.setVersion_Low((int)(data[5]&0xff));
                        version.setVersion_High((int)(data[6] & 0xff));
                        info.setSoftwareVersion(version);

                        version.setVersion_Low((int)(data[7] & 0xff));
                        version.setVersion_High((int)(data[8] & 0xff));
                        info.setHardwareVersion(version);

                        info.setBatteryIndex(String.valueOf((int)(data[5] & 0xff)) + String.valueOf((int)(data[6]&0xff)) +String.valueOf((int)(data[7]&0xff))+String.valueOf((int)(data[8]&0xff)));
                        break;
                    case Definition.BATTERYMANUFACTUREDATE:

                        calendar.set((int)(data[5] & 0xff),(int)(data[6] & 0xff),(int)(data[7] & 0xff));
                        info.setBatteryManufactureDate(calendar);
                        break;
                    case Definition.BATTERYRTC:
                        calendar.set((int)(data[5] & 0xff),(int)(data[6] & 0xff),(int)(data[7] & 0xff),(int)(data[8] & 0xff),(int)(data[9] & 0xff),(int)(data[10] & 0xff));
                        info.setBatteryRTC(calendar);
                        break;
                    case Definition.FULLCHARGEMINUTE:
                        break;
                    case Definition.BATTERYMANUFACTER:
                        byte[] factuer = Arrays.copyOfRange(data,5,5+data[4]);
                        try{
                            String factuerS = new String(factuer,"ASCII");
                            info.setBatteryManufacturer(factuerS);
                        }catch (UnsupportedEncodingException e){

                        }
                        break;
                    case Definition.BATTERYNAME:
                        byte[] name = Arrays.copyOfRange(data,5,5+data[4]);
                        try{
                            String batteryname = new String(name,"ASCII");
                            info.setBatteryName(batteryname);
                        }catch (UnsupportedEncodingException e){

                        }
                        break;
                    case Definition.CELLMODEL:
                        byte[] model = Arrays.copyOfRange(data,5,5+data[4]);
                        try{
                            String modleS = new String(model,"ASCII");
                            info.setCellModel(modleS);
                        }catch (UnsupportedEncodingException e){

                        }
                        break;
                    case Definition.BATTERYBARCODE:
                        byte[] barcode = Arrays.copyOfRange(data,5,5+data[4]);
                        try{
                            String barcodeS = new String(barcode,"ASCII");
                            info.setBatteryBarcode(barcodeS);
                        }catch (UnsupportedEncodingException e){

                        }
                        break;
                    case Definition.LOWSINGLECELLVOLTAGE:

                        for(int i = 0;i<16;i++)
                        {
                            int value = (data[5+i*2] & 0xff) | ((data[6+i*2] << 8) & 0xffff);
                            single.put(i,value);
                        }
                        break;

                    case Definition.HIGHSINGLECELLVOLTAGE:
                        for(int i = 0;i<16;i++)
                        {
                            int value = (data[5+i*2] & 0xff) | ((data[6+i*2] << 8) & 0xffff);
                            single.put(i + 16,value);
                        }
                        info.setSingleCellVoltage(single);
                        break;
                    case Definition.LIFTTIME:
                        info.setBatteryMaxDSGCurrent(data[0] | (data[1] << 8) | (data[2] << 16) | (data[3] << 24));
                        info.setBatteryMaxCHGCurrent(data[4] | (data[5] << 8) | (data[6] << 16) | (data[7] << 24));
                        info.setBatteryMaxCellVoltage(data[8] | (data[9] <<8));
                        info.setBatteryMinCellVolatge(data[10] | (data[11] <<8));
                        info.setPackMaxTemp(data[12]);
                        info.setPackMinTemp(data[13]);
                        break;
                    case Definition.IICREAD:
                        break;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    public static boolean isAvalidFrame(byte[] data)
    {
        byte check = (byte)(greenwaySumCheck(data) - data[data.length - 1]);
        return (check == data[data.length -1]?true:false);

    }

    public static byte greenwaySumCheck(byte[] data)
    {
        int sum = 0;
        for(int i =0;i<data.length;i++)
        {
            sum += data[i];
        }
        return (byte)sum;
    }
    //读取Ram数据帧
/*
   public static class WrongDirectionException extends Exception{
       public WrongDirectionException(String msg) {
           super(msg);
       }
   }
*/
    public static byte[] makeRamFrame (byte orderAddr,int length,byte direction)
    {
        byte[] data = new byte[6];
        data[0] = Definition.GREENWAYREQUESTFRAMEHEAD;
        data[1] = Definition.BATTERYSLAVEADDR;
        if(direction == Definition.READ || direction == Definition.WRITE){
            data[2] =direction;
        }
        data[3] = orderAddr;
        data[4] = (byte)length;
        data[5] = (byte)(greenwaySumCheck(data) - data[5]);

        return data;
    }

    //在一组byte数组中，找到第一个FrameHead的位置，并且将这个位置以及这个位置只有的数据移动到数组小标0开始的地方
    //返回值
    //int 舍弃的byte字节数
    public static int SolveByteArray(byte FrameHead,byte data[]){
        int index = 0;
        byte[] storeData = data.clone();
        for(int i =0;i<data.length;i++){
            if(data[i] == FrameHead){
                for(int j =0;j<data.length -i;j++){
                    data[j] = storeData[i+j];
                }
                index = i+1;
                break;
            }
        }
        return index;
    }


   //从一帧有效数据中取得电池错误信息
    private static ArrayList<String> getBatteryErrorFromFrame(byte[] Frame)
    {
        ArrayList<String> error = new ArrayList<>();
        //0
        if(1 == (Frame[4] & 0x01)){error.add(Definition.BatteryStatus.ProtectionChipError);}
        if(1 == (Frame[4] & 0x02)){error.add(Definition.BatteryStatus.CellDropError);}
        if(1 == (Frame[4] & 0x04)){error.add(Definition.BatteryStatus.Imbanlance);}
        if(1 == (Frame[4] & 0x08)){error.add(Definition.BatteryStatus.EstimateError);}
        if(1 == (Frame[4] & 0x10)){error.add(Definition.BatteryStatus.RecordError);}
        if(1 == (Frame[4] & 0x20)){error.add(Definition.BatteryStatus.RTCError);}
        if(1 == (Frame[4] & 0x40)){error.add(Definition.BatteryStatus.DischargingMosfetError);}
        if(1 == (Frame[4] & 0x80)){error.add(Definition.BatteryStatus.ChargingMosfetError);}

        //1
        if(1 == (Frame[5] & 0x01)){error.add(Definition.BatteryStatus.OverCharge);}
        if(1 == (Frame[5] & 0x02)){error.add(Definition.BatteryStatus.PrimaryOverDischarge);}
        if(1 == (Frame[5] & 0x04)){error.add(Definition.BatteryStatus.SecondaryOverDischarge);}
        if(1 == (Frame[5] & 0x08)){error.add(Definition.BatteryStatus.PrimaryOverCurrent);}
        if(1 == (Frame[5] & 0x10)){error.add(Definition.BatteryStatus.SecondaryOverCurrent);}
        if(1 == (Frame[5] & 0x20)){error.add(Definition.BatteryStatus.OverChargeCurrent);}
        if(1 == (Frame[5] & 0x40)){error.add(Definition.BatteryStatus.PreStartFail);}
        if(1 == (Frame[5] & 0x80)){error.add(Definition.BatteryStatus.PreChargeOvertime);}

        //2
        if(1 == (Frame[6] & 0x01)){error.add(Definition.BatteryStatus.MOSTemperatureSensorError);}
        if(1 == (Frame[6] & 0x02)){error.add(Definition.BatteryStatus.CellTemperatureSensorError);}
        if(1 == (Frame[6] & 0x04)){error.add(Definition.BatteryStatus.OverDischargeTemperature);}
        if(1 == (Frame[6] & 0x08)){error.add(Definition.BatteryStatus.OverChargeTemperature);}
        if(1 == (Frame[6] & 0x10)){error.add(Definition.BatteryStatus.UnderDischargeTemperature);}
        if(1 == (Frame[6] & 0x20)){error.add(Definition.BatteryStatus.UnderChargeTemperature);}
        if(1 == (Frame[6] & 0x40)){error.add(Definition.BatteryStatus.OverTemperatureofDischargeMosfet);}
        if(1 == (Frame[6] & 0x80)){error.add(Definition.BatteryStatus.OvertemperatureofChargeMosfet);}

        //3
        if(1 == (Frame[7] & 0x01)){error.add(Definition.BatteryStatus.OvertemperatureofPreStartcircuit);}
        if(1 == (Frame[7] & 0x02)){error.add(Definition.BatteryStatus.ROMError);}
        if(1 == (Frame[7] & 0x04)){error.add(Definition.BatteryStatus.DischargeFuseBurned);}
        if(1 == (Frame[7] & 0x08)){error.add(Definition.BatteryStatus.ChargingFuseBurned);}
        if(1 == (Frame[7] & 0x10)){error.add(Definition.BatteryStatus.ThirdOverCurrent);}
        if(1 == (Frame[7] & 0x20)){error.add(Definition.BatteryStatus.ForthOverCurrent);}

        return error;
    }
    //从一帧有效数据中取得电池警告信息
    private static ArrayList<String> getBatteryWarningFromFrame(byte[] Frame)
    {
        ArrayList<String> warning = new ArrayList<>();
        //0
        if(1 == (Frame[8] & 0x01)){warning.add(Definition.BatteryStatus.ProtectionChipWarning);}
        if(1 == (Frame[8] & 0x02)){warning.add(Definition.BatteryStatus.CellDropWarning);}
        if(1 == (Frame[8] & 0x04)){warning.add(Definition.BatteryStatus.ImbanlanceWarning);}
        if(1 == (Frame[8] & 0x08)){warning.add(Definition.BatteryStatus.EstimateWarning);}
        if(1 == (Frame[8] & 0x10)){warning.add(Definition.BatteryStatus.RecordWarning);}
        if(1 == (Frame[8] & 0x20)){warning.add(Definition.BatteryStatus.RTCWarning);}
        //1
        if(1 == (Frame[9] & 0x01)){warning.add(Definition.BatteryStatus.OverChargeWarning);}
        if(1 == (Frame[9] & 0x02)){warning.add(Definition.BatteryStatus.PrimaryOverDischargeWarning);}

        if(1 == (Frame[9] & 0x08)){warning.add(Definition.BatteryStatus.PrimaryOverCurrentWarning);}

        if(1 == (Frame[9] & 0x20)){warning.add(Definition.BatteryStatus.OverChargeCurrentWarning);}

        if(1 == (Frame[9] & 0x80)){warning.add(Definition.BatteryStatus.PreChargeOvertimeWarning);}
        //2
        if(1 == (Frame[10] & 0x01)){warning.add(Definition.BatteryStatus.MOSTemperatureSensorWarning);}
        if(1 == (Frame[10] & 0x02)){warning.add(Definition.BatteryStatus.CellTemperatureSensorWarning);}
        if(1 == (Frame[10] & 0x04)){warning.add(Definition.BatteryStatus.OverDischargeTemperatureWarning);}
        if(1 == (Frame[10] & 0x08)){warning.add(Definition.BatteryStatus.OverChargeTemperatureWarning);}
        if(1 == (Frame[10] & 0x10)){warning.add(Definition.BatteryStatus.UnderDischargeTemperatureWarning);}
        if(1 == (Frame[10] & 0x20)){warning.add(Definition.BatteryStatus.UnderChargeTemperatureWarning);}
        if(1 == (Frame[10] & 0x40)){warning.add(Definition.BatteryStatus.OverTemperatureofDischargeMosfetWarning);}
        if(1 == (Frame[10] & 0x80)){warning.add(Definition.BatteryStatus.OvertemperatureofChargeMosfetWarning);}

        //3
        if(1 == (Frame[11] & 0x01)){warning.add(Definition.BatteryStatus.OvertemperatureofPreStartcircuitWarning);}
        if(1 == (Frame[11] & 0x02)){warning.add(Definition.BatteryStatus.ROMErrorWarning);}

        return warning;
    }




}
