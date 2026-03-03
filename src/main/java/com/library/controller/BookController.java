package com.library.controller;

import com.library.dto.BookRequest;
import com.library.dto.BookResponse;
import com.library.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

	private final BookService bookService;

	// Public - students and librarians can see all books
	@GetMapping
	public ResponseEntity<List<BookResponse>> getAllBooks() {
		return ResponseEntity.ok(bookService.getAllBooks());
	}

	@GetMapping("/{id}")
	public ResponseEntity<BookResponse> getBook(@PathVariable Long id) {
		return ResponseEntity.ok(bookService.getBookById(id));
	}

	// Librarian only below
	@PostMapping
	@PreAuthorize("hasRole('LIBRARIAN')")
	public ResponseEntity<BookResponse> addBook(@Valid @RequestBody BookRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(bookService.addBook(request));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('LIBRARIAN')")
	public ResponseEntity<BookResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
		return ResponseEntity.ok(bookService.updateBook(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('LIBRARIAN')")
	public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
		bookService.deleteBook(id);
		return ResponseEntity.noContent().build();
	}
}