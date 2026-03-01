package com.library.service;

import com.library.dto.LoanResponse;

import java.util.List;
import java.util.Map;

public interface LoanService {
	LoanResponse borrowBook(Long bookId, String userEmail);

	LoanResponse returnBook(Long loanId, String userEmail);

	List<LoanResponse> getMyLoans(String userEmail);

	List<LoanResponse> getAllLoans();

	Map<String, Object> getLoanStats();
}
