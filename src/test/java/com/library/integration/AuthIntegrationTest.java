package com.library.integration;

import com.library.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
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