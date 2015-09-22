package com.mtaas.db;

import org.apache.log4j.Logger;

import com.mtaas.bean.Device;

public class DeviceInfoUpdater extends Thread {
	private static Logger log = Logger.getLogger(DeviceInfoUpdater.class.getName());
	
	private Device device = null;
	
	public DeviceInfoUpdater(Device device){
		this.device = device;
	}
	
	public void run(){
		int hubId = updateHubInfo();
		if (hubId > 0)
			updateDeviceInfo(device, hubId);
		else
			log.error("Hub table is not inserted Properly. Check for sql above");
	}

	private void updateDeviceInfo(Device device, int hubId) {
		if (!(DeviceInfoDAO.insertDeviceInfo(device, hubId) >= 0))
			log.error("Device table is not inserted correctly");
		// TODO Auto-generated method stub
		
	}

	/**
	 * Update the Hub table. I.e make new entry if not present else return the available hubId.
	 * @return - hubId if already present in Hub table with the hub info present in passed device object. 
	 * If not present, insert the new hub entry in hub table 
	 */
	private int updateHubInfo() {
		return HubInfoDAO.insertHubInfo(device);
		// TODO Auto-generated method stub
	}
}
