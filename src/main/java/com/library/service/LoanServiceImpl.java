package com.library.service;

import com.library.dto.LoanResponse;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.entity.User;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

	private final LoanRepository loanRepository;
	private final BookRepository bookRepository;
	private final UserRepository userRepository;

	@Override
	public LoanResponse borrowBook(Long bookId, String userEmail) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new RuntimeException("Book not found"));

		if (!book.isAvailable()) {
			throw new RuntimeException("Book is not available for borrowing");
		}

		Loan loan = new Loan();
		loan.setUser(user);
		loan.setBook(book);
		loan.setBorrowedAt(LocalDateTime.now());
		loan.setReturned(false);

		book.setAvailable(false);
		bookRepository.save(book);

		Loan saved = loanRepository.save(loan);
		return toResponse(saved);
	}

	@Override
	public LoanResponse returnBook(Long loanId, String userEmail) {
		Loan loan = loanRepository.findById(loanId)
				.orElseThrow(() -> new RuntimeException("Loan not found"));

		if (loan.isReturned()) {
			throw new RuntimeException("Book has already been returned");
		}

		if (!loan.getUser().getEmail().equals(userEmail)) {
			throw new RuntimeException("You can only return your own borrowed books");
		}

		loan.setReturned(true);
		loan.setReturnedAt(LocalDateTime.now());

		Book book = loan.getBook();
		book.setAvailable(true);
		bookRepository.save(book);

		Loan saved = loanRepository.save(loan);
		return toResponse(saved);
	}

	@Override
	public List<LoanResponse> getMyLoans(String userEmail) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new RuntimeException("User not found"));
		return loanRepository.findByUserId(user.getId()).stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	public List<LoanResponse> getAllLoans() {
		return loanRepository.findAll().stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	public Map<String, Object> getLoanStats() {
		Map<String, Object> stats = new HashMap<>();
		long totalBooks = bookRepository.count();
		long activeBorrows = loanRepository.countByReturnedFalse();
		long completedReturns = loanRepository.countByReturnedTrue();
		long availableBooks = bookRepository.countByAvailableTrue();

		stats.put("totalBooks", totalBooks);
		stats.put("activeBorrows", activeBorrows);
		stats.put("completedReturns", completedReturns);
		stats.put("availableBooks", availableBooks);

		// Genre distribution
		List<Book> allBooks = bookRepository.findAll();
		Map<String, Long> genreCount = allBooks.stream()
				.collect(Collectors.groupingBy(Book::getGenre, Collectors.counting()));
		stats.put("genreDistribution", genreCount);

		return stats;
	}

	private LoanResponse toResponse(Loan loan) {
		return new LoanResponse(
				loan.getId(),
				loan.getBook().getId(),
				loan.getBook().getTitle(),
				loan.getBook().getAuthor(),
				loan.getUser().getId(),
				loan.getUser().getName(),
				loan.getBorrowedAt(),
				loan.getReturnedAt(),
				loan.isReturned());
	}
}
