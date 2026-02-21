package com.library.controller;

import com.library.dto.CreateUserRequest;
import com.library.dto.UpdateRoleRequest;
import com.library.dto.UserResponse;
import com.library.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	// Scenario 1: Create new user account (LIBRARIAN only)
	@PostMapping
	@PreAuthorize("hasRole('LIBRARIAN')")
	public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
		UserResponse response = userService.createUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// Scenario 2: Update user role (LIBRARIAN only)
	@PatchMapping("/{userId}/role")
	@PreAuthorize("hasRole('LIBRARIAN')")
	public ResponseEntity<UserResponse> updateRole(@PathVariable Long userId,
			@Valid @RequestBody UpdateRoleRequest request) {
		UserResponse response = userService.updateUserRole(userId, request);
		return ResponseEntity.ok(response);
	}

	// Get all users (LIBRARIAN only)
	@GetMapping
	@PreAuthorize("hasRole('LIBRARIAN')")
	public ResponseEntity<List<UserResponse>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

	// Get single user
	@GetMapping("/{userId}")
	@PreAuthorize("hasRole('LIBRARIAN')")
	public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
		return ResponseEntity.ok(userService.getUserById(userId));
	}
}