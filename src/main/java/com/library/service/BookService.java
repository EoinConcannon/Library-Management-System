package com.library.service;

import com.library.dto.BookRequest;
import com.library.dto.BookResponse;
import java.util.List;

public interface BookService {
	BookResponse addBook(BookRequest request);

	BookResponse updateBook(Long id, BookRequest request);

	void deleteBook(Long id);

	List<BookResponse> getAllBooks();

	BookResponse getBookById(Long id);
}