package com.library.service;

import com.library.dto.BookRequest;
import com.library.dto.BookResponse;
import com.library.entity.Book;
import com.library.exception.BookAlreadyExistsException;
import com.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookServiceImpl")
class BookServiceImplTest {

	@Mock
	private BookRepository bookRepository;

	@InjectMocks
	private BookServiceImpl bookService;

	private Book book;
	private BookRequest request;

	@BeforeEach
	void setUp() {
		book = new Book();
		book.setId(1L);
		book.setTitle("Clean Code");
		book.setAuthor("Robert C. Martin");
		book.setIsbn("978-0132350884");
		book.setGenre("Technology");
		book.setAvailable(true);

		request = new BookRequest();
		request.setTitle("Clean Code");
		request.setAuthor("Robert C. Martin");
		request.setIsbn("978-0132350884");
		request.setGenre("Technology");
	}

	@Nested
	@DisplayName("addBook")
	class AddBook {

		@Test
		@DisplayName("saves and returns the new book when the ISBN is not already registered")
		void savesAndReturnsBookWhenIsbnIsUnique() {
			when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(false);
			when(bookRepository.save(any(Book.class))).thenReturn(book);

			BookResponse response = bookService.addBook(request);

			assertThat(response).isNotNull();
			assertThat(response.getTitle()).isEqualTo("Clean Code");
			assertThat(response.getAuthor()).isEqualTo("Robert C. Martin");
			assertThat(response.getIsbn()).isEqualTo("978-0132350884");
			assertThat(response.getGenre()).isEqualTo("Technology");
			verify(bookRepository).save(any(Book.class));
		}

		@Test
		@DisplayName("throws BookAlreadyExistsException when a book with the same ISBN already exists")
		void throwsWhenIsbnIsDuplicate() {
			when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(true);

			assertThatThrownBy(() -> bookService.addBook(request)).isInstanceOf(BookAlreadyExistsException.class)
					.hasMessageContaining("Book already exists");

			verify(bookRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("searchBooks")
	class SearchBooks {

		@Test
		@DisplayName("searches by title when a non-empty title is provided")
		void returnsBooksMatchingTitleWhenTitleIsProvided() {
			when(bookRepository.findByTitleContainingIgnoreCase("Clean")).thenReturn(List.of(book));

			List<BookResponse> results = bookService.searchBooks("Clean", null);

			assertThat(results).hasSize(1);
			assertThat(results.get(0).getTitle()).isEqualTo("Clean Code");
			verify(bookRepository).findByTitleContainingIgnoreCase("Clean");
			verify(bookRepository, never()).findByGenre(any());
		}

		@Test
		@DisplayName("searches by genre when no title is provided but a genre is")
		void returnsBooksMatchingGenreWhenOnlyGenreIsProvided() {
			when(bookRepository.findByGenre("Technology")).thenReturn(List.of(book));

			List<BookResponse> results = bookService.searchBooks(null, "Technology");

			assertThat(results).hasSize(1);
			assertThat(results.get(0).getGenre()).isEqualTo("Technology");
			verify(bookRepository).findByGenre("Technology");
			verify(bookRepository, never()).findByTitleContainingIgnoreCase(any());
		}

		@Test
		@DisplayName("returns all books when both title and genre are null")
		void returnsAllBooksWhenNoFiltersAreProvided() {
			when(bookRepository.findAll()).thenReturn(List.of(book));

			List<BookResponse> results = bookService.searchBooks(null, null);

			assertThat(results).hasSize(1);
			verify(bookRepository).findAll();
		}

		@Test
		@DisplayName("returns all books when both title and genre are empty strings")
		void returnsAllBooksWhenFiltersAreEmptyStrings() {
			when(bookRepository.findAll()).thenReturn(List.of(book));

			List<BookResponse> results = bookService.searchBooks("", "");

			assertThat(results).hasSize(1);
			verify(bookRepository).findAll();
		}

		@Test
		@DisplayName("title filter takes precedence when both title and genre are provided")
		void prioritisesTitleOverGenreWhenBothAreProvided() {
			when(bookRepository.findByTitleContainingIgnoreCase("Clean")).thenReturn(List.of(book));

			List<BookResponse> results = bookService.searchBooks("Clean", "Technology");

			assertThat(results).hasSize(1);
			verify(bookRepository).findByTitleContainingIgnoreCase("Clean");
			verify(bookRepository, never()).findByGenre(any());
		}
	}

	@Nested
	@DisplayName("updateBook")
	class UpdateBook {

		@Test
		@DisplayName("updates all fields and returns the saved book when the ISBN is unchanged")
		void updatesBookSuccessfullyWhenIsbnIsUnchanged() {
			when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
			when(bookRepository.save(any(Book.class))).thenReturn(book);

			BookResponse response = bookService.updateBook(1L, request);

			assertThat(response).isNotNull();
			assertThat(response.getTitle()).isEqualTo("Clean Code");
			verify(bookRepository).save(any(Book.class));
		}

		@Test
		@DisplayName("updates the book when the new ISBN is unique across all other books")
		void updatesBookSuccessfullyWhenNewIsbnIsUnique() {
			BookRequest updatedRequest = new BookRequest();
			updatedRequest.setTitle("Clean Code");
			updatedRequest.setAuthor("Robert C. Martin");
			updatedRequest.setIsbn("978-9999999999");
			updatedRequest.setGenre("Technology");

			when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
			when(bookRepository.existsByIsbn("978-9999999999")).thenReturn(false);
			when(bookRepository.save(any(Book.class))).thenReturn(book);

			BookResponse response = bookService.updateBook(1L, updatedRequest);

			assertThat(response).isNotNull();
			verify(bookRepository).save(any(Book.class));
		}

		@Test
		@DisplayName("throws BookAlreadyExistsException when the new ISBN belongs to a different book")
		void throwsWhenNewIsbnIsAlreadyTakenByAnotherBook() {
			BookRequest updatedRequest = new BookRequest();
			updatedRequest.setTitle("Clean Code");
			updatedRequest.setAuthor("Robert C. Martin");
			updatedRequest.setIsbn("978-9999999999");
			updatedRequest.setGenre("Technology");

			when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
			when(bookRepository.existsByIsbn("978-9999999999")).thenReturn(true);

			assertThatThrownBy(() -> bookService.updateBook(1L, updatedRequest))
					.isInstanceOf(BookAlreadyExistsException.class).hasMessageContaining("Book already exists");

			verify(bookRepository, never()).save(any());
		}

		@Test
		@DisplayName("throws RuntimeException when the book to update does not exist")
		void throwsWhenBookToUpdateDoesNotExist() {
			when(bookRepository.findById(99L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> bookService.updateBook(99L, request)).isInstanceOf(RuntimeException.class)
					.hasMessageContaining("Book not found");
		}
	}

	@Nested
	@DisplayName("deleteBook")
	class DeleteBook {

		@Test
		@DisplayName("deletes the book when it exists")
		void deletesBookSuccessfullyWhenItExists() {
			when(bookRepository.existsById(1L)).thenReturn(true);

			bookService.deleteBook(1L);

			verify(bookRepository).deleteById(1L);
		}

		@Test
		@DisplayName("throws RuntimeException when the book to delete does not exist")
		void throwsWhenBookToDeleteDoesNotExist() {
			when(bookRepository.existsById(99L)).thenReturn(false);

			assertThatThrownBy(() -> bookService.deleteBook(99L)).isInstanceOf(RuntimeException.class)
					.hasMessageContaining("Book not found");

			verify(bookRepository, never()).deleteById(any());
		}
	}

	@Nested
	@DisplayName("getAllBooks")
	class GetAllBooks {

		@Test
		@DisplayName("returns all books mapped to response DTOs")
		void returnsMappedListOfAllBooks() {
			when(bookRepository.findAll()).thenReturn(List.of(book));

			List<BookResponse> results = bookService.getAllBooks();

			assertThat(results).hasSize(1);
			assertThat(results.get(0).getIsbn()).isEqualTo("978-0132350884");
		}

		@Test
		@DisplayName("returns an empty list when no books are stored")
		void returnsEmptyListWhenNoBooksExist() {
			when(bookRepository.findAll()).thenReturn(List.of());

			List<BookResponse> results = bookService.getAllBooks();

			assertThat(results).isEmpty();
		}
	}

	@Nested
	@DisplayName("getBookById")
	class GetBookById {

		@Test
		@DisplayName("returns the matching book mapped to a response DTO")
		void returnsMappedBookWhenItExists() {
			when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

			BookResponse response = bookService.getBookById(1L);

			assertThat(response).isNotNull();
			assertThat(response.getId()).isEqualTo(1L);
			assertThat(response.getTitle()).isEqualTo("Clean Code");
		}

		@Test
		@DisplayName("throws RuntimeException when no book exists with the given ID")
		void throwsWhenBookDoesNotExist() {
			when(bookRepository.findById(99L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> bookService.getBookById(99L)).isInstanceOf(RuntimeException.class)
					.hasMessageContaining("Book not found");
		}
	}
}