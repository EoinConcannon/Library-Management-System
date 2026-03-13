package com.library.controller;

import com.library.config.JwtUtil;
import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;
	private final PasswordEncoder passwordEncoder;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		return userRepository.findByEmail(request.getEmail())
				.filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword())).map(user -> {
					String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
					return ResponseEntity.ok(new LoginResponse(token, user.getName(), user.getRole().name()));
				}).orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
	}

	@GetMapping("/me")
	public ResponseEntity<Map<String, String>> me(@AuthenticationPrincipal String email) {
		if (email == null)
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		return userRepository.findByEmail(email)
				.map(user -> ResponseEntity.ok(Map.of("name", user.getName(), "role", user.getRole().name())))
				.orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
	}
}