package com.mtaas.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class EbayLogin {
	private static final String PACKAGE_NAME = "com.ebay.mobile";

	WebDriver driver = null;

	@BeforeMethod
	@Parameters({"device","browserName", "version", "platform", "packageName", "appActivity", "gridURL","appName","isEmulator","manufacturer","model"})
	public void setup(String device, String browserName, String version, String platform, String packageName, 
			String appActivity, String gridURL,String appName,String isEmulator,String manufacturer,String model) throws MalformedURLException {
		
		try {
			
			System.out.println("inside setup");
			
//			File app = new File("/home/eBayMobile.apk");
			File app = new File("/home/" + appName);
			
			System.out.println("Retrieve apk successfully");
			
			DesiredCapabilities capabilities = new DesiredCapabilities();
			capabilities.setCapability("device", device);
			capabilities.setCapability(CapabilityType.BROWSER_NAME, browserName);
			capabilities.setCapability(CapabilityType.VERSION, version);
			capabilities.setCapability(CapabilityType.PLATFORM, platform);
			// Here we mention the app's package name, to find the package name we
			// have to convert .apk file into java class files
			capabilities.setCapability("app-package", packageName);
			// Here we mention the activity name, which is invoked initially as
			// app's first page.
			capabilities.setCapability("app-activity", appActivity);
			// capabilities.setCapability("app-wait-activity","LoginActivity,NewAccountActivity");
			capabilities.setCapability("app", app.getAbsolutePath());
			
			System.out.println("Is Emulator : " + isEmulator) ;
			System.out.println("Manufacturer: " + manufacturer) ;
			System.out.println("model : " + model) ;
			//Custom capability
			capabilities.setCapability("isEmulator", Boolean.valueOf(isEmulator));
			capabilities.setCapability("manufacturer", manufacturer);
			capabilities.setCapability("model", model);
			
			System.out.println("capabilities set up completed");
			
			System.out.println("before establishing driver to " + gridURL + ", capabilities: " + capabilities.getVersion());
			
			driver = new RemoteWebDriver(new URL(gridURL), //4723
					capabilities);
			System.out.println("established driver to " + gridURL);
			
			
			driver.manage().timeouts().implicitlyWait(80, TimeUnit.SECONDS);
		}
		catch (Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		

	}

	@Test
	public void loginTest() throws Exception {
		
		goToMainScreen();
		
		WebElement we = driver.findElement(By.id(PACKAGE_NAME + ":id/button_sign_in"));
		if (we != null){
			System.out.println("Found using by.id Sign in");
			we.click();
		}
	}

	private void goToMainScreen() {
		WebElement we = driver.findElement(By.id(PACKAGE_NAME + ":id/accept_btn"));
		if (we != null){
			System.out.println("Found using by.id accept_btn");
			we.click();
		}
	}
	
	@Test
	public void register() throws Exception {
		goToMainScreen();
		String eName = "Register";
		WebElement we = driver.findElement(By.name(eName));
		if (we != null){
			System.out.println("Found using name: " + eName);
			we.click();
		}
	}

	@AfterMethod
	public void tearDown() {
		driver.quit();
	}

}

