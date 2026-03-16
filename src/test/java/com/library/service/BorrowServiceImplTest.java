package com.library.service;

import com.library.dto.BorrowedBookResponse;
import com.library.entity.Book;
import com.library.entity.BorrowedBook;
import com.library.entity.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowedBookRepository;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BorrowingServiceImpl")
class BorrowingServiceImplTest {

	@Mock
	private BorrowedBookRepository borrowedBookRepository;
	@Mock
	private BookRepository bookRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private ReservationServiceImpl reservationService;

	@InjectMocks
	private BorrowingServiceImpl borrowingService;

	private User user;
	private Book book;
	private BorrowedBook borrowedBook;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(1L);
		user.setEmail("user@example.com");

		book = new Book();
		book.setId(10L);
		book.setTitle("Clean Code");
		book.setAuthor("Robert C. Martin");
		book.setIsbn("978-0132350884");
		book.setGenre("Technology");
		book.setAvailable(true);

		borrowedBook = new BorrowedBook();
		borrowedBook.setId(100L);
		borrowedBook.setBook(book);
		borrowedBook.setUser(user);
		borrowedBook.setBorrowedDate(LocalDate.now());
		borrowedBook.setReturnedDate(null);
	}

	@Nested
	@DisplayName("borrowBook")
	class BorrowBook {

		@Test
		@DisplayName("marks the book unavailable, saves a borrowing record, and returns the response")
		void borrowsBookSuccessfullyWhenAvailable() {
			when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
			when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
			when(borrowedBookRepository.save(any(BorrowedBook.class))).thenReturn(borrowedBook);

			BorrowedBookResponse response = borrowingService.borrowBook(10L, "user@example.com");

			assertThat(response).isNotNull();
			assertThat(response.getBookId()).isEqualTo(10L);
			assertThat(response.getTitle()).isEqualTo("Clean Code");
			assertThat(response.getBorrowedDate()).isEqualTo(LocalDate.now());
			assertThat(response.getReturnedDate()).isNull();
			assertThat(book.isAvailable()).isFalse();
			verify(bookRepository).save(book);
			verify(borrowedBookRepository).save(any(BorrowedBook.class));
		}

		@Test
		@DisplayName("throws 404 when the requested book does not exist")
		void throwsNotFoundWhenBookDoesNotExist() {
			when(bookRepository.findById(99L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> borrowingService.borrowBook(99L, "user@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("Book not found");

			verify(borrowedBookRepository, never()).save(any());
		}

		@Test
		@DisplayName("throws 409 when the book is already on loan to someone else")
		void throwsConflictWhenBookIsUnavailable() {
			book.setAvailable(false);
			when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

			assertThatThrownBy(() -> borrowingService.borrowBook(10L, "user@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("Book is not available");

			verify(borrowedBookRepository, never()).save(any());
		}

		@Test
		@DisplayName("throws 404 when the borrowing user does not exist")
		void throwsNotFoundWhenUserDoesNotExist() {
			when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
			when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> borrowingService.borrowBook(10L, "unknown@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("User not found");

			verify(borrowedBookRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("returnBook")
	class ReturnBook {

		@Test
		@DisplayName("sets the returned date, marks the book available, notifies the queue, and returns the response")
		void returnsBookSuccessfullyWhenAllConditionsMet() {
			when(borrowedBookRepository.findById(100L)).thenReturn(Optional.of(borrowedBook));
			when(borrowedBookRepository.save(any(BorrowedBook.class))).thenReturn(borrowedBook);

			BorrowedBookResponse response = borrowingService.returnBook(100L, "user@example.com");

			assertThat(response).isNotNull();
			assertThat(borrowedBook.getReturnedDate()).isEqualTo(LocalDate.now());
			assertThat(book.isAvailable()).isTrue();
			verify(bookRepository).save(book);
			verify(borrowedBookRepository).save(borrowedBook);
			verify(reservationService).notifyNextInQueue(book);
		}

		@Test
		@DisplayName("throws 404 when the borrowing record does not exist")
		void throwsNotFoundWhenBorrowingRecordDoesNotExist() {
			when(borrowedBookRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> borrowingService.returnBook(999L, "user@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("Borrowing record not found");
		}

		@Test
		@DisplayName("throws 403 when the user attempting to return the book did not borrow it")
		void throwsForbiddenWhenUserDidNotBorrowTheBook() {
			when(borrowedBookRepository.findById(100L)).thenReturn(Optional.of(borrowedBook));

			assertThatThrownBy(() -> borrowingService.returnBook(100L, "other@example.com"))
					.isInstanceOf(ResponseStatusException.class)
					.hasMessageContaining("Cannot return books you have not borrowed");

			verify(bookRepository, never()).save(any());
		}

		@Test
		@DisplayName("throws 409 when the book has already been returned")
		void throwsConflictWhenBookHasAlreadyBeenReturned() {
			borrowedBook.setReturnedDate(LocalDate.now().minusDays(1));
			when(borrowedBookRepository.findById(100L)).thenReturn(Optional.of(borrowedBook));

			assertThatThrownBy(() -> borrowingService.returnBook(100L, "user@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("already been returned");

			verify(bookRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("getActiveBorrowings")
	class GetActiveBorrowings {

		@Test
		@DisplayName("returns all currently active borrowings mapped to response DTOs for the given user")
		void returnsMappedActiveBorrowingsForUser() {
			when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
			when(borrowedBookRepository.findByUserAndReturnedDateIsNull(user)).thenReturn(List.of(borrowedBook));

			List<BorrowedBookResponse> results = borrowingService.getActiveBorrowings("user@example.com");

			assertThat(results).hasSize(1);
			assertThat(results.get(0).getTitle()).isEqualTo("Clean Code");
			assertThat(results.get(0).getReturnedDate()).isNull();
		}

		@Test
		@DisplayName("returns an empty list when the user has no active borrowings")
		void returnsEmptyListWhenNoActiveBorrowingsExist() {
			when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
			when(borrowedBookRepository.findByUserAndReturnedDateIsNull(user)).thenReturn(List.of());

			List<BorrowedBookResponse> results = borrowingService.getActiveBorrowings("user@example.com");

			assertThat(results).isEmpty();
		}

		@Test
		@DisplayName("throws 404 when the user does not exist")
		void throwsNotFoundWhenUserDoesNotExist() {
			when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> borrowingService.getActiveBorrowings("ghost@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("User not found");
		}
	}

	@Nested
	@DisplayName("getBorrowingHistory")
	class GetBorrowingHistory {

		@Test
		@DisplayName("returns the full borrowing history including returned books for the given user")
		void returnsMappedBorrowingHistoryForUser() {
			BorrowedBook returned = new BorrowedBook();
			returned.setId(101L);
			returned.setBook(book);
			returned.setUser(user);
			returned.setBorrowedDate(LocalDate.now().minusDays(14));
			returned.setReturnedDate(LocalDate.now().minusDays(7));

			when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
			when(borrowedBookRepository.findByUser(user)).thenReturn(List.of(borrowedBook, returned));

			List<BorrowedBookResponse> results = borrowingService.getBorrowingHistory("user@example.com");

			assertThat(results).hasSize(2);
		}

		@Test
		@DisplayName("returns an empty list when the user has never borrowed any books")
		void returnsEmptyListWhenUserHasNoBorrowingHistory() {
			when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
			when(borrowedBookRepository.findByUser(user)).thenReturn(List.of());

			List<BorrowedBookResponse> results = borrowingService.getBorrowingHistory("user@example.com");

			assertThat(results).isEmpty();
		}

		@Test
		@DisplayName("throws 404 when the user does not exist")
		void throwsNotFoundWhenUserDoesNotExist() {
			when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> borrowingService.getBorrowingHistory("ghost@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("User not found");
		}
	}
}