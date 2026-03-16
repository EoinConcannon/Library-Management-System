package com.library.controller;

import com.library.dto.NotificationResponse;
import com.library.dto.ReservationResponse;
import com.library.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	@PostMapping("/reservations/{bookId}")
	public ResponseEntity<ReservationResponse> reserveBook(@PathVariable Long bookId, Authentication authentication) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(reservationService.reserveBook(bookId, authentication.getName()));
	}

	@DeleteMapping("/reservations/{reservationId}")
	public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId, Authentication authentication) {
		reservationService.cancelReservation(reservationId, authentication.getName());
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/reservations/my")
	public ResponseEntity<List<ReservationResponse>> getMyReservations(Authentication authentication) {
		return ResponseEntity.ok(reservationService.getActiveReservations(authentication.getName()));
	}

	@GetMapping("/notifications/my")
	public ResponseEntity<List<NotificationResponse>> getMyNotifications(Authentication authentication) {
		return ResponseEntity.ok(reservationService.getUnreadNotifications(authentication.getName()));
	}

	@PatchMapping("/notifications/read")
	public ResponseEntity<Void> markNotificationsRead(Authentication authentication) {
		reservationService.markNotificationsRead(authentication.getName());
		return ResponseEntity.noContent().build();
	}
}