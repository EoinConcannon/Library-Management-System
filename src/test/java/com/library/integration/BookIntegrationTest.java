package com.library.integration;

import com.library.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class BookIntegrationTest extends BaseIntegrationTest {

	@LocalServerPort
	private int port;

	@Test
	void getAllBooks_ReturnsOk() throws Exception {
		var client = java.net.http.HttpClient.newHttpClient();
		var request = java.net.http.HttpRequest.newBuilder()
				.uri(java.net.URI.create("http://localhost:" + port + "/api/books")).GET().build();
		var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).startsWith("[");
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