package com.library.e2e.utility;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.HashMap;
import java.util.Map;

public class SeleniumChromeDriver {

	private static WebDriver driver;

	private SeleniumChromeDriver() {
	}

	public static WebDriver getDriver() {
		if (driver == null) {
			initialiseDriver();
		}
		return driver;
	}

	private static void initialiseDriver() {
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver(buildChromeOptions());
		configureTimeouts();
		registerShutdownHook();
	}

	private static void configureTimeouts() {
		driver.manage().window().maximize();
	}

	private static ChromeOptions buildChromeOptions() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments(getBrowserArguments());
		options.setExperimentalOption("prefs", buildChromePreferences());
		options.setExperimentalOption("excludeSwitches", new String[] { "enable-automation" });
		options.setExperimentalOption("useAutomationExtension", false);
		options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE);
		return options;
	}

	private static String[] getBrowserArguments() {
		return new String[] { "--headless=new", "--disable-notifications", "--remote-allow-origins=*", "--disable-gpu",
				"--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920,1080" };
	}

	private static Map<String, Object> buildChromePreferences() {
		Map<String, Object> prefs = new HashMap<>();
		prefs.put("credentials_enable_service", false);
		prefs.put("profile.password_manager_enabled", false);
		prefs.put("profile.password_manager_leak_detection", false);
		prefs.put("profile.default_content_setting_values.notifications", 2);
		return prefs;
	}

	private static void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(SeleniumChromeDriver::terminateDriver));
	}

	private static void terminateDriver() {
		if (driver != null) {
			driver.quit();
			driver = null;
		}
	}
}