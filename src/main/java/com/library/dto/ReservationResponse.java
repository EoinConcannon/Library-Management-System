package com.library.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReservationResponse {
	private Long id;
	private Long bookId;
	private String title;
	private String author;
	private String genre;
	private String isbn;
	private LocalDate reservedDate;
	private boolean active;
}