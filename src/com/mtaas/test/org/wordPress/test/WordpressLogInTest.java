package com.mtaas.test.org.wordPress.test;

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

public class WordpressLogInTest {
	private static final String PACKAGE_NAME = "org.wordpress.android";

	WebDriver driver = null;

	@BeforeMethod
	@Parameters({"device","browserName", "version", "platform", "packageName", "appActivity", "gridURL","appName","isEmulator","manufacturer","model"})
	public void setup(String device, String browserName, String version, String platform, String packageName, 
			String appActivity, String gridURL, String appName,String isEmulator,String manufacturer,String model) throws MalformedURLException {
		
		try {
			
			System.out.println("inside setup");
			
//			File app = new File("/home/wordpress_2.4.5.apk");
			File app = new File("/home/apk/" + appName);
			
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
			
			//Custom capability
			capabilities.setCapability("isEmulator", Boolean.valueOf(isEmulator));
			capabilities.setCapability("manufacturer", manufacturer);
			capabilities.setCapability("model", model);
			//Added for time-out
			capabilities.setCapability("newCommandTimeout", "20");
			
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
	public void addWordPressHostedBlog() throws Exception {
		
		WebElement we = driver.findElement(By.name("Add blog hosted at WordPress.com"));
		if (we != null){
			System.out.println("Found using by.name add blog hosted");
			we.click();
		}
		
		we = driver.findElement(By.name("Username"));
		if (we != null){
			System.out.println("Found by name Username");
			we.sendKeys("krish1631988@gmail.com");
		}
		
		we = driver.findElement(By.id(PACKAGE_NAME + ":id/password"));
		if (we != null){
			System.out.println("Found by id Password");
			we.sendKeys("almighty2222er");
		}
		
		we = driver.findElement(By.name("Sign In"));
		if (we != null){
			System.out.println("Found by name Sign In");
			we.click();
		}
	}
	
	@Test
	public void addSelfHostedBlog() throws Exception {
		WebElement we = driver.findElement(By.name("Add self-hosted WordPress blog"));
		if (we != null){
			System.out.println("Found using by.name add blog hosted");
			we.click();
		}
		
		we = driver.findElement(By.name("Blog URL"));
		if (we != null){
			System.out.println("found blog URL");
			we.sendKeys("http://HaidangTestBlog.com");
		}
		
		we = driver.findElement(By.name("Username"));
		if (we != null){
			System.out.println("Found Username");
			we.sendKeys("haidang_99");
		}
	}

	
	@AfterMethod
	public void tearDown() {
		driver.quit();
	}

}
