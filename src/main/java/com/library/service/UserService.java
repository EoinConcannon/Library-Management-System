package com.library.service;

import com.library.dto.CreateUserRequest;
import com.library.dto.UpdateRoleRequest;
import com.library.dto.UserResponse;

import java.util.List;

public interface UserService {
	UserResponse createUser(CreateUserRequest request);

	UserResponse updateUserRole(Long userId, UpdateRoleRequest request);

	List<UserResponse> getAllUsers();

	UserResponse getUserById(Long userId);
}