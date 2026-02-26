package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
	boolean existsByIsbn(String isbn);

	List<Book> findByGenre(String genre);

	List<Book> findByTitleContainingIgnoreCase(String title);
}