package com.library.controller;

import com.library.dto.LoanRequest;
import com.library.dto.LoanResponse;
import com.library.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

	private final LoanService loanService;

	@PostMapping("/borrow")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<LoanResponse> borrowBook(@Valid @RequestBody LoanRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {
		LoanResponse response = loanService.borrowBook(request.getBookId(), userDetails.getUsername());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/{loanId}/return")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<LoanResponse> returnBook(@PathVariable Long loanId,
			@AuthenticationPrincipal UserDetails userDetails) {
		LoanResponse response = loanService.returnBook(loanId, userDetails.getUsername());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/my")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<LoanResponse>> getMyLoans(
			@AuthenticationPrincipal UserDetails userDetails) {
		return ResponseEntity.ok(loanService.getMyLoans(userDetails.getUsername()));
	}

	@GetMapping
	@PreAuthorize("hasRole('LIBRARIAN')")
	public ResponseEntity<List<LoanResponse>> getAllLoans() {
		return ResponseEntity.ok(loanService.getAllLoans());
	}

	@GetMapping("/stats")
	public ResponseEntity<Map<String, Object>> getLoanStats() {
		return ResponseEntity.ok(loanService.getLoanStats());
	}
}
