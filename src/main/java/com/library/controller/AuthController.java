package com.library.controller;

import com.library.config.JwtUtil;
import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserRepository userRepository;
	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	private final JwtUtil jwtUtil;

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
		}

		UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
		String token = jwtUtil.generateToken(userDetails);

		return userRepository.findByEmail(request.getEmail())
				.map(user -> ResponseEntity.ok(
						(Object) new LoginResponse(token, user.getName(), user.getRole().name())))
				.orElse(ResponseEntity.status(401).body(Map.of("error", "User not found")));
	}

	@GetMapping("/me")
	public ResponseEntity<Map<String, String>> me(@AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null)
			return ResponseEntity.ok(Map.of());

		return userRepository.findByEmail(userDetails.getUsername())
				.map(user -> ResponseEntity.ok(Map.of("name", user.getName(), "role", user.getRole().name())))
				.orElse(ResponseEntity.ok(Map.of()));
	}
}