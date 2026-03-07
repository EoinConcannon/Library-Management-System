package com.library.integration;

import com.library.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class BookIntegrationTest extends BaseIntegrationTest {

	@Test
	void getAllBooks_ReturnsOk() {
		given().when().get("/api/books").then().statusCode(200).body("$", instanceOf(java.util.List.class));
	}

	@Test
	void addBook_AsLibrarian_ReturnsCreated() {
		String token = getLibrarianToken();

		given().contentType("application/json").header("Authorization", "Bearer " + token)
				.body("{\"title\":\"Test Book\",\"author\":\"Test Author\","
						+ "\"isbn\":\"978-test-001\",\"genre\":\"Fiction\"}")
				.when().post("/api/books").then().statusCode(201).body("title", equalTo("Test Book"))
				.body("available", equalTo(true));
	}

	@Test
	void addBook_AsStudent_Returns403() {
		String token = getStudentToken();

		given().contentType("application/json").header("Authorization", "Bearer " + token)
				.body("{\"title\":\"Test Book\",\"author\":\"Test Author\","
						+ "\"isbn\":\"978-test-002\",\"genre\":\"Fiction\"}")
				.when().post("/api/books").then().statusCode(403);
	}

	@Test
	void addBook_WithDuplicateISBN_Returns409() {
		String token = getLibrarianToken();
		String body = "{\"title\":\"Duplicate\",\"author\":\"Author\","
				+ "\"isbn\":\"978-duplicate-001\",\"genre\":\"Fiction\"}";

		// Add once
		given().contentType("application/json").header("Authorization", "Bearer " + token).body(body).when()
				.post("/api/books").then().statusCode(201);

		// Add again — should get 409
		given().contentType("application/json").header("Authorization", "Bearer " + token).body(body).when()
				.post("/api/books").then().statusCode(409).body("error", equalTo("Book already exists"));
	}
}