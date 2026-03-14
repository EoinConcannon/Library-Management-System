package com.library.service;

import com.library.dto.BorrowedBookResponse;
import java.util.List;

public interface BorrowingService {
	BorrowedBookResponse borrowBook(Long bookId, String userEmail);

	List<BorrowedBookResponse> getActiveBorrowings(String userEmail);
	
	List<BorrowedBookResponse> getBorrowingHistory(String userEmail);
}