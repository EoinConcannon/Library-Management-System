package com.library.service;

import com.library.dto.LoanResponse;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.entity.User;
import com.library.enums.Role;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

	@Mock
	private LoanRepository loanRepository;

	@Mock
	private BookRepository bookRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private LoanServiceImpl loanService;

	private User mockUser;
	private Book mockBook;
	private Loan mockLoan;

	@BeforeEach
	void setUp() {
		mockUser = new User();
		mockUser.setId(1L);
		mockUser.setName("Jane Doe");
		mockUser.setEmail("jane@example.com");
		mockUser.setRole(Role.STUDENT);

		mockBook = new Book();
		mockBook.setId(1L);
		mockBook.setTitle("Clean Code");
		mockBook.setAuthor("Robert C. Martin");
		mockBook.setIsbn("9780132350884");
		mockBook.setGenre("Technology");
		mockBook.setAvailable(true);

		mockLoan = new Loan();
		mockLoan.setId(1L);
		mockLoan.setUser(mockUser);
		mockLoan.setBook(mockBook);
		mockLoan.setBorrowedAt(LocalDateTime.now());
		mockLoan.setReturned(false);
	}

	@Test
	void testBorrowBookCreatesLoanAndMarksBookUnavailable() {
		when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(mockUser));
		when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));
		when(loanRepository.save(any(Loan.class))).thenReturn(mockLoan);
		when(bookRepository.save(any(Book.class))).thenReturn(mockBook);

		LoanResponse response = loanService.borrowBook(1L, "jane@example.com");

		assertThat(response).isNotNull();
		assertThat(response.getBookId()).isEqualTo(1L);
		assertThat(response.getUserId()).isEqualTo(1L);
		assertThat(response.isReturned()).isFalse();

		verify(bookRepository).save(any(Book.class));
		verify(loanRepository).save(any(Loan.class));
	}

	@Test
	void testBorrowBookThrowsWhenBookNotAvailable() {
		mockBook.setAvailable(false);
		when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(mockUser));
		when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));

		assertThatThrownBy(() -> loanService.borrowBook(1L, "jane@example.com"))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Book is not available for borrowing");

		verify(loanRepository, never()).save(any(Loan.class));
	}

	@Test
	void testBorrowBookThrowsWhenUserNotFound() {
		when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> loanService.borrowBook(1L, "unknown@example.com"))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("User not found");
	}

	@Test
	void testBorrowBookThrowsWhenBookNotFound() {
		when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(mockUser));
		when(bookRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> loanService.borrowBook(99L, "jane@example.com"))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Book not found");
	}

	@Test
	void testReturnBookSetsReturnedAndMakesBookAvailable() {
		when(loanRepository.findById(1L)).thenReturn(Optional.of(mockLoan));
		when(loanRepository.save(any(Loan.class))).thenReturn(mockLoan);
		when(bookRepository.save(any(Book.class))).thenReturn(mockBook);

		LoanResponse response = loanService.returnBook(1L, "jane@example.com");

		assertThat(response).isNotNull();
		verify(bookRepository).save(any(Book.class));
		verify(loanRepository).save(any(Loan.class));
	}

	@Test
	void testReturnBookThrowsWhenAlreadyReturned() {
		mockLoan.setReturned(true);
		when(loanRepository.findById(1L)).thenReturn(Optional.of(mockLoan));

		assertThatThrownBy(() -> loanService.returnBook(1L, "jane@example.com"))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Book has already been returned");
	}

	@Test
	void testReturnBookThrowsWhenDifferentUser() {
		when(loanRepository.findById(1L)).thenReturn(Optional.of(mockLoan));

		assertThatThrownBy(() -> loanService.returnBook(1L, "other@example.com"))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("You can only return your own borrowed books");
	}

	@Test
	void testReturnBookThrowsWhenLoanNotFound() {
		when(loanRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> loanService.returnBook(99L, "jane@example.com"))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Loan not found");
	}

	@Test
	void testGetMyLoansReturnsUserLoans() {
		when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(mockUser));
		when(loanRepository.findByUserId(1L)).thenReturn(Arrays.asList(mockLoan));

		List<LoanResponse> loans = loanService.getMyLoans("jane@example.com");

		assertThat(loans).hasSize(1);
		assertThat(loans.get(0).getBookTitle()).isEqualTo("Clean Code");

		verify(loanRepository).findByUserId(1L);
	}

	@Test
	void testGetAllLoansReturnsList() {
		when(loanRepository.findAll()).thenReturn(Arrays.asList(mockLoan));

		List<LoanResponse> loans = loanService.getAllLoans();

		assertThat(loans).hasSize(1);
		verify(loanRepository).findAll();
	}

	@Test
	void testGetLoanStatsReturnsStatistics() {
		when(bookRepository.count()).thenReturn(10L);
		when(loanRepository.countByReturnedFalse()).thenReturn(3L);
		when(loanRepository.countByReturnedTrue()).thenReturn(7L);
		when(bookRepository.countByAvailableTrue()).thenReturn(7L);
		when(bookRepository.findAll()).thenReturn(Arrays.asList(mockBook));

		Map<String, Object> stats = loanService.getLoanStats();

		assertThat(stats.get("totalBooks")).isEqualTo(10L);
		assertThat(stats.get("activeBorrows")).isEqualTo(3L);
		assertThat(stats.get("completedReturns")).isEqualTo(7L);
		assertThat(stats.get("availableBooks")).isEqualTo(7L);
		assertThat(stats.get("genreDistribution")).isNotNull();
	}
}
