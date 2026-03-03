package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookResponse {
	private Long id;
	private String title;
	private String author;
	private String isbn;
	private String genre;
	private boolean available;
}