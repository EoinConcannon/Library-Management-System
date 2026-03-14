package com.library.service;

import com.library.dto.BorrowedBookResponse;
import com.library.entity.Book;
import com.library.entity.BorrowedBook;
import com.library.entity.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowedBookRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {

	private final BorrowedBookRepository borrowedBookRepository;
	private final BookRepository bookRepository;
	private final UserRepository userRepository;

	@Override
	public BorrowedBookResponse borrowBook(Long bookId, String userEmail) {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

		if (!book.isAvailable()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Book is not available");
		}

		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		book.setAvailable(false);
		bookRepository.save(book);

		BorrowedBook borrowedBook = new BorrowedBook();
		borrowedBook.setBook(book);
		borrowedBook.setUser(user);
		borrowedBook.setBorrowedDate(LocalDate.now());
		borrowedBookRepository.save(borrowedBook);

		return toResponse(borrowedBook);
	}

	@Override
	public List<BorrowedBookResponse> getActiveBorrowings(String userEmail) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		return borrowedBookRepository.findByUserAndReturnedDateIsNull(user).stream().map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	public List<BorrowedBookResponse> getBorrowingHistory(String userEmail) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		return borrowedBookRepository.findByUser(user).stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Override
	public BorrowedBookResponse returnBook(Long borrowingId, String userEmail) {
		BorrowedBook borrowedBook = borrowedBookRepository.findById(borrowingId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Borrowing record not found"));

		// Scenario 2 - ensure the borrowing belongs to the requesting user
		if (!borrowedBook.getUser().getEmail().equals(userEmail)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot return books you have not borrowed");
		}

		// Already returned
		if (borrowedBook.getReturnedDate() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "This book has already been returned");
		}

		borrowedBook.setReturnedDate(LocalDate.now());
		borrowedBookRepository.save(borrowedBook);

		Book book = borrowedBook.getBook();
		book.setAvailable(true);
		bookRepository.save(book);

		return toResponse(borrowedBook);
	}

	private BorrowedBookResponse toResponse(BorrowedBook b) {
		BorrowedBookResponse dto = new BorrowedBookResponse();
		dto.setId(b.getId());
		dto.setBookId(b.getBook().getId());
		dto.setTitle(b.getBook().getTitle());
		dto.setAuthor(b.getBook().getAuthor());
		dto.setGenre(b.getBook().getGenre());
		dto.setIsbn(b.getBook().getIsbn());
		dto.setBorrowedDate(b.getBorrowedDate());
		dto.setReturnedDate(b.getReturnedDate());
		return dto;
	}
}