package com.library.dto;

import com.library.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
	private Long id;
	private String name;
	private String email;
	private Role role;
	private String temporaryPassword; // only populated on account creation
}