package com.library.service;

import com.library.dto.StatisticsResponse;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsServiceImpl")
class StatisticsServiceImplTest {

	@Mock
	private BookRepository bookRepository;
	@Mock
	private BorrowedBookRepository borrowedBookRepository;
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private StatisticsServiceImpl statisticsService;

	private User userA;
	private User userB;
	private Book techBook;
	private Book fictionBook;

	@BeforeEach
	void setUp() {
		userA = new User();
		userA.setId(1L);
		userA.setEmail("a@example.com");

		userB = new User();
		userB.setId(2L);
		userB.setEmail("b@example.com");

		techBook = new Book();
		techBook.setId(10L);
		techBook.setTitle("Clean Code");
		techBook.setGenre("Technology");

		fictionBook = new Book();
		fictionBook.setId(11L);
		fictionBook.setTitle("Dune");
		fictionBook.setGenre("Fiction");
	}

	private BorrowedBook borrowing(User user, Book book, LocalDate date) {
		BorrowedBook b = new BorrowedBook();
		b.setUser(user);
		b.setBook(book);
		b.setBorrowedDate(date);
		return b;
	}

	private Book buildBook(Long id, String title, String genre) {
		Book b = new Book();
		b.setId(id);
		b.setTitle(title);
		b.setGenre(genre);
		return b;
	}

	@Nested
	@DisplayName("getStatistics — no genre filter")
	class GetStatisticsWithoutGenreFilter {

		@Test
		@DisplayName("returns total book count from the repository when no genre is specified")
		void returnsTotalBookCountFromRepository() {
			when(borrowedBookRepository.findAll()).thenReturn(List.of());
			when(bookRepository.count()).thenReturn(42L);

			StatisticsResponse response = statisticsService.getStatistics(null);

			assertThat(response.getTotalBooks()).isEqualTo(42L);
		}

		@Test
		@DisplayName("returns the total number of borrowings across all books")
		void returnsTotalBorrowingCount() {
			List<BorrowedBook> borrowings = List.of(borrowing(userA, techBook, LocalDate.now()),
					borrowing(userB, fictionBook, LocalDate.now()));
			when(borrowedBookRepository.findAll()).thenReturn(borrowings);
			when(bookRepository.count()).thenReturn(2L);

			StatisticsResponse response = statisticsService.getStatistics(null);

			assertThat(response.getTotalBorrowings()).isEqualTo(2);
		}

		@Test
		@DisplayName("counts only distinct users who have at least one borrowing as active users")
		void countsDistinctBorrowersAsActiveUsers() {
			List<BorrowedBook> borrowings = List.of(borrowing(userA, techBook, LocalDate.now()),
					borrowing(userA, fictionBook, LocalDate.now()), borrowing(userB, techBook, LocalDate.now()));
			when(borrowedBookRepository.findAll()).thenReturn(borrowings);
			when(bookRepository.count()).thenReturn(2L);

			StatisticsResponse response = statisticsService.getStatistics(null);

			assertThat(response.getActiveUsers()).isEqualTo(2L);
		}

		@Test
		@DisplayName("returns zero active users and zero total borrowings when no borrowings exist")
		void returnsZeroActiveUsersWhenNoBorrowingsExist() {
			when(borrowedBookRepository.findAll()).thenReturn(List.of());
			when(bookRepository.count()).thenReturn(5L);

			StatisticsResponse response = statisticsService.getStatistics(null);

			assertThat(response.getActiveUsers()).isZero();
			assertThat(response.getTotalBorrowings()).isZero();
		}

		@Test
		@DisplayName("returns the top 5 most borrowed books ordered by borrow count descending")
		void returnsTopFiveMostBorrowedBooksInDescendingOrder() {
			Book bookA = buildBook(1L, "A", "Technology");
			Book bookB = buildBook(2L, "B", "Technology");
			Book bookC = buildBook(3L, "C", "Technology");
			Book bookD = buildBook(4L, "D", "Technology");
			Book bookE = buildBook(5L, "E", "Technology");
			Book bookF = buildBook(6L, "F", "Technology");

			List<BorrowedBook> borrowings = List.of(borrowing(userA, bookA, LocalDate.now()),
					borrowing(userA, bookA, LocalDate.now()), borrowing(userA, bookA, LocalDate.now()),
					borrowing(userA, bookB, LocalDate.now()), borrowing(userA, bookB, LocalDate.now()),
					borrowing(userA, bookC, LocalDate.now()), borrowing(userA, bookC, LocalDate.now()),
					borrowing(userA, bookD, LocalDate.now()), borrowing(userA, bookE, LocalDate.now()),
					borrowing(userA, bookF, LocalDate.now()));

			when(borrowedBookRepository.findAll()).thenReturn(borrowings);
			when(bookRepository.count()).thenReturn(6L);

			StatisticsResponse response = statisticsService.getStatistics(null);

			List<Map<String, Object>> mostBorrowed = response.getMostBorrowedBooks();
			assertThat(mostBorrowed).hasSize(5);
			assertThat(mostBorrowed.get(0).get("title")).isEqualTo("A");
			assertThat(mostBorrowed.get(0).get("count")).isEqualTo(3L);
			assertThat(mostBorrowed.get(1).get("title")).isEqualTo("B");
			assertThat(mostBorrowed.get(1).get("count")).isEqualTo(2L);
		}

		@Test
		@DisplayName("returns borrowings trend entries grouped and sorted by borrow date ascending")
		void returnsBorrowingsTrendSortedByDateAscending() {
			LocalDate older = LocalDate.of(2024, 1, 10);
			LocalDate newer = LocalDate.of(2024, 3, 5);

			List<BorrowedBook> borrowings = List.of(borrowing(userA, techBook, newer),
					borrowing(userB, fictionBook, older));

			when(borrowedBookRepository.findAll()).thenReturn(borrowings);
			when(bookRepository.count()).thenReturn(2L);

			StatisticsResponse response = statisticsService.getStatistics(null);

			List<Map<String, Object>> trend = response.getBorrowingsTrend();
			assertThat(trend).hasSize(2);
			assertThat(trend.get(0).get("month")).isEqualTo("10 Jan 2024");
			assertThat(trend.get(1).get("month")).isEqualTo("05 Mar 2024");
		}

		@Test
		@DisplayName("aggregates multiple borrowings on the same date into a single trend entry")
		void aggregatesBorrowingsOnTheSameDateIntoOneTrendEntry() {
			LocalDate today = LocalDate.now();
			List<BorrowedBook> borrowings = List.of(borrowing(userA, techBook, today),
					borrowing(userB, fictionBook, today));

			when(borrowedBookRepository.findAll()).thenReturn(borrowings);
			when(bookRepository.count()).thenReturn(2L);

			StatisticsResponse response = statisticsService.getStatistics(null);

			List<Map<String, Object>> trend = response.getBorrowingsTrend();
			assertThat(trend).hasSize(1);
			assertThat(trend.get(0).get("count")).isEqualTo(2L);
		}

		@Test
		@DisplayName("returns empty most borrowed books and trend lists when there are no borrowings")
		void returnsEmptyListsWhenNoBorrowingsExist() {
			when(borrowedBookRepository.findAll()).thenReturn(List.of());
			when(bookRepository.count()).thenReturn(0L);

			StatisticsResponse response = statisticsService.getStatistics(null);

			assertThat(response.getMostBorrowedBooks()).isEmpty();
			assertThat(response.getBorrowingsTrend()).isEmpty();
		}
	}

	@Nested
	@DisplayName("getStatistics — with genre filter")
	class GetStatisticsWithGenreFilter {

		@Test
		@DisplayName("counts only books matching the given genre towards the total book count")
		void countsBooksInMatchingGenreOnly() {
			Book anotherTechBook = buildBook(12L, "Refactoring", "Technology");
			when(borrowedBookRepository.findAll()).thenReturn(List.of());
			when(bookRepository.findAll()).thenReturn(List.of(techBook, anotherTechBook, fictionBook));

			StatisticsResponse response = statisticsService.getStatistics("Technology");

			assertThat(response.getTotalBooks()).isEqualTo(2L);
		}

		@Test
		@DisplayName("includes only borrowings for books in the given genre")
		void includesOnlyBorrowingsMatchingGenre() {
			List<BorrowedBook> allBorrowings = List.of(borrowing(userA, techBook, LocalDate.now()),
					borrowing(userB, fictionBook, LocalDate.now()));
			when(borrowedBookRepository.findAll()).thenReturn(allBorrowings);
			when(bookRepository.findAll()).thenReturn(List.of(techBook, fictionBook));

			StatisticsResponse response = statisticsService.getStatistics("Technology");

			assertThat(response.getTotalBorrowings()).isEqualTo(1);
		}

		@Test
		@DisplayName("counts active users based only on borrowings within the given genre")
		void countsActiveUsersWithinGenreOnly() {
			List<BorrowedBook> allBorrowings = List.of(borrowing(userA, techBook, LocalDate.now()),
					borrowing(userB, fictionBook, LocalDate.now()));
			when(borrowedBookRepository.findAll()).thenReturn(allBorrowings);
			when(bookRepository.findAll()).thenReturn(List.of(techBook, fictionBook));

			StatisticsResponse response = statisticsService.getStatistics("Technology");

			assertThat(response.getActiveUsers()).isEqualTo(1L);
		}

		@Test
		@DisplayName("returns all borrowings unfiltered when an empty string genre is provided")
		void treatsEmptyStringGenreAsNoFilter() {
			List<BorrowedBook> allBorrowings = List.of(borrowing(userA, techBook, LocalDate.now()),
					borrowing(userB, fictionBook, LocalDate.now()));
			when(borrowedBookRepository.findAll()).thenReturn(allBorrowings);
			when(bookRepository.count()).thenReturn(2L);

			StatisticsResponse response = statisticsService.getStatistics("");

			assertThat(response.getTotalBorrowings()).isEqualTo(2);
			verify(bookRepository, never()).findAll();
		}

		@Test
		@DisplayName("returns zero totals when no borrowings exist for the given genre")
		void returnsZeroTotalsWhenNoBorrowingsMatchGenre() {
			when(borrowedBookRepository.findAll()).thenReturn(List.of(borrowing(userA, fictionBook, LocalDate.now())));
			when(bookRepository.findAll()).thenReturn(List.of(techBook));

			StatisticsResponse response = statisticsService.getStatistics("Technology");

			assertThat(response.getTotalBorrowings()).isZero();
			assertThat(response.getActiveUsers()).isZero();
			assertThat(response.getMostBorrowedBooks()).isEmpty();
			assertThat(response.getBorrowingsTrend()).isEmpty();
		}
	}
}