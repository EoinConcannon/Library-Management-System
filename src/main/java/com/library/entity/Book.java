package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
public class Book {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String author;

	@Column(nullable = false, unique = true)
	private String isbn;

	@Column(nullable = false)
	private String genre;

	@Column(nullable = false)
	private boolean available = true;
}