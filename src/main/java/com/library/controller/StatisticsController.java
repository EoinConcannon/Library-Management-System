package com.library.controller;

import com.library.dto.StatisticsResponse;
import com.library.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

	private final StatisticsService statisticsService;

	@GetMapping
	@PreAuthorize("hasRole('LIBRARIAN')")
	public ResponseEntity<StatisticsResponse> getStatistics(@RequestParam(required = false) String genre) {
		return ResponseEntity.ok(statisticsService.getStatistics(genre));
	}
}