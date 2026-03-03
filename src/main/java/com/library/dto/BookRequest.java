package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookRequest {

	@NotBlank(message = "Title is required")
	private String title;

	@NotBlank(message = "Author is required")
	private String author;

	@NotBlank(message = "ISBN is required")
	private String isbn;

	@NotBlank(message = "Genre is required")
	private String genre;
}