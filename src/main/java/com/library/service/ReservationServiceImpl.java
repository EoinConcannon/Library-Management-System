package com.library.service;

import com.library.dto.NotificationResponse;
import com.library.dto.ReservationResponse;
import com.library.entity.*;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

	private final ReservationRepository reservationRepository;
	private final NotificationRepository notificationRepository;
	private final BookRepository bookRepository;
	private final UserRepository userRepository;

	@Override
	public ReservationResponse reserveBook(Long bookId, String userEmail) {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

		if (book.isAvailable()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Book is currently available - you can borrow it directly");
		}

		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		if (reservationRepository.existsByUserAndBookAndActiveTrue(user, book)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"You already have an active reservation for this book");
		}

		Reservation reservation = new Reservation();
		reservation.setUser(user);
		reservation.setBook(book);
		reservation.setReservedDate(LocalDate.now());
		reservation.setActive(true);
		reservationRepository.save(reservation);

		return toResponse(reservation);
	}

	@Override
	public void cancelReservation(Long reservationId, String userEmail) {
		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

		if (!reservation.getUser().getEmail().equals(userEmail)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"Cannot cancel reservations that do not belong to you");
		}

		reservation.setActive(false);
		reservationRepository.save(reservation);

		// Notify next person in queue for this book
		List<Reservation> remaining = reservationRepository
				.findByBookAndActiveTrueOrderByReservedDateAsc(reservation.getBook());
		if (!remaining.isEmpty()) {
			createNotification(remaining.get(0).getUser(),
					"You are next in the queue for \"" + reservation.getBook().getTitle() + "\".");
		}
	}

	// Called by BorrowingServiceImpl when a book is returned
	public void notifyNextInQueue(Book book) {
		List<Reservation> queue = reservationRepository.findByBookAndActiveTrueOrderByReservedDateAsc(book);
		if (!queue.isEmpty()) {
			createNotification(queue.get(0).getUser(), "\"" + book.getTitle() + "\" is now available to borrow!");
		}
	}

	@Override
	public List<ReservationResponse> getActiveReservations(String userEmail) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		return reservationRepository.findByUserAndActiveTrue(user).stream().map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	public List<NotificationResponse> getUnreadNotifications(String userEmail) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user).stream()
				.map(this::toNotificationResponse).collect(Collectors.toList());
	}

	@Override
	public void markNotificationsRead(String userEmail) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		List<Notification> unread = notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
		unread.forEach(n -> n.setRead(true));
		notificationRepository.saveAll(unread);
	}

	private void createNotification(User user, String message) {
		Notification notification = new Notification();
		notification.setUser(user);
		notification.setMessage(message);
		notification.setRead(false);
		notification.setCreatedAt(LocalDateTime.now());
		notificationRepository.save(notification);
	}

	private ReservationResponse toResponse(Reservation r) {
		ReservationResponse dto = new ReservationResponse();
		dto.setId(r.getId());
		dto.setBookId(r.getBook().getId());
		dto.setTitle(r.getBook().getTitle());
		dto.setAuthor(r.getBook().getAuthor());
		dto.setGenre(r.getBook().getGenre());
		dto.setIsbn(r.getBook().getIsbn());
		dto.setReservedDate(r.getReservedDate());
		dto.setActive(r.isActive());
		return dto;
	}

	private NotificationResponse toNotificationResponse(Notification n) {
		NotificationResponse dto = new NotificationResponse();
		dto.setId(n.getId());
		dto.setMessage(n.getMessage());
		dto.setRead(n.isRead());
		dto.setCreatedAt(n.getCreatedAt());
		return dto;
	}
}