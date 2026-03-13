package com.library;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
public abstract class BaseIntegrationTest {

	@LocalServerPort
	private int port;

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
		RestAssured.baseURI = "http://localhost";
	}

	// Helper to get a librarian token for protected endpoint tests
	protected String getLibrarianToken() {
		io.restassured.response.Response response = RestAssured.given().contentType("application/json")
				.body("{\"email\":\"test@librarian.test\",\"password\":\"test\"}").when().post("/api/auth/login");

		System.out.println("LOGIN RESPONSE STATUS: " + response.getStatusCode());
		System.out.println("LOGIN RESPONSE BODY: " + response.getBody().asString());

		return response.then().statusCode(200).extract().path("token");
	}

	protected String getStudentToken() {
		return io.restassured.RestAssured.given().contentType("application/json")
				.body("{\"email\":\"test@student.test\",\"password\":\"test\"}").when().post("/api/auth/login").then()
				.statusCode(200).extract().path("token");
	}
}