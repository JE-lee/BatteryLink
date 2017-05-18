package com.lee.batterylink.battery.protocol;

/**
 * Created by lee on 2017/5/15.
 */
//版本号
public class BatteryVersion {
    private int version_High = 0;
    private int version_Low  = 0;
    public BatteryVersion(float version) {
        String versionString = String.valueOf(version);
        String[] versionArray = versionString.split(".");
        for(int i =0;i<versionArray.length;i++)
        {
            version_High = stringToInt(versionArray[0],0);
            version_Low = stringToInt(versionArray[1],0);
        }
    }
    //如果出现非数字导致异常，就放回default
    private int stringToInt(String string,int defaultValue)
    {
        try{
            return Integer.parseInt(string);
        }catch(NumberFormatException e) {
            return defaultValue;
        }
    }

    public int getVersion_High() {
        return version_High;
    }

    public int getVersion_Low() {
        return version_Low;
    }

    public void setVersion_High(int version_High) {
        this.version_High = version_High;
    }

    public void setVersion_Low(int version_Low) {
        this.version_Low = version_Low;
    }

    public String getVersion()
    {
        return Integer.toString(version_High) + "." + Integer.toString(version_Low);
    }
}
