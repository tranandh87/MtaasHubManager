package com.mtaas.bean;

import java.util.ArrayList;
import java.util.List;

public class GetDevicesResponse {
	private List<DeviceConfig> devices = new ArrayList<DeviceConfig>();
	
	public void setDevices(List<DeviceConfig> devices){
		this.devices = devices;
	}
	
	public List<DeviceConfig> getDevices(){
		return devices;
	}
	
	public void addDevice(DeviceConfig dev){
		devices.add(dev);
	}
}
