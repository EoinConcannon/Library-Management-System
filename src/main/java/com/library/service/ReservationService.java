package com.library.service;

import com.library.dto.NotificationResponse;
import com.library.dto.ReservationResponse;
import java.util.List;

public interface ReservationService {
	ReservationResponse reserveBook(Long bookId, String userEmail);

	void cancelReservation(Long reservationId, String userEmail);

	List<ReservationResponse> getActiveReservations(String userEmail);

	List<NotificationResponse> getUnreadNotifications(String userEmail);

	void markNotificationsRead(String userEmail);
}