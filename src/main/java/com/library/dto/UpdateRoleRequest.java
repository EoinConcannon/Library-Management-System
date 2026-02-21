package com.library.dto;

import com.library.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {

	@NotNull(message = "Role is required")
	private Role role;
}