package com.library.integration;

import com.library.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class AuthIntegrationTest extends BaseIntegrationTest {

	@Test
	void login_WithValidCredentials_ReturnsToken() {
		given().contentType("application/json").body("{\"email\":\"test@librarian.test\",\"password\":\"test\"}").when()
				.post("/api/auth/login").then().statusCode(200).body("token", notNullValue())
				.body("name", equalTo("librarian_test")).body("role", equalTo("LIBRARIAN"));
	}

	@Test
	void login_WithInvalidCredentials_Returns401() {
		given().contentType("application/json").body("{\"email\":\"wrong@email.com\",\"password\":\"wrongpassword\"}")
				.when().post("/api/auth/login").then().statusCode(401);
	}
}