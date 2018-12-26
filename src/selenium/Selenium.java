package selenium;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Selenium {
	private static final String DRIVERS_DIRECTORY = "C:\\Git\\Selenium\\drivers\\";
	
	private static WebDriver driver;
	private static int timeoutTime = 3;
	
	//---------------------------------------------------------------------------------------------
	public static WebDriver getDriver(){
		return driver;
	}
	//---------------------------------------------------------------------------------------------
	//SETUP
	//---------------------------------------------------------------------------------------------
	public static void setup(Browser browser){
		setBrowser(browser);
		driver.manage().timeouts().implicitlyWait(timeoutTime, TimeUnit.SECONDS);
	}
	//---------------------------------------------------------------------------------------------
	private static void setBrowser(Browser browser){
		System.setProperty(browser.getDriverKey(), browser.getDriverPath());
		switch(browser){
			case Firefox: driver = new FirefoxDriver(); break;
			case Chrome: driver = new ChromeDriver(); break;
			case IE: driver = new InternetExplorerDriver(); break;
		}
	}
	//---------------------------------------------------------------------------------------------
	public static void setTimeoutTime(int seconds){
		timeoutTime = seconds;
		driver.manage().timeouts().implicitlyWait(timeoutTime, TimeUnit.SECONDS);
	}
	//---------------------------------------------------------------------------------------------
	//ACTIONS
	//---------------------------------------------------------------------------------------------
	public static void navigate(String url){
		try{
			driver.get(url);
		}
		catch(Exception e){
			System.out.println("Failure loading page: " + url);
			stop();
		}
	}
	//---------------------------------------------------------------------------------------------
	public static void click(By by){
		try{
			WebElement clickable = (new WebDriverWait(driver, timeoutTime)).until(ExpectedConditions.elementToBeClickable(by));
			clickable.click();
		}
		catch(Exception e){
			System.out.println("SCRIPT STOPPED: Error in clicking element: " + by.toString());
			System.out.println(e.toString());
			stop();
		}
	}
	//---------------------------------------------------------------------------------------------
	public static void set(By by, String input){
		try{
			WebElement inputField = (new WebDriverWait(driver, timeoutTime)).until(ExpectedConditions.presenceOfElementLocated(by));
			inputField.sendKeys(input);
		}
		catch(Exception e){
			System.out.println("SCRIPT STOPPED: Error in setting element: " + by.toString());
			System.out.println(e.toString());
			stop();
		}
	}
	//---------------------------------------------------------------------------------------------
	public static void setAndEnter(By by , String input){
		set(by, input);
		driver.findElement(by).sendKeys(Keys.ENTER);
	}
	//---------------------------------------------------------------------------------------------
	public static void wait(int seconds){
		try {
			Thread.sleep((long)seconds * 1000);
		} catch (InterruptedException e) {
			System.out.println("SCRIPT STOPPED: Error with wait method");
			stop();
		}
	}
	//---------------------------------------------------------------------------------------------
	public static void stop(){
		driver.close();
		driver.quit();
		System.exit(0);
	}
	//---------------------------------------------------------------------------------------------
	// CHECKS
	//---------------------------------------------------------------------------------------------
	public static boolean exists(By element){
		return driver.findElements(element).size() != 0;
	}
	//---------------------------------------------------------------------------------------------
	public static WebElement waitForSelector(By by){
		WebElement element = null;
		try{
			element = (WebElement)new WebDriverWait(driver, timeoutTime).until(ExpectedConditions.presenceOfElementLocated(by));
		}
		catch(Exception e){
			System.out.println("SCRIPT STOPPED: Timeout trying to find element: " + by.toString());
			stop();
		}
		
		return element;
	}
	//---------------------------------------------------------------------------------------------
	public static List<WebElement> waitForSelectors(By by){
		waitForSelector(by);
		return driver.findElements(by);
	}
	//---------------------------------------------------------------------------------------------
	public static String fetchText(By by){
		
		try{
			String fullText = "";
			for(WebElement webElement : waitForSelectors(by)){
				fullText += webElement.getText();
			}
			return fullText;
		}
		catch(NoSuchElementException e){
			System.out.println("SCRIPT STOPPED: Timeout getting text from element: " + by.toString());
			stop();
			return "";
		}
	}
	//---------------------------------------------------------------------------------------------

	
	//=============================================================================================
	public static enum Browser{
		Firefox("", ""),
		Chrome("chromedriver.exe", "webdriver.chrome.driver"),
		IE("", "");
		
		private String driverFileName;
		private String driverKey;
		
		public String getDriverFileName(){
			return this.driverFileName;
		}
		
		public String getDriverPath(){
			return DRIVERS_DIRECTORY + driverFileName;
		}
		
		public String getDriverKey(){
			return this.driverKey;
		}
		
		Browser(String driverFileName, String driverKey){
			this.driverFileName = driverFileName;
			this.driverKey = driverKey;
		}
	}
	//=============================================================================================
}
