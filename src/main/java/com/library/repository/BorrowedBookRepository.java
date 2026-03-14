package com.library.repository;

import com.library.entity.BorrowedBook;
import com.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BorrowedBookRepository extends JpaRepository<BorrowedBook, Long> {
	List<BorrowedBook> findByUserAndReturnedDateIsNull(User user);

	List<BorrowedBook> findByUser(User user);

	Optional<BorrowedBook> findByUserAndBookIdAndReturnedDateIsNull(User user, Long bookId);
}