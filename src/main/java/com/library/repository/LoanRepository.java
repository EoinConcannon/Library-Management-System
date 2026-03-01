package com.library.repository;

import com.library.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
	List<Loan> findByUserIdAndReturnedFalse(Long userId);

	List<Loan> findByUserId(Long userId);

	Optional<Loan> findByBookIdAndReturnedFalse(Long bookId);

	long countByReturnedFalse();

	long countByReturnedTrue();
}
