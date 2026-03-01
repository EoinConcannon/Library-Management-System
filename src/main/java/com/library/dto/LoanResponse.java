package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LoanResponse {
	private Long id;
	private Long bookId;
	private String bookTitle;
	private String bookAuthor;
	private Long userId;
	private String userName;
	private LocalDateTime borrowedAt;
	private LocalDateTime returnedAt;
	private boolean returned;
}
