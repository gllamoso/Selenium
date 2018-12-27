package selenium;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Selenium {
	//---------------------------------------------------------------------------------------------
	private static final String DRIVERS_DIRECTORY = "..\\Selenium\\drivers\\";
	private static final String RESULTS_FOLDER_NAME = getDateTime("yyyyMMdd_hhmmss");
	private static final String RESULTS_DIRECTORY = "Results\\" + RESULTS_FOLDER_NAME + "\\";
	private static final String FINAL_RESULTS_LOG = "Results\\all.txt";
	//---------------------------------------------------------------------------------------------
	private static WebDriver driver;
	private static int timeoutTime = 3;
	private static final PrintStream console = System.out;
	private static PrintStream log;
	private static PrintStream results;
	static{
		try{
			new File(RESULTS_DIRECTORY).mkdirs();
			log = new PrintStream(new File(RESULTS_DIRECTORY + "log.txt"));
			results = new PrintStream(new File(RESULTS_DIRECTORY + "results.txt"));
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
			System.exit(0);
		}
	}
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
		log("Browser set to " + browser.toString());
	}
	//---------------------------------------------------------------------------------------------
	public static void setTimeoutTime(int seconds){
		timeoutTime = seconds;
		driver.manage().timeouts().implicitlyWait(timeoutTime, TimeUnit.SECONDS);
		log("timeoutTime set to " + seconds);
	}
	//---------------------------------------------------------------------------------------------
	//ACTIONS
	//---------------------------------------------------------------------------------------------
	public static void navigate(String url){
		try{
			driver.get(url);
			log("Navigate: " + url);
		}
		catch(Exception e){
			stop("Failure loading page: " + url);
		}
	}
	//---------------------------------------------------------------------------------------------
	public static void click(By by, String label){
		try{
			WebElement clickable = (new WebDriverWait(driver, timeoutTime)).until(ExpectedConditions.elementToBeClickable(by));
			clickable.click();
			
			// Logging
			if(label.equals(""))
				log("Clicked " + by.toString());
			else
				result("Clicked " + label + "");
		}
		catch(Exception e){
			result(e.toString());
			stop("FAILURE: Error in clicking element " + by.toString());
		}
	}
	//---------------------------------------------------------------------------------------------
	public static void click(By by){
		click(by, "");
	}
	//---------------------------------------------------------------------------------------------
	public static void set(By by, String input, String label){
		try{
			WebElement inputField = (new WebDriverWait(driver, timeoutTime)).until(ExpectedConditions.presenceOfElementLocated(by));
			inputField.sendKeys(input);
			
			// Logging
			if(label.equals(""))
				log("Set \"" + input + "\" to " + by.toString());
			else
				result("Set \"" + input + "\" to " + label);
		}
		catch(Exception e){
			result(e.toString());
			stop("FAILURE: Error in setting element " + by.toString());
		}
	}
	//---------------------------------------------------------------------------------------------
	public static void set(By by, String input){
		set(by, input, "");
	}
	//---------------------------------------------------------------------------------------------
	public static void setAndEnter(By by , String input, String label){
		set(by, input, label);
		driver.findElement(by).sendKeys(Keys.ENTER);
	}
	//---------------------------------------------------------------------------------------------
	public static void setAndEnter(By by , String input){
		setAndEnter(by, input, "");
	}
	//---------------------------------------------------------------------------------------------
	public static void wait(int seconds){
		try {
			Thread.sleep((long)seconds * 1000);
			log("Wait for " + seconds + " seconds");
		} catch (InterruptedException e) {
			result(e.toString());
			stop("FAILURE: Error with wait");
		}
	}
	//---------------------------------------------------------------------------------------------
	public static void stop(){
		driver.close();
		driver.quit();
		System.exit(0);
	}
	//---------------------------------------------------------------------------------------------
	public static void stop(String message){
		result(message);
		finalResult(message);
		stop();
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
	// DATE/TIME
	public static String getDateTime(String pattern){
		return DateTimeFormatter.ofPattern(pattern).format(LocalDateTime.now());
	}
	//---------------------------------------------------------------------------------------------
	// RESULTS AND LOGGING
	public static void log(String message){
		message = "\n[" + getDateTime("MM-dd-yyyy: hh:mm:ss") + "][Log]: " + message;
		
		System.setOut(log);
		System.out.println(message);
		System.setOut(console);
		System.out.println(message);
	}
	//---------------------------------------------------------------------------------------------
	public static void result(String message){
		message = "\n[" + getDateTime("MM-dd-yyyy: hh:mm:ss") + "][Result]: " + message;
		
		System.setOut(results);
		System.out.println(message);
		System.setOut(log);
		System.out.println(message);
		System.setOut(console);
		System.out.println(message);
	}
	//---------------------------------------------------------------------------------------------
	public static void finalResult(String message){
		message = RESULTS_FOLDER_NAME + ": " + message;
		
		try {
			FileWriter fw = new FileWriter(FINAL_RESULTS_LOG, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			pw.append(message);
			pw.println();
			pw.println();
			pw.close();
		}
		catch (IOException e){
			log(e.toString());
		}
	}
	//---------------------------------------------------------------------------------------------
	public static void screenshot(String name){
		TakesScreenshot screenshot = (TakesScreenshot)driver;
		File srcFile = screenshot.getScreenshotAs(OutputType.FILE);
		File destFile = new File(RESULTS_DIRECTORY + name + ".png");
		try {
			FileUtils.copyFile(srcFile, destFile);
			log("Screenshot saved in results folder");
		} catch (IOException e) {
			result(e.toString());
			stop("FAILURE: Unable to copy screenshot");
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
