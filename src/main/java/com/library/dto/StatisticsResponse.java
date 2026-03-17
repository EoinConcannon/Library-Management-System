package com.library.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StatisticsResponse {
	private long totalBooks;
	private long totalBorrowings;
	private long activeUsers;
	private List<Map<String, Object>> mostBorrowedBooks;
	private List<Map<String, Object>> borrowingsByGenre;
	private List<Map<String, Object>> borrowingsTrend;
}