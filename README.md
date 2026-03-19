# LibraryMS — Library Management System

A full-stack web application for managing library books, users, borrowing, reservations and statistics.

Built with **Spring Boot**, **MySQL** and **Bootstrap/jQuery** frontend.

---

## Prerequisites

Make sure the following are installed before running the project:

- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Git
- Google Chrome (for E2E tests)

---

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/EoinConcannon/Library-Management-System.git
cd Library-Management-System
```

### 2. Create the MySQL Database

Open MySQL and run:

```sql
CREATE DATABASE library_db;
```

### 3. Configure Application Properties

Create the file `src/main/resources/application-local.properties` with your local settings:

```properties
jwt.secret=your-jwt-secret-key-must-be-at-least-32-characters-long
jwt.expiration=86400000
```

The main `application.properties` is already configured to connect to MySQL on `localhost:3306` with username `root` and password `root`. Update these values in `application.properties` if your MySQL credentials differ:

```properties
spring.datasource.username=root
spring.datasource.password=root
```

### 4. Build the Project

```bash
mvn clean package -DskipTests
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

Hibernate will automatically create all required database tables on first run.

---

## Default Test Accounts

Two accounts are seeded automatically on startup:

| Role      | Email                    | Password |
|-----------|--------------------------|----------|
| Librarian | test@librarian.test      | test     |
| Student   | test@student.test        | test     |

---

## Running Tests

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn failsafe:integration-test failsafe:verify
```

Integration tests use an H2 in-memory database — no MySQL setup required.

### All Tests with Coverage Report

```bash
mvn verify
```

Coverage report generated at: `target/site/jacoco/index.html`

### End to End Tests

Requires the application to be running on port 8080 first, then:

```bash
mvn test -Dtest=CucumberRunnerE2E
```

---

## CI/CD Pipeline (Jenkins)

The project includes a `Jenkinsfile` in the root directory. The pipeline consists of the following stages:

1. **Checkout** — Clones the repository
2. **Build** — Compiles and packages the application
3. **Unit Tests** — Runs JUnit/Mockito unit tests
4. **Integration Tests** — Runs REST Assured integration tests
5. **End to End Tests** — Runs Selenium/Cucumber E2E tests
6. **Code Coverage** — Generates JaCoCo coverage report
7. **SonarQube Analysis** — Runs static code analysis

Jenkins must have the following credentials configured:
- `github-credentials` — GitHub username and Personal Access Token
- `sonarqube-token` — SonarQube authentication token

---

## Project Structure

```
src/
├── main/
│   ├── java/com/library/
│   │   ├── config/          # Security, JWT, DataInitializer
│   │   ├── controller/      # REST API controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── enums/           # Role enum
│   │   ├── exception/       # Global exception handler
│   │   ├── repository/      # Spring Data JPA repositories
│   │   └── service/         # Business logic
│   └── resources/
│       └── static/          # Frontend HTML, CSS, JS
└── test/
    ├── java/com/library/
    │   ├── e2e/             # Selenium/Cucumber E2E tests
    │   ├── integration/     # REST Assured integration tests
    │   └── service/         # JUnit/Mockito unit tests
    └── resources/
        └── features/        # Cucumber feature files
```

---

## API Documentation

Key API endpoints:

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/login` | Public | Login and receive JWT token |
| GET | `/api/books` | Public | Get all books |
| POST | `/api/books` | Librarian | Add a new book |
| PUT | `/api/books/{id}` | Librarian | Update a book |
| DELETE | `/api/books/{id}` | Librarian | Delete a book |
| POST | `/api/users` | Librarian | Create a user account |
| POST | `/api/borrowings/{bookId}` | Authenticated | Borrow a book |
| PATCH | `/api/borrowings/{id}/return` | Authenticated | Return a book |
| POST | `/api/reservations/{bookId}` | Authenticated | Reserve a book |
| GET | `/api/statistics` | Librarian | Get library statistics |

---

## Built With

- [Spring Boot 4.0](https://spring.io/projects/spring-boot)
- [Spring Security + JWT](https://github.com/jwtk/jjwt)
- [Spring Data JPA + Hibernate](https://spring.io/projects/spring-data-jpa)
- [MySQL](https://www.mysql.com/)
- [Bootstrap 5](https://getbootstrap.com/)
- [Chart.js](https://www.chartjs.org/)
- [DataTables](https://datatables.net/)
- [JaCoCo](https://www.jacoco.org/jacoco/)
- [SonarQube](https://www.sonarqube.org/)
- [Selenium + Cucumber](https://www.selenium.dev/)
- [REST Assured](https://rest-assured.io/)