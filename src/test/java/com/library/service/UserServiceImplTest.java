package com.library.service;

import com.library.dto.CreateUserRequest;
import com.library.dto.UpdateRoleRequest;
import com.library.dto.UserResponse;
import com.library.entity.User;
import com.library.enums.Role;
import com.library.exception.EmailAlreadyExistsException;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserServiceImpl userService;

	private User mockUser;

	@BeforeEach
	void setUp() {
		mockUser = new User();
		mockUser.setId(1L);
		mockUser.setName("Jane Doe");
		mockUser.setEmail("jane@example.com");
		mockUser.setRole(Role.STUDENT);
		mockUser.setPassword("encodedPassword");
	}

	@Test
	void testCreateUserReturnsUserResponseWithTempPassword() {
		// Arrange
		CreateUserRequest request = new CreateUserRequest();
		request.setName("Jane Doe");
		request.setEmail("jane@example.com");
		request.setRole(Role.STUDENT);

		when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
		when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
		when(userRepository.save(any(User.class))).thenReturn(mockUser);

		// Act
		UserResponse response = userService.createUser(request);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getName()).isEqualTo("Jane Doe");
		assertThat(response.getEmail()).isEqualTo("jane@example.com");
		assertThat(response.getRole()).isEqualTo(Role.STUDENT);
		// A temporary password should be returned on creation
		assertThat(response.getTemporaryPassword()).isNotNull();

		verify(userRepository).existsByEmail("jane@example.com");
		verify(passwordEncoder).encode(anyString());
		verify(userRepository).save(any(User.class));
	}

	@Test
	void testCreateUserThrowsWhenEmailAlreadyExists() {
		// Arrange
		CreateUserRequest request = new CreateUserRequest();
		request.setName("Jane Doe");
		request.setEmail("jane@example.com");
		request.setRole(Role.STUDENT);

		when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

		// Act & Assert
		assertThatThrownBy(() -> userService.createUser(request)).isInstanceOf(EmailAlreadyExistsException.class)
				.hasMessage("Email already registered");

		verify(userRepository).existsByEmail("jane@example.com");
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void testCreateUserEncodesPasswordBeforeSaving() {
		// Arrange
		CreateUserRequest request = new CreateUserRequest();
		request.setName("Jane Doe");
		request.setEmail("jane@example.com");
		request.setRole(Role.STUDENT);

		when(userRepository.existsByEmail(anyString())).thenReturn(false);
		when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
		when(userRepository.save(any(User.class))).thenReturn(mockUser);

		// Act
		userService.createUser(request);

		// Assert — password encoder must be called exactly once
		verify(passwordEncoder, times(1)).encode(anyString());
	}

	@Test
	void testCreateUserTempPasswordIsEightCharacters() {
		// Arrange
		CreateUserRequest request = new CreateUserRequest();
		request.setName("Jane Doe");
		request.setEmail("jane@example.com");
		request.setRole(Role.STUDENT);

		when(userRepository.existsByEmail(anyString())).thenReturn(false);
		when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
		when(userRepository.save(any(User.class))).thenReturn(mockUser);

		// Act
		UserResponse response = userService.createUser(request);

		// Assert — temp password generation uses substring(0, 8)
		assertThat(response.getTemporaryPassword()).hasSize(8);
	}

	@Test
	void testUpdateUserRoleUpdatesRoleAndReturnsResponse() {
		// Arrange
		UpdateRoleRequest request = new UpdateRoleRequest();
		request.setRole(Role.LIBRARIAN);

		User updatedUser = new User();
		updatedUser.setId(1L);
		updatedUser.setName("Jane Doe");
		updatedUser.setEmail("jane@example.com");
		updatedUser.setRole(Role.LIBRARIAN);
		updatedUser.setPassword("encodedPassword");

		when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
		when(userRepository.save(any(User.class))).thenReturn(updatedUser);

		// Act
		UserResponse response = userService.updateUserRole(1L, request);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getRole()).isEqualTo(Role.LIBRARIAN);
		// Temporary password should NOT be returned on role update
		assertThat(response.getTemporaryPassword()).isNull();

		verify(userRepository).findById(1L);
		verify(userRepository).save(any(User.class));
	}

	@Test
	void testUpdateUserRoleThrowsWhenUserNotFound() {
		// Arrange
		UpdateRoleRequest request = new UpdateRoleRequest();
		request.setRole(Role.LIBRARIAN);

		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> userService.updateUserRole(99L, request)).isInstanceOf(RuntimeException.class)
				.hasMessage("User not found");

		verify(userRepository).findById(99L);
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void testGetAllUsersReturnsListOfUserResponses() {
		// Arrange
		User secondUser = new User();
		secondUser.setId(2L);
		secondUser.setName("John Smith");
		secondUser.setEmail("john@example.com");
		secondUser.setRole(Role.LIBRARIAN);

		when(userRepository.findAll()).thenReturn(Arrays.asList(mockUser, secondUser));

		// Act
		List<UserResponse> responses = userService.getAllUsers();

		// Assert
		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).getEmail()).isEqualTo("jane@example.com");
		assertThat(responses.get(1).getEmail()).isEqualTo("john@example.com");
		// Temporary passwords should never be exposed in list responses
		responses.forEach(r -> assertThat(r.getTemporaryPassword()).isNull());

		verify(userRepository).findAll();
	}

	@Test
	void testGetAllUsersReturnsEmptyListWhenNoUsersExist() {
		// Arrange
		when(userRepository.findAll()).thenReturn(List.of());

		// Act
		List<UserResponse> responses = userService.getAllUsers();

		// Assert
		assertThat(responses).isEmpty();
		verify(userRepository).findAll();
	}

	@Test
	void testGetUserByIdReturnsUserResponse() {
		// Arrange
		when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

		// Act
		UserResponse response = userService.getUserById(1L);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getName()).isEqualTo("Jane Doe");
		assertThat(response.getEmail()).isEqualTo("jane@example.com");
		assertThat(response.getRole()).isEqualTo(Role.STUDENT);
		assertThat(response.getTemporaryPassword()).isNull();

		verify(userRepository).findById(1L);
	}

	@Test
	void testGetUserByIdThrowsWhenUserNotFound() {
		// Arrange
		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> userService.getUserById(99L)).isInstanceOf(RuntimeException.class)
				.hasMessage("User not found");

		verify(userRepository).findById(99L);
	}
}