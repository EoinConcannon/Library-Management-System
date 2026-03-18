package com.library.e2e.steps;

import com.library.e2e.utility.SeleniumChromeDriver;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class UserManagementSteps {

	private WebDriver driver;
	private WebDriverWait wait;
	private static final String BASE_URL = "http://localhost:8080";

	@Before
	public void setUp() {
		driver = SeleniumChromeDriver.getDriver();
		wait = new WebDriverWait(driver, Duration.ofSeconds(15));
	}

	@Given("I am logged in as a librarian")
	public void iAmLoggedInAsALibrarian() {
		driver.get(BASE_URL + "/login.html");

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));

		driver.findElement(By.id("email")).sendKeys("test@librarian.test");
		driver.findElement(By.id("password")).sendKeys("test");
		driver.findElement(By.cssSelector(".btn-primary")).click();

		// Wait for redirect to index page
		wait.until(ExpectedConditions.urlContains("index.html"));

		// Verify JWT is stored in localStorage
		JavascriptExecutor js = (JavascriptExecutor) driver;
		String token = (String) js.executeScript("return localStorage.getItem('jwt_token');");
		assertNotNull(token, "JWT token should be present after login");
	}

	@When("I navigate to the Manage Users page")
	public void iNavigateToManageUsersPage() {
		driver.get(BASE_URL + "/create-account.html");
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("fullName")));
	}

	@When("I fill in the user registration form with name {string}, email {string} and role {string}")
	public void iFillInTheUserRegistrationForm(String name, String email, String role) {
		// Clear and fill name
		WebElement nameField = driver.findElement(By.id("fullName"));
		nameField.clear();
		nameField.sendKeys(name);

		// Clear and fill email
		WebElement emailField = driver.findElement(By.id("email"));
		emailField.clear();
		emailField.sendKeys(email);

		// Select role
		Select roleSelect = new Select(driver.findElement(By.id("roleSelect")));
		roleSelect.selectByValue(role);
	}

	@When("I submit the user registration form")
	public void iSubmitTheUserRegistrationForm() {
	    WebElement submitBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submitBtn")));

	    // Scroll into view first
	    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitBtn);

	    // Small pause to allow any animations to settle
	    try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

	    // Use JavaScript click to bypass any overlay issues
	    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);

	    // Wait for credential card to appear
	    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credentialCard")));
	}

	@Then("a new user account is created")
	public void aNewUserAccountIsCreated() {
		// Success alert should be visible
		WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("alertSuccess")));
		assertTrue(successAlert.isDisplayed(), "Success alert should be visible");
	}

	@Then("the login credentials are displayed on screen")
	public void theLoginCredentialsAreDisplayedOnScreen() {
		WebElement credentialCard = driver.findElement(By.id("credentialCard"));
		assertTrue(credentialCard.isDisplayed(), "Credential card should be visible");

		// Verify the credential fields are populated
		String credName = driver.findElement(By.id("credName")).getText();
		String credEmail = driver.findElement(By.id("credEmail")).getText();
		String credPassword = driver.findElement(By.id("credPassword")).getText();

		assertFalse(credName.isEmpty(), "Credential name should not be empty");
		assertFalse(credEmail.isEmpty(), "Credential email should not be empty");
		assertFalse(credPassword.isEmpty(), "Credential password should not be empty");
	}
}