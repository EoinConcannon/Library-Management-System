package com.library.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
	private Long id;
	private String message;
	private boolean read;
	private LocalDateTime createdAt;
}