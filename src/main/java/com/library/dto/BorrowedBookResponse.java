package com.library.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BorrowedBookResponse {
	private Long id;
	private Long bookId;
	private String title;
	private String author;
	private String genre;
	private String isbn;
	private LocalDate borrowedDate;
	private LocalDate returnedDate;
}