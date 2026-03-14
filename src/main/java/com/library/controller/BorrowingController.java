package com.library.controller;

import com.library.dto.BorrowedBookResponse;
import com.library.service.BorrowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

	private final BorrowingService borrowingService;

	@PostMapping("/{bookId}")
	public ResponseEntity<BorrowedBookResponse> borrowBook(@PathVariable Long bookId, Authentication authentication) {
		String email = authentication.getName();
		return ResponseEntity.status(HttpStatus.CREATED).body(borrowingService.borrowBook(bookId, email));
	}

	@GetMapping("/my")
	public ResponseEntity<List<BorrowedBookResponse>> getMyBorrowings(Authentication authentication) {
		String email = authentication.getName();
		return ResponseEntity.ok(borrowingService.getActiveBorrowings(email));
	}

	@GetMapping("/my/history")
	public ResponseEntity<List<BorrowedBookResponse>> getMyHistory(Authentication authentication) {
		String email = authentication.getName();
		return ResponseEntity.ok(borrowingService.getBorrowingHistory(email));
	}

	@PatchMapping("/{borrowingId}/return")
	public ResponseEntity<BorrowedBookResponse> returnBook(@PathVariable Long borrowingId,
			Authentication authentication) {
		String email = authentication.getName();
		return ResponseEntity.ok(borrowingService.returnBook(borrowingId, email));
	}
}