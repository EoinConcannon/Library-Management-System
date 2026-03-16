package com.library.service;

import com.library.dto.StatisticsResponse;
import com.library.entity.BorrowedBook;
import com.library.repository.BookRepository;
import com.library.repository.BorrowedBookRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

	private final BookRepository bookRepository;
	private final BorrowedBookRepository borrowedBookRepository;
	private final UserRepository userRepository;

	@Override
	public StatisticsResponse getStatistics(String genre) {
		StatisticsResponse response = new StatisticsResponse();

		List<BorrowedBook> allBorrowings = borrowedBookRepository.findAll();

		// Filter by genre if provided
		if (genre != null && !genre.isEmpty()) {
			allBorrowings = allBorrowings.stream().filter(b -> genre.equals(b.getBook().getGenre()))
					.collect(Collectors.toList());
		}

		// Total books
		response.setTotalBooks(genre != null && !genre.isEmpty()
				? bookRepository.findAll().stream().filter(b -> genre.equals(b.getGenre())).count()
				: bookRepository.count());

		// Total borrowings
		response.setTotalBorrowings(allBorrowings.size());

		// Active users — distinct users with at least one borrowing
		long activeUsers = allBorrowings.stream().map(b -> b.getUser().getId()).distinct().count();
		response.setActiveUsers(activeUsers);

		// Most borrowed books — top 5
		Map<String, Long> bookCounts = allBorrowings.stream()
				.collect(Collectors.groupingBy(b -> b.getBook().getTitle(), Collectors.counting()));

		List<Map<String, Object>> mostBorrowed = bookCounts.entrySet().stream()
				.sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(5).map(e -> {
					Map<String, Object> m = new LinkedHashMap<>();
					m.put("title", e.getKey());
					m.put("count", e.getValue());
					return m;
				}).collect(Collectors.toList());
		response.setMostBorrowedBooks(mostBorrowed);

		// Borrowings by genre
		Map<String, Long> genreCounts = allBorrowings.stream()
				.collect(Collectors.groupingBy(b -> b.getBook().getGenre(), Collectors.counting()));

		List<Map<String, Object>> byGenre = genreCounts.entrySet().stream()
				.sorted(Map.Entry.<String, Long>comparingByValue().reversed()).map(e -> {
					Map<String, Object> m = new LinkedHashMap<>();
					m.put("genre", e.getKey());
					m.put("count", e.getValue());
					return m;
				}).collect(Collectors.toList());
		response.setBorrowingsByGenre(byGenre);

		// Borrowings trend — by month (last 6 months)
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
		Map<String, Long> trendCounts = allBorrowings.stream()
				.collect(Collectors.groupingBy(b -> b.getBorrowedDate().format(formatter), Collectors.counting()));

		List<Map<String, Object>> trend = trendCounts.entrySet().stream().map(e -> {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("month", e.getKey());
			m.put("count", e.getValue());
			return m;
		}).collect(Collectors.toList());
		response.setBorrowingsTrend(trend);

		return response;
	}
}