package com.library.config;

import com.library.entity.User;
import com.library.enums.Role;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitialiser implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) {
		createIfNotExists("librarian_test", "test@librarian.test", "test", Role.LIBRARIAN);
		createIfNotExists("student_test", "test@student.test", "test", Role.STUDENT);
	}

	private void createIfNotExists(String name, String email, String password, Role role) {
		if (!userRepository.existsByEmail(email)) {
			User user = new User();
			user.setName(name);
			user.setEmail(email);
			user.setPassword(passwordEncoder.encode(password));
			user.setRole(role);
			userRepository.save(user);
			System.out.println("Created user: " + email);
		}
	}
}