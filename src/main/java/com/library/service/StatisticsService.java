package com.library.service;

import com.library.dto.StatisticsResponse;

public interface StatisticsService {
	StatisticsResponse getStatistics(String genre);
}