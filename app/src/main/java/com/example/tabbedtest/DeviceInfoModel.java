package com.example.tabbedtest;

public class DeviceInfoModel {
    public String deviceName, deviceHardwareAddress;

    public DeviceInfoModel(){}

    public DeviceInfoModel(String deviceName, String deviceHardwareAddress){
        this.deviceName = deviceName;
        this.deviceHardwareAddress = deviceHardwareAddress;
    }

    public String getDeviceName(){return deviceName;}

    public String getDeviceHardwareAddress(){return deviceHardwareAddress;}


}
