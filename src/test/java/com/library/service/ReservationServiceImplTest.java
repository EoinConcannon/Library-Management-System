package com.library.service;

import com.library.dto.NotificationResponse;
import com.library.dto.ReservationResponse;
import com.library.entity.*;
import com.library.repository.*;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationServiceImpl")
class ReservationServiceImplTest {

	@Mock
	private ReservationRepository reservationRepository;
	@Mock
	private NotificationRepository notificationRepository;
	@Mock
	private BookRepository bookRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private BorrowedBookRepository borrowedBookRepository;

	@InjectMocks
	private ReservationServiceImpl reservationService;

	private User user;
	private Book book;
	private Reservation reservation;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(1L);
		user.setEmail("user@example.com");

		book = new Book();
		book.setId(10L);
		book.setTitle("Clean Code");
		book.setAuthor("Robert C. Martin");
		book.setGenre("Technology");
		book.setIsbn("978-0132350884");
		book.setAvailable(false);

		reservation = new Reservation();
		reservation.setId(100L);
		reservation.setUser(user);
		reservation.setBook(book);
		reservation.setReservedDate(LocalDate.now());
		reservation.setActive(true);
	}

	@Nested
	@DisplayName("reserveBook")
	class ReserveBook {

		@Test
		@DisplayName("creates and returns a reservation when the book is unavailable and the user has no existing reservation")
		void createsReservationSuccessfully() {
			when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
			when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
			when(borrowedBookRepository.findByUserAndBookIdAndReturnedDateIsNull(user, 10L))
					.thenReturn(Optional.empty());
			when(reservationRepository.existsByUserAndBookAndActiveTrue(user, book)).thenReturn(false);
			when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

			ReservationResponse response = reservationService.reserveBook(10L, "user@example.com");

			assertThat(response).isNotNull();
			assertThat(response.getBookId()).isEqualTo(10L);
			assertThat(response.getTitle()).isEqualTo("Clean Code");
			assertThat(response.isActive()).isTrue();
			verify(reservationRepository).save(any(Reservation.class));
		}

		@Test
		@DisplayName("throws 404 when the requested book does not exist")
		void throwsNotFoundWhenBookDoesNotExist() {
			when(bookRepository.findById(99L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> reservationService.reserveBook(99L, "user@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("Book not found");
		}

		@Test
		@DisplayName("throws 409 when the book is currently available to borrow directly")
		void throwsConflictWhenBookIsAvailable() {
			book.setAvailable(true);
			when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

			assertThatThrownBy(() -> reservationService.reserveBook(10L, "user@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("currently available");
		}

		@Test
		@DisplayName("throws 404 when the requesting user does not exist")
		void throwsNotFoundWhenUserDoesNotExist() {
			when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
			when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> reservationService.reserveBook(10L, "unknown@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("User not found");
		}

		@Test
		@DisplayName("throws 409 when the user is already borrowing the book they are trying to reserve")
		void throwsConflictWhenUserIsCurrentlyBorrowingTheBook() {
			BorrowedBook borrowedBook = new BorrowedBook();
			when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
			when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
			when(borrowedBookRepository.findByUserAndBookIdAndReturnedDateIsNull(user, 10L))
					.thenReturn(Optional.of(borrowedBook));

			assertThatThrownBy(() -> reservationService.reserveBook(10L, "user@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("currently borrowing");
		}

		@Test
		@DisplayName("throws 409 when the user already has an active reservation for the same book")
		void throwsConflictWhenDuplicateActiveReservationExists() {
			when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
			when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
			when(borrowedBookRepository.findByUserAndBookIdAndReturnedDateIsNull(user, 10L))
					.thenReturn(Optional.empty());
			when(reservationRepository.existsByUserAndBookAndActiveTrue(user, book)).thenReturn(true);

			assertThatThrownBy(() -> reservationService.reserveBook(10L, "user@example.com"))
					.isInstanceOf(ResponseStatusException.class)
					.hasMessageContaining("already have an active reservation");
		}
	}

	@Nested
	@DisplayName("cancelReservation")
	class CancelReservation {

		@Test
		@DisplayName("deactivates the reservation and skips notification when no one else is in the queue")
		void deactivatesReservationWithNoQueueNotification() {
			when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
			when(reservationRepository.findByBookAndActiveTrueOrderByReservedDateAsc(book)).thenReturn(List.of());

			reservationService.cancelReservation(100L, "user@example.com");

			assertThat(reservation.isActive()).isFalse();
			verify(reservationRepository).save(reservation);
			verify(notificationRepository, never()).save(any());
		}

		@Test
		@DisplayName("notifies the next user in the queue after a reservation is cancelled")
		void notifiesNextUserInQueueAfterCancellation() {
			User nextUser = new User();
			nextUser.setId(2L);
			nextUser.setEmail("next@example.com");

			Reservation nextReservation = new Reservation();
			nextReservation.setUser(nextUser);
			nextReservation.setBook(book);
			nextReservation.setActive(true);

			when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
			when(reservationRepository.findByBookAndActiveTrueOrderByReservedDateAsc(book))
					.thenReturn(List.of(nextReservation));

			reservationService.cancelReservation(100L, "user@example.com");

			verify(notificationRepository).save(
					argThat(n -> n.getUser().equals(nextUser) && n.getMessage().contains("Clean Code") && !n.isRead()));
		}

		@Test
		@DisplayName("throws 404 when the reservation to cancel does not exist")
		void throwsNotFoundWhenReservationDoesNotExist() {
			when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> reservationService.cancelReservation(999L, "user@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("Reservation not found");
		}

		@Test
		@DisplayName("throws 403 when the user attempts to cancel a reservation that belongs to someone else")
		void throwsForbiddenWhenUserDoesNotOwnTheReservation() {
			when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));

			assertThatThrownBy(() -> reservationService.cancelReservation(100L, "other@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("Cannot cancel");
		}
	}

	@Nested
	@DisplayName("notifyNextInQueue")
	class NotifyNextInQueue {

		@Test
		@DisplayName("sends an availability notification to the first user in the queue")
		void sendsNotificationToFirstUserInQueue() {
			when(reservationRepository.findByBookAndActiveTrueOrderByReservedDateAsc(book))
					.thenReturn(List.of(reservation));

			reservationService.notifyNextInQueue(book);

			verify(notificationRepository).save(argThat(n -> n.getUser().equals(user)
					&& n.getMessage().contains("Clean Code") && n.getMessage().contains("available") && !n.isRead()));
		}

		@Test
		@DisplayName("does not send any notification when the queue is empty")
		void doesNotSendNotificationWhenQueueIsEmpty() {
			when(reservationRepository.findByBookAndActiveTrueOrderByReservedDateAsc(book)).thenReturn(List.of());

			reservationService.notifyNextInQueue(book);

			verify(notificationRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("getActiveReservations")
	class GetActiveReservations {

		@Test
		@DisplayName("returns all active reservations mapped to response DTOs for the given user")
		void returnsMappedActiveReservationsForUser() {
			when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
			when(reservationRepository.findByUserAndActiveTrue(user)).thenReturn(List.of(reservation));

			List<ReservationResponse> result = reservationService.getActiveReservations("user@example.com");

			assertThat(result).hasSize(1);
			assertThat(result.get(0).getTitle()).isEqualTo("Clean Code");
			assertThat(result.get(0).getIsbn()).isEqualTo("978-0132350884");
		}

		@Test
		@DisplayName("returns an empty list when the user has no active reservations")
		void returnsEmptyListWhenNoActiveReservationsExist() {
			when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
			when(reservationRepository.findByUserAndActiveTrue(user)).thenReturn(List.of());

			List<ReservationResponse> result = reservationService.getActiveReservations("user@example.com");

			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("throws 404 when the user does not exist")
		void throwsNotFoundWhenUserDoesNotExist() {
			when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> reservationService.getActiveReservations("ghost@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("User not found");
		}
	}

	@Nested
	@DisplayName("getUnreadNotifications")
	class GetUnreadNotifications {

		@Test
		@DisplayName("returns unread notifications mapped to response DTOs for the given user")
		void returnsMappedUnreadNotificationsForUser() {
			Notification notification = new Notification();
			notification.setId(1L);
			notification.setUser(user);
			notification.setMessage("Your book is ready!");
			notification.setRead(false);
			notification.setCreatedAt(LocalDateTime.now());

			when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
			when(notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user))
					.thenReturn(List.of(notification));

			List<NotificationResponse> result = reservationService.getUnreadNotifications("user@example.com");

			assertThat(result).hasSize(1);
			assertThat(result.get(0).getMessage()).isEqualTo("Your book is ready!");
			assertThat(result.get(0).isRead()).isFalse();
		}

		@Test
		@DisplayName("throws 404 when the user does not exist")
		void throwsNotFoundWhenUserDoesNotExist() {
			when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> reservationService.getUnreadNotifications("ghost@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("User not found");
		}
	}

	@Nested
	@DisplayName("markNotificationsRead")
	class MarkNotificationsRead {

		@Test
		@DisplayName("marks all unread notifications as read and persists the changes")
		void marksAllUnreadNotificationsAsRead() {
			Notification n1 = new Notification();
			n1.setRead(false);
			Notification n2 = new Notification();
			n2.setRead(false);

			when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
			when(notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user)).thenReturn(List.of(n1, n2));

			reservationService.markNotificationsRead("user@example.com");

			assertThat(n1.isRead()).isTrue();
			assertThat(n2.isRead()).isTrue();
			verify(notificationRepository).saveAll(List.of(n1, n2));
		}

		@Test
		@DisplayName("completes without error when there are no unread notifications to mark")
		void completesGracefullyWhenNoUnreadNotificationsExist() {
			when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
			when(notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user)).thenReturn(List.of());

			reservationService.markNotificationsRead("user@example.com");

			verify(notificationRepository).saveAll(List.of());
		}

		@Test
		@DisplayName("throws 404 when the user does not exist")
		void throwsNotFoundWhenUserDoesNotExist() {
			when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> reservationService.markNotificationsRead("ghost@example.com"))
					.isInstanceOf(ResponseStatusException.class).hasMessageContaining("User not found");
		}
	}
}