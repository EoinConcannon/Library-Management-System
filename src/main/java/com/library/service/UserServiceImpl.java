package com.library.service;

import com.library.dto.CreateUserRequest;
import com.library.dto.UpdateRoleRequest;
import com.library.dto.UserResponse;
import com.library.entity.User;
import com.library.exception.EmailAlreadyExistsException;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public UserResponse createUser(CreateUserRequest request) {
		// Scenario 3: Prevent duplicate emails
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new EmailAlreadyExistsException("Email already registered");
		}

		// Generate a temporary password (in real app you'd email this)
		String tempPassword = UUID.randomUUID().toString().substring(0, 8);

		User user = new User();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setRole(request.getRole());
		user.setPassword(passwordEncoder.encode(tempPassword));

		User saved = userRepository.save(user);

		// Return temp password only on creation (Scenario 1)
		return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole(), tempPassword);
	}

	@Override
	public UserResponse updateUserRole(Long userId, UpdateRoleRequest request) {
		// Scenario 2: Update role
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

		user.setRole(request.getRole());
		User updated = userRepository.save(user);

		return new UserResponse(updated.getId(), updated.getName(), updated.getEmail(), updated.getRole(), null);
	}

	@Override
	public List<UserResponse> getAllUsers() {
		return userRepository.findAll().stream()
				.map(u -> new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), null))
				.collect(Collectors.toList());
	}

	@Override
	public UserResponse getUserById(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), null);
	}
}