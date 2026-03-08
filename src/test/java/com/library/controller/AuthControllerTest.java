package com.library.controller;

import com.library.dto.LoginRequest;
import com.library.entity.User;
import com.library.enums.Role;
import com.library.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerTest {

	@LocalServerPort
	private int port;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
		userRepository.deleteAll();

		User user = new User();
		user.setName("Jane Doe");
		user.setEmail("jane@example.com");
		user.setPassword(passwordEncoder.encode("plaintext_password"));
		user.setRole(Role.STUDENT);
		userRepository.save(user);

		User librarian = new User();
		librarian.setName("Librarian User");
		librarian.setEmail("librarian@example.com");
		librarian.setPassword(passwordEncoder.encode("librarian_password"));
		librarian.setRole(Role.LIBRARIAN);
		userRepository.save(librarian);
	}

	// POST /api/auth/login

	@Test
	void testLoginWithValidCredentialsReturnsOkWithTokenAndUserDetails() {
		LoginRequest request = new LoginRequest();
		request.setEmail("jane@example.com");
		request.setPassword("plaintext_password");

		given().contentType(ContentType.JSON).body(request).when().post("/api/auth/login").then().statusCode(200)
				.body("token", notNullValue()).body("token", not(emptyString())).body("name", equalTo("Jane Doe"))
				.body("role", equalTo("STUDENT"));
	}

	@Test
	void testLoginWithWrongPasswordReturnsUnauthorized() {
		LoginRequest request = new LoginRequest();
		request.setEmail("jane@example.com");
		request.setPassword("wrong_password");

		given().contentType(ContentType.JSON).body(request).when().post("/api/auth/login").then().statusCode(401);
	}

	@Test
	void testLoginWithUnknownEmailReturnsUnauthorized() {
		LoginRequest request = new LoginRequest();
		request.setEmail("nobody@example.com");
		request.setPassword("plaintext_password");

		given().contentType(ContentType.JSON).body(request).when().post("/api/auth/login").then().statusCode(401);
	}

	@Test
	void testLoginAsLibrarianReturnsLibrarianRole() {
		LoginRequest request = new LoginRequest();
		request.setEmail("librarian@example.com");
		request.setPassword("librarian_password");

		given().contentType(ContentType.JSON).body(request).when().post("/api/auth/login").then().statusCode(200)
				.body("role", equalTo("LIBRARIAN")).body("name", equalTo("Librarian User"));
	}

	@Test
	void testLoginReturnsAValidJwtToken() {
		LoginRequest request = new LoginRequest();
		request.setEmail("jane@example.com");
		request.setPassword("plaintext_password");

		String token = given().contentType(ContentType.JSON).body(request).when().post("/api/auth/login").then()
				.statusCode(200).extract().path("token");

		String[] parts = token.split("\\.");
		assert parts.length == 3 : "Token is not a valid JWT structure";
	}

	// GET /api/auth/me

	@Test
	void testMeWithValidTokenReturnsNameAndRole() {
		String token = loginAndGetToken("jane@example.com", "plaintext_password");

		given().header("Authorization", "Bearer " + token).when().get("/api/auth/me").then().statusCode(200)
				.body("name", equalTo("Jane Doe")).body("role", equalTo("STUDENT"));
	}

	@Test
	void testMeWithLibrarianTokenReturnsLibrarianDetails() {
		String token = loginAndGetToken("librarian@example.com", "librarian_password");

		given().header("Authorization", "Bearer " + token).when().get("/api/auth/me").then().statusCode(200)
				.body("name", equalTo("Librarian User")).body("role", equalTo("LIBRARIAN"));
	}

	@Test
	void testMeWithNoTokenReturnsEmptyOrUnauthorized() {
		given().when().get("/api/auth/me").then().statusCode(anyOf(equalTo(200), equalTo(401)));
	}

	@Test
	void testMeWithInvalidTokenReturnsUnauthorized() {
		given().header("Authorization", "Bearer totally.invalid.token").when().get("/api/auth/me").then()
				.statusCode(401);
	}

	// Helper

	private String loginAndGetToken(String email, String password) {
		LoginRequest request = new LoginRequest();
		request.setEmail(email);
		request.setPassword(password);

		return given().contentType(ContentType.JSON).body(request).when().post("/api/auth/login").then().statusCode(200)
				.extract().path("token");
	}
}