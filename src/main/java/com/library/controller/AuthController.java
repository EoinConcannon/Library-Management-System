package com.library.controller;

import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserRepository userRepository;

	@GetMapping("/me")
	public ResponseEntity<Map<String, String>> me(@AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null)
			return ResponseEntity.ok(Map.of());

		return userRepository.findByEmail(userDetails.getUsername())
				.map(user -> ResponseEntity.ok(Map.of("name", user.getName(), "role", user.getRole().name())))
				.orElse(ResponseEntity.ok(Map.of()));
	}
}