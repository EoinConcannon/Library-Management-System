package com.library.service;

import com.library.dto.BookRequest;
import com.library.dto.BookResponse;
import com.library.entity.Book;
import com.library.exception.BookAlreadyExistsException;
import com.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

	@Mock
	private BookRepository bookRepository;

	@InjectMocks
	private BookServiceImpl bookService;

	private Book mockBook;
	private BookRequest mockRequest;

	@BeforeEach
	void setUp() {
		mockBook = new Book();
		mockBook.setId(1L);
		mockBook.setTitle("Clean Code");
		mockBook.setAuthor("Robert C. Martin");
		mockBook.setIsbn("9780132350884");
		mockBook.setGenre("Technology");
		mockBook.setAvailable(true);

		mockRequest = new BookRequest();
		mockRequest.setTitle("Clean Code");
		mockRequest.setAuthor("Robert C. Martin");
		mockRequest.setIsbn("9780132350884");
		mockRequest.setGenre("Technology");
	}

	@Test
	void testAddBookReturnsBookResponse() {
		when(bookRepository.existsByIsbn("9780132350884")).thenReturn(false);
		when(bookRepository.save(any(Book.class))).thenReturn(mockBook);

		BookResponse response = bookService.addBook(mockRequest);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getTitle()).isEqualTo("Clean Code");
		assertThat(response.getAuthor()).isEqualTo("Robert C. Martin");
		assertThat(response.getIsbn()).isEqualTo("9780132350884");
		assertThat(response.getGenre()).isEqualTo("Technology");
		assertThat(response.isAvailable()).isTrue();

		verify(bookRepository).existsByIsbn("9780132350884");
		verify(bookRepository).save(any(Book.class));
	}

	@Test
	void testAddBookThrowsWhenIsbnAlreadyExists() {
		when(bookRepository.existsByIsbn("9780132350884")).thenReturn(true);

		assertThatThrownBy(() -> bookService.addBook(mockRequest)).isInstanceOf(BookAlreadyExistsException.class)
				.hasMessage("Book already exists");

		verify(bookRepository).existsByIsbn("9780132350884");
		verify(bookRepository, never()).save(any(Book.class));
	}

	@Test
	void testUpdateBookUpdatesFieldsAndReturnsResponse() {
		BookRequest updateRequest = new BookRequest();
		updateRequest.setTitle("Clean Code 2nd Ed");
		updateRequest.setAuthor("Robert C. Martin");
		updateRequest.setIsbn("9780132350884");
		updateRequest.setGenre("Technology");

		Book updatedBook = new Book();
		updatedBook.setId(1L);
		updatedBook.setTitle("Clean Code 2nd Ed");
		updatedBook.setAuthor("Robert C. Martin");
		updatedBook.setIsbn("9780132350884");
		updatedBook.setGenre("Technology");
		updatedBook.setAvailable(true);

		when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));
		when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

		BookResponse response = bookService.updateBook(1L, updateRequest);

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Clean Code 2nd Ed");
		assertThat(response.getIsbn()).isEqualTo("9780132350884");

		verify(bookRepository).findById(1L);
		verify(bookRepository).save(any(Book.class));
	}

	@Test
	void testUpdateBookThrowsWhenNewIsbnAlreadyExistsOnAnotherBook() {
		BookRequest updateRequest = new BookRequest();
		updateRequest.setTitle("Clean Code");
		updateRequest.setAuthor("Robert C. Martin");
		updateRequest.setIsbn("9999999999999");
		updateRequest.setGenre("Technology");

		when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));
		when(bookRepository.existsByIsbn("9999999999999")).thenReturn(true);

		assertThatThrownBy(() -> bookService.updateBook(1L, updateRequest))
				.isInstanceOf(BookAlreadyExistsException.class).hasMessage("Book already exists");

		verify(bookRepository).findById(1L);
		verify(bookRepository, never()).save(any(Book.class));
	}

	@Test
	void testUpdateBookThrowsWhenBookNotFound() {
		when(bookRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookService.updateBook(99L, mockRequest)).isInstanceOf(RuntimeException.class)
				.hasMessage("Book not found");

		verify(bookRepository).findById(99L);
		verify(bookRepository, never()).save(any(Book.class));
	}

	@Test
	void testUpdateBookAllowsSameIsbnForSameBook() {
		when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));
		when(bookRepository.save(any(Book.class))).thenReturn(mockBook);

		BookResponse response = bookService.updateBook(1L, mockRequest);

		assertThat(response).isNotNull();
		verify(bookRepository, never()).existsByIsbn(any());
		verify(bookRepository).save(any(Book.class));
	}

	@Test
	void testDeleteBookDeletesSuccessfullyWhenBookExists() {
		when(bookRepository.existsById(1L)).thenReturn(true);

		bookService.deleteBook(1L);

		verify(bookRepository).existsById(1L);
		verify(bookRepository).deleteById(1L);
	}

	@Test
	void testDeleteBookThrowsWhenBookNotFound() {
		when(bookRepository.existsById(99L)).thenReturn(false);

		assertThatThrownBy(() -> bookService.deleteBook(99L)).isInstanceOf(RuntimeException.class)
				.hasMessage("Book not found");

		verify(bookRepository).existsById(99L);
		verify(bookRepository, never()).deleteById(any());
	}

	@Test
	void testGetAllBooksReturnsListOfBookResponses() {
		Book secondBook = new Book();
		secondBook.setId(2L);
		secondBook.setTitle("The Pragmatic Programmer");
		secondBook.setAuthor("Andrew Hunt");
		secondBook.setIsbn("9780201616224");
		secondBook.setGenre("Technology");
		secondBook.setAvailable(false);

		when(bookRepository.findAll()).thenReturn(Arrays.asList(mockBook, secondBook));

		List<BookResponse> responses = bookService.getAllBooks();

		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).getTitle()).isEqualTo("Clean Code");
		assertThat(responses.get(1).getTitle()).isEqualTo("The Pragmatic Programmer");

		verify(bookRepository).findAll();
	}

	@Test
	void testGetAllBooksReturnsEmptyListWhenNoBooksExist() {
		when(bookRepository.findAll()).thenReturn(List.of());

		List<BookResponse> responses = bookService.getAllBooks();

		assertThat(responses).isEmpty();
		verify(bookRepository).findAll();
	}

	@Test
	void testGetBookByIdReturnsBookResponse() {
		when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));

		BookResponse response = bookService.getBookById(1L);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getTitle()).isEqualTo("Clean Code");
		assertThat(response.getAuthor()).isEqualTo("Robert C. Martin");
		assertThat(response.getIsbn()).isEqualTo("9780132350884");
		assertThat(response.isAvailable()).isTrue();

		verify(bookRepository).findById(1L);
	}

	@Test
	void testGetBookByIdThrowsWhenBookNotFound() {
		when(bookRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookService.getBookById(99L)).isInstanceOf(RuntimeException.class)
				.hasMessage("Book not found");

		verify(bookRepository).findById(99L);
	}
}