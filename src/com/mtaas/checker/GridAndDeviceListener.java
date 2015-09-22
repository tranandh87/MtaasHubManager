package com.mtaas.checker;

import javax.servlet.ServletContextEvent;

import org.apache.log4j.Logger;

import com.mtaas.util.MtaasConstants;

public class GridAndDeviceListener implements javax.servlet.ServletContextListener {

	private static Logger log = Logger.getLogger(GridAndDeviceListener.class.getName());
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		log.info("Contex is destroyed");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		log.info("Contex is Initialized");
		if (MtaasConstants.GRID_URL_CHECKER_ENABLED){
			log.info("Chekcer started for grid url in hub table");
			GridUrlChecker.main(null);
		}
		else
			log.info("Grid URL checker is disabled so not performing any background operation to check grid url is up or not");
		
		if (MtaasConstants.APPIUM_IP_CHECKER_ENABLED){
			log.info("Chekcer started for appium Ip in device table");
			AppiumNodeUrlChecker.main(null);
		}
		else
			log.info("Appium Ip checker is disabled so not performing any background operation to check appium ip is up or not");
		
	}
}
