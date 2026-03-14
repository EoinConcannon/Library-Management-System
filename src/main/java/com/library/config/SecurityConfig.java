package com.library.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtFilter jwtFilter;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				// No sessions — JWT is stateless
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						// All static pages are public — JS handles role redirects
						.requestMatchers("/", "/index.html", "/catalogue.html", "/login.html", "/create-account.html",
								"/book-management.html", "/css/**", "/js/**")
						.permitAll()
						// Public API endpoints
						.requestMatchers("/api/auth/login", "/api/auth/me").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/books", "/api/books/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/borrowings/**").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/borrowings/my").authenticated()
						// Protected API endpoints only
						.requestMatchers("/api/users/**").hasRole("LIBRARIAN")
						.requestMatchers(HttpMethod.POST, "/api/books/**").hasRole("LIBRARIAN")
						.requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("LIBRARIAN")
						.requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("LIBRARIAN").anyRequest()
						.authenticated())
				// Register JWT filter before Spring's auth filter
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}