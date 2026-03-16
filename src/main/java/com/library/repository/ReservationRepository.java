package com.library.repository;

import com.library.entity.Book;
import com.library.entity.Reservation;
import com.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	List<Reservation> findByUserAndActiveTrue(User user);

	Optional<Reservation> findByUserAndBookAndActiveTrue(User user, Book book);

	List<Reservation> findByBookAndActiveTrueOrderByReservedDateAsc(Book book);

	boolean existsByUserAndBookAndActiveTrue(User user, Book book);
}