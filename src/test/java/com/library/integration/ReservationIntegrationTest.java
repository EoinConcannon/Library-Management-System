package com.library.integration;

import com.library.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ReservationIntegrationTest extends BaseIntegrationTest {

	private String librarianToken;
	private String studentToken;

	@BeforeEach
	void init() {
		super.setUp(); // sets baseURI and port
		librarianToken = getLibrarianToken();
		studentToken = getStudentToken();
	}

	// Adds a book and returns its ID
	private int seedAvailableBook() {
		return given().contentType("application/json").header("Authorization", "Bearer " + librarianToken)
				.body("{\"title\":\"Test Book\",\"author\":\"Author\"," + "\"isbn\":\"" + UUID.randomUUID()
						+ "\",\"genre\":\"Fiction\"}")
				.when().post("/api/books").then().statusCode(201).extract().path("id");
	}

	// Adds a book, has the student borrow it so it becomes unavailable, returns the
	// book ID
	private int seedUnavailableBook() {
		int bookId = seedAvailableBook();
		given().header("Authorization", "Bearer " + studentToken).when().post("/api/borrowings/" + bookId).then()
				.statusCode(201);
		return bookId;
	}

	@Test
	void reserveBook_WhenBookIsUnavailable_ReturnsCreated() {
		int bookId = seedUnavailableBook();

		// librarianToken user is not the borrower so can reserve
		given().header("Authorization", "Bearer " + librarianToken).when().post("/api/reservations/" + bookId).then()
				.statusCode(201).body("bookId", equalTo(bookId)).body("active", equalTo(true));
	}
}