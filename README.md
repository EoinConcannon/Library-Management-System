# Library Management System

A full-stack web application built with **Spring Boot**, **jQuery/Bootstrap**, and **MySQL** for managing a library's books, users, and borrowing operations.

## Features

- **User Management** — Librarians can create student/librarian accounts and manage roles
- **Book Catalogue** — Browse, search, and filter books by title, author, genre, and availability
- **Book Management** — Librarians can add, edit, and delete books (CRUD)
- **Borrow & Return** — Authenticated users can borrow available books and return them
- **Dashboard & Statistics** — Real-time charts showing genre distribution and borrowing overview
- **JWT Authentication** — Secure stateless authentication with role-based access control
- **DataTables** — Sortable, searchable, paginated tables for books and users
- **Swagger API Docs** — Interactive API documentation via OpenAPI/Swagger UI

## Tech Stack

| Layer      | Technology                                        |
|------------|---------------------------------------------------|
| Backend    | Spring Boot 4.x, Spring Security, Spring Data JPA |
| Frontend   | HTML, CSS, JavaScript, jQuery 3.7, Bootstrap 5.3  |
| Database   | MySQL 8.x                                         |
| Auth       | JWT (JSON Web Tokens) via JJWT                     |
| UI Plugins | DataTables 1.13, Chart.js 4.4, Bootstrap Icons     |
| Docs       | Springdoc OpenAPI (Swagger UI)                     |
| Build      | Maven, Jenkins (CI/CD)                             |

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.x

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/EoinConcannon/Library-Management-System.git
cd Library-Management-System
```

### 2. Configure MySQL Database

Create the database:

```sql
CREATE DATABASE library_db;
```

Update `src/main/resources/application.properties` if your MySQL credentials differ:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/library_db
spring.datasource.username=root
spring.datasource.password=root
```

### 3. Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

The application starts at **http://localhost:8080**

### 4. Default Accounts

On first startup, two test accounts are created automatically:

| Role      | Email                  | Password |
|-----------|------------------------|----------|
| Librarian | test@librarian.test    | test     |
| Student   | test@student.test      | test     |

## API Endpoints

### Authentication

| Method | Endpoint          | Description             | Auth     |
|--------|-------------------|-------------------------|----------|
| POST   | `/api/auth/login` | Login and get JWT token | Public   |
| GET    | `/api/auth/me`    | Get current user info   | Bearer   |

### Books

| Method | Endpoint          | Description       | Auth             |
|--------|-------------------|-------------------|------------------|
| GET    | `/api/books`      | List all books    | Public           |
| GET    | `/api/books/{id}` | Get book by ID    | Public           |
| POST   | `/api/books`      | Add a new book    | LIBRARIAN        |
| PUT    | `/api/books/{id}` | Update a book     | LIBRARIAN        |
| DELETE | `/api/books/{id}` | Delete a book     | LIBRARIAN        |

### Users

| Method | Endpoint                  | Description       | Auth      |
|--------|---------------------------|-------------------|-----------|
| GET    | `/api/users`              | List all users    | LIBRARIAN |
| GET    | `/api/users/{id}`         | Get user by ID    | LIBRARIAN |
| POST   | `/api/users`              | Create new user   | LIBRARIAN |
| PATCH  | `/api/users/{id}/role`    | Update user role  | LIBRARIAN |

### Loans

| Method | Endpoint                    | Description              | Auth          |
|--------|-----------------------------|--------------------------|---------------|
| POST   | `/api/loans/borrow`         | Borrow a book            | Authenticated |
| POST   | `/api/loans/{id}/return`    | Return a borrowed book   | Authenticated |
| GET    | `/api/loans/my`             | Get my loans             | Authenticated |
| GET    | `/api/loans`                | Get all loans            | LIBRARIAN     |
| GET    | `/api/loans/stats`          | Get library statistics   | Public        |

### Swagger UI

Interactive API documentation is available at:

```
http://localhost:8080/swagger-ui.html
```

## Entity Relationships

```
User (1) ──── (N) Loan (N) ──── (1) Book
```

- **User** has many **Loans** (One-to-Many)
- **Book** has many **Loans** (One-to-Many)
- **Loan** belongs to one **User** and one **Book** (Many-to-One)

## Project Structure

```
src/main/java/com/library/
├── config/          # Security, JWT, data initialisation
├── controller/      # REST API controllers
├── dto/             # Data Transfer Objects
├── entity/          # JPA entities (User, Book, Loan)
├── enums/           # Role enum
├── exception/       # Custom exceptions & global handler
├── repository/      # Spring Data JPA repositories
└── service/         # Business logic (interfaces + implementations)

src/main/resources/
├── static/          # Frontend HTML pages
└── application.properties

src/test/java/com/library/
└── service/         # Unit tests for service layer
```

## Running Tests

```bash
mvn test
```

## Frontend Pages

| Page                  | URL                     | Access    |
|-----------------------|-------------------------|-----------|
| Home / Dashboard      | `/index.html`           | Public    |
| Login                 | `/login.html`           | Public    |
| Book Catalogue        | `/catalogue.html`       | Public    |
| My Books              | `/my-books.html`        | Auth      |
| Book Management       | `/book-management.html` | LIBRARIAN |
| User Management       | `/create-account.html`  | LIBRARIAN |
