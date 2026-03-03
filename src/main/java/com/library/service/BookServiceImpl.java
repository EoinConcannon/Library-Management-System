package com.library.service;

import com.library.dto.BookRequest;
import com.library.dto.BookResponse;
import com.library.entity.Book;
import com.library.exception.BookAlreadyExistsException;
import com.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

	private final BookRepository bookRepository;

	@Override
	public BookResponse addBook(BookRequest request) {
		// Scenario 2: duplicate ISBN
		if (bookRepository.existsByIsbn(request.getIsbn())) {
			throw new BookAlreadyExistsException("Book already exists");
		}
		Book book = new Book();
		book.setTitle(request.getTitle());
		book.setAuthor(request.getAuthor());
		book.setIsbn(request.getIsbn());
		book.setGenre(request.getGenre());
		Book saved = bookRepository.save(book);
		return toResponse(saved);
	}
	
	@Override
	public List<BookResponse> searchBooks(String title, String genre) {
	    if (title != null && !title.isEmpty()) {
	        return bookRepository.findByTitleContainingIgnoreCase(title)
	                .stream().map(this::toResponse).collect(Collectors.toList());
	    }
	    if (genre != null && !genre.isEmpty()) {
	        return bookRepository.findByGenre(genre)
	                .stream().map(this::toResponse).collect(Collectors.toList());
	    }
	    return getAllBooks();
	}

	@Override
	public BookResponse updateBook(Long id, BookRequest request) {
		Book book = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));

		// Allow same ISBN if it belongs to this book
		if (!book.getIsbn().equals(request.getIsbn()) && bookRepository.existsByIsbn(request.getIsbn())) {
			throw new BookAlreadyExistsException("Book already exists");
		}
		book.setTitle(request.getTitle());
		book.setAuthor(request.getAuthor());
		book.setIsbn(request.getIsbn());
		book.setGenre(request.getGenre());
		return toResponse(bookRepository.save(book));
	}

	@Override
	public void deleteBook(Long id) {
		if (!bookRepository.existsById(id)) {
			throw new RuntimeException("Book not found");
		}
		bookRepository.deleteById(id);
	}

	@Override
	public List<BookResponse> getAllBooks() {
		return bookRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Override
	public BookResponse getBookById(Long id) {
		return bookRepository.findById(id).map(this::toResponse)
				.orElseThrow(() -> new RuntimeException("Book not found"));
	}

	private BookResponse toResponse(Book book) {
		return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(), book.getGenre(),
				book.isAvailable());
	}
}