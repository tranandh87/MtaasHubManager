package com.mtaas.test.org.wordPress.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class WordpressReaderTest {
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
	public void testReader() throws Exception {
		
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
		
		Thread.sleep(10000);
		
		//Test the Reader button and activity on the dashboard.
		we = driver.findElement(By.name("Reader"));
		if (we != null){
			System.out.println("Found by name Reader");
			we.click();
		}
		
		Thread.sleep(10000);        
	}
	

	
	@AfterMethod
	public void tearDown() {
		driver.quit();
	}
	

	
	
	//Added for taking screenshots.
	/*WebDriver augmentedDriver = new Augmenter().augment(driver);
    File f  = ((TakesScreenshot)augmentedDriver).getScreenshotAs(OutputType.FILE);
    FileUtils.copyFile(f, new File("screenshot.jpg"));
    
    
        
		//Test to click the Dropdown element in the action bar.
		we = driver.findElement(By.id("@+id/menu_text_dropdown"));
		if (we != null){
			System.out.println("Found by name @+id/menu_text_dropdown");
			Select dropDown = new Select(we);
			if(dropDown != null){
				System.out.println("dropDown is not null");
				dropDown.selectByVisibleText("Freshly Pressed");
			}
			
			Thread.sleep(10000);
		}
    */

}

