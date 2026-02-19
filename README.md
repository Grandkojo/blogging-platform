# Blogging Platform

A Spring Boot blogging backend with REST and GraphQL APIs, backed by MySQL and Spring Data JPA.  
It provides user authentication, post management with tags, reviews and comments, transactional write operations, JPQL-based search with pagination/sorting, and application-level caching using Spring Cache.

## Features

- **User Management**: Registration and authentication with JWT-based login and role-based access (Admin/Regular)
- **Post Management**: Create, edit, delete, and publish blog posts with draft/published status
- **Tag System**: Categorize posts with tags; create tags (Admin only); link tags to posts
- **Review System**: Rate posts (1–5 stars) with messages; view average ratings; edit/delete reviews (author or admin post-owner)
- **Comment System**: Add, edit, and delete comments on posts (author-only edit/delete)
- **Advanced Search & Sort**:
  - JPQL query (`PostRepository.searchByTitleAuthorOrTag`) searches by title, author name, or tag name
  - Pagination and sorting implemented via Spring Data `Pageable` and `Sort` on both REST and GraphQL endpoints
- **Caching (Spring Cache)**:
  - Frequently accessed posts (single post by id, all posts, paged lists)
  - Users list
  - Tag lists and tags-by-post
  - Cache entries invalidated automatically on create/update/delete operations
- **Transaction Management**: Service write operations use `@Transactional` with rollback rules for domain-specific exceptions
- **Custom Exception Handling**: Central REST and GraphQL exception handlers with domain-specific exception types
- **Repository/Service Architecture**: Spring Data JPA repositories with a thin service layer encapsulating business logic

## Prerequisites

- **Java Development Kit (JDK) 21** or higher
- **Maven 3.6+** for dependency management
- **MySQL 8.0+** database server

## Database Schema

The application uses a MySQL database with the following structure:

- **users**: Stores user accounts with name, email, role, and hashed password
- **posts**: Stores blog posts with title, content, status (DRAFT/PUBLISHED/DELETED), and publication date
- **comments**: Stores user comments on posts with metadata support
- **tags**: Stores available tags for categorizing posts
- **post_tags**: Junction table for many-to-many relationship between posts and tags
- **reviews**: Stores post reviews with rating (1–5), message, and timestamps (one review per user per post)

The complete database schema and setup scripts are available at:

- `docs/blogging_platform.sql` – MySQL schema with indexes and foreign keys

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd blogging_platform
```

### 2. Database Setup

1. **Start MySQL Server**
   ```bash
   # On Linux/Mac
   sudo systemctl start mysql
   # Or
   sudo service mysql start
   ```

2. **Create the Database**
   ```bash
   mysql -u root -p
   ```
   ```sql
   CREATE DATABASE blogging_platform;
   USE blogging_platform;
   ```

3. **Run the SQL Script**
   ```bash
   mysql -u root -p blogging_platform < docs/blogging_platform.sql
   ```

### 3. Configuration

#### Environment variables via `.env`

Sensitive configuration (database credentials, JWT secret, Google OAuth2) is externalized to a `.env` file. The application loads `.env` on startup via `EnvFileLoader` before Spring Boot reads `application.properties`, so placeholders like `${SPRING_DATASOURCE_USERNAME}` resolve correctly.

1. Copy the example file and fill in your values:
   ```bash
   cp .env.example .env
   ```
2. Edit `.env` with your MySQL credentials, JWT secret, and (optional) Google OAuth2 client ID/secret.
3. Never commit `.env` — it is listed in `.gitignore`.

Required variables for dev include: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`. For Google login, add `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET`.

#### Database (alternative: direct properties)

If you prefer not to use `.env`, database configuration can be set in `application.properties` (or profile-specific files like `application-dev.properties`):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/blogging_platform?allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
server.servlet.context-path=/api/v1
```

Update the database name and credentials to match your local MySQL setup.

### 4. Install Dependencies

Maven will automatically download all required dependencies when you build or run the project:

```bash
mvn clean install
```

## Dependencies

The project uses the following key dependencies (managed via Maven):

### Core Dependencies

- **Spring Boot**:
  - `spring-boot-starter-web` – REST API
  - `spring-boot-starter-graphql` – GraphQL endpoint on `/graphql`
  - `spring-boot-starter-data-jpa` – JPA/Hibernate data access
  - `spring-boot-starter-jdbc` – JDBC support where needed
  - `spring-boot-starter-validation` – Bean validation (Jakarta Validation)
  - `spring-boot-starter-aspectj` – cross-cutting logging and performance monitoring
  - `spring-boot-starter-cache` – Spring Cache abstraction (posts, users, tags)
  - `spring-boot-starter-security` – authentication/authorization infrastructure
  - `spring-boot-starter-oauth2-resource-server` – JWT validation for Bearer tokens
  - `spring-boot-starter-oauth2-client` – OAuth2 client support (for Google login in BEM‑07)
- **springdoc-openapi** – interactive REST docs at `/swagger-ui.html`
- **MySQL Connector/J** – MySQL database driver
- **BCrypt (Spring Security)** – Password hashing via `BCryptPasswordEncoder`

### Test Dependencies

- **JUnit Jupiter** – Unit testing framework
- **Mockito** – Mocking framework for tests

All dependencies are defined in `pom.xml` and will be automatically resolved by Maven.

## Execution Instructions

### Running the Application

#### Using Maven (Recommended)

```bash
mvn spring-boot:run
```

#### Using IDE

- Import the project as a Maven project.
- Run the `com.blogging_platform.Main` class (Spring Boot entry point with `@EnableCaching`).

## API Overview

- **REST APIs** under `server.servlet.context-path` (e.g. `/api/v1/posts`, `/api/v1/users`, `/api/v1/comments`, etc.).
- **GraphQL endpoint** at `/graphql` with schema defined in `src/main/resources/graphql/schema.graphqls`.
- Authentication and user management handled via REST controllers and `UserService`.
- Posts, comments, reviews, and tags are all backed by JPA entities and repositories.

### Security (BEM‑07 Spring Security)

- **JWT Authentication**:
  - `POST /api/v1/auth/register` – register a new user (name, email, password, role).
  - `POST /api/v1/auth/login` – authenticate and receive a signed JWT:
    - Claims include subject (email), issuer, issue/expiry times, `jti`, and `roles`.
  - `POST /api/v1/auth/logout` – revoke the current JWT (in‑memory blacklist by `jti` until expiry).
  - All protected endpoints (e.g. `GET /api/v1/posts`) require `Authorization: Bearer <token>`.
- **Google OAuth2 Login**:
  - `GET /api/v1/oauth2/authorization/google` – redirects to Google for sign-in. No prior registration required.
  - After successful authentication, user details (email, name) are fetched from Google and persisted in the `users` table. New users receive the default `Regular` role; existing users keep their stored role.
  - Requires `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` in `.env`. Configure the redirect URI `http://localhost:8080/api/v1/login/oauth2/code/google` (or your base URL + context path) in [Google Cloud Console](https://console.cloud.google.com/apis/credentials) for your OAuth2 client.
- **Password Hashing**:
  - All stored passwords are hashed using `BCryptPasswordEncoder` from Spring Security.
- **Error Handling**:
  - Spring Security authentication failures are mapped to `401 Unauthorized` with a consistent JSON payload via `GlobalExceptionHandler`.
- **Update/Delete error responses** (comments, posts, reviews):
  - **404 Not Found** – The resource (comment/post/review) with the given id does not exist, or (for post update/delete) the user id does not exist. Message examples: `"Comment with id '...' not found."`, `"Post with id '...' not found."`, `"User with id '...' not found."`, `"Review with id '...' not found."`
  - **403 Forbidden** – The authenticated user is not the author of the resource. Message examples: `"User is not the author of this comment."`, `"User is not the author of this post."`, `"User is not the author of this review."`
  - **400 Bad Request** – Validation failed (e.g. post id in body does not match the comment/review, or required fields missing). Message examples: `"Post does not match this comment."`, `"Comment id and user id are required."`
- **CORS (Cross-Origin Resource Sharing)**:
  - Global CORS is configured via `security.cors.*` properties. Allowed origins, methods, and headers are explicit; requests from other origins do not receive `Access-Control-Allow-Origin` and are blocked by the browser.
  - **Dev profile**: allows `http://localhost:3000`, `http://localhost:5173`, `http://127.0.0.1:3000`, `http://127.0.0.1:5173`, `http://localhost:8080`, `http://127.0.0.1:8080` (for React, Vite, Swagger, etc.).
  - **Prod**: set `security.cors.allowed-origins` (comma-separated) or `CORS_ALLOWED_ORIGINS`; if empty, cross-origin browser requests are blocked.
  - Allowed methods: GET, POST, PUT, PATCH, DELETE, OPTIONS. Allowed headers: Authorization, Content-Type, Accept. Test with Postman (no CORS restriction) and from a web frontend to verify allowed vs blocked origins.
- **CSRF (Cross-Site Request Forgery)**:
  - **Disabled for the main API** because authentication is stateless JWT in the `Authorization` header (no cookies). CSRF targets cookie-based sessions: a malicious site cannot send the JWT from the victim’s browser without the victim’s code, so the main API does not require CSRF tokens.
  - **When to enable CSRF**: enable it for stateful session authentication (e.g. cookie-based login) or for any form submissions that rely on session cookies. Then use a synchronizer token (e.g. Spring Security’s CSRF token) in forms or the `X-CSRF-TOKEN` header.
  - **Demo**: to see CSRF in action, the app exposes a small form-style demo under `/api/v1/demo/`: `GET /api/v1/demo/csrf-token` returns a CSRF token (and creates a session); `POST /api/v1/demo/csrf-submit` accepts the request only if the token is sent (header `X-CSRF-TOKEN` or parameter `_csrf`). See [Testing the CSRF demo](#testing-the-csrf-demo) below.

#### CORS vs CSRF

| | CORS | CSRF |
|---|------|------|
| **Purpose** | Controls which *origins* (e.g. `https://myapp.com`) can call your API from the browser. | Protects against a *site* (e.g. evil.com) making the *user’s browser* send a request to your site using the user’s cookies/session. |
| **Enforced by** | Browser (using response headers like `Access-Control-Allow-Origin`). | Server (validates a token that only your own pages know). |
| **Relevant when** | Any cross-origin browser request (e.g. React on port 3000 calling API on 8080). | State-changing requests that use cookies/session (e.g. form POST with `JSESSIONID`). |
| **Postman** | CORS does not apply (Postman is not a browser). | CSRF does not apply to our JWT API (no session). For the demo, use the same session cookie and `X-CSRF-TOKEN` header. |
| **Browser** | Requests from a disallowed origin are blocked by the browser before they reach the server. | For session-based flows, the server rejects requests that lack a valid CSRF token. |

**Practical tests**: (1) **CORS**: From a browser page on an allowed origin (e.g. `http://localhost:3000`), call `GET /api/v1/posts` with `Authorization: Bearer <token>` — request succeeds; from a disallowed origin or without the right headers, the browser blocks it. In Postman, CORS is not enforced. (2) **CSRF demo**: In Postman, `GET /api/v1/demo/csrf-token` (save session cookie and token), then `POST /api/v1/demo/csrf-submit` with header `X-CSRF-TOKEN: <token>` and the same cookie — request succeeds; without the token (or with wrong token), the server returns 403.

#### Testing the CSRF demo

1. **Get a CSRF token (and session)**  
   `GET http://localhost:8080/api/v1/demo/csrf-token`  
   - If using Postman: enable “Send cookies” and call the URL. Copy the `token` value from the JSON response.
2. **Submit with the token**  
   `POST http://localhost:8080/api/v1/demo/csrf-submit`  
   - Header: `X-CSRF-TOKEN: <paste the token>`  
   - Use the same session (in Postman, the cookie is sent automatically if you did step 1 in the same request collection/session).  
   - Body (optional): `{"message": "hello"}`  
   - You should get 200 and a success message.
3. **Without the token**  
   Repeat the POST without the `X-CSRF-TOKEN` header (or with a wrong value). The server responds with **403 Forbidden**.

Interactive REST API documentation (including these response codes per endpoint) is available at `/swagger-ui.html` when the application is running.

#### Testing JWT with Postman/Insomnia

1. `POST /api/v1/auth/register` with JSON body:
   ```json
   { "name": "Jane Doe", "email": "jane@example.com", "password": "password123", "role": "READER" }
   ```
2. `POST /api/v1/auth/login` with the same email/password to obtain the `accessToken`.
3. Call a protected endpoint such as:
   - `GET /api/v1/posts`
   - Header: `Authorization: Bearer <accessToken>`
4. Without the header (or with a tampered/expired token) the API responds with `401 Unauthorized`.

#### Testing Google OAuth2 login

1. Ensure `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` are set in `.env`.
2. In [Google Cloud Console](https://console.cloud.google.com/apis/credentials), add `http://localhost:8080/api/v1/login/oauth2/code/google` to your OAuth2 client's **Authorized redirect URIs**.
3. Open `http://localhost:8080/api/v1/oauth2/authorization/google` in a browser. Sign in with Google.
4. On success, you are redirected to Swagger UI, and the user is stored in the `users` table with role `Regular`.

## Testing

### Running Tests

Execute all tests using Maven:

```bash
mvn test
```

Tests are located in `src/test/java/com/blogging_platform/` and cover controllers, services, and exceptions using JUnit and Mockito.

## Project Structure

```text
blogging_platform/
├── docs/
│   ├── blogging_platform_erd.png      # Entity-Relationship Diagram
│   ├── blogging_platform.sql          # MySQL database schema
│   ├── performance_review.sql         # Performance optimization queries
│   ├── swagger_ui.png                 # REST API documentation (Swagger UI)
│   ├── graphql1.png                   # GraphQL schema / queries
│   ├── graphql2.png                   # GraphQL mutations
│   ├── custom_validation.png          # Bean validation & error handling
│   ├── aop_logging.png                # AOP logging and performance metrics
│   ├── coverage_overview_bem07.png    # Test coverage report (BEM‑07)
│   ├── csrf_get_token_insomnia.png    # CSRF demo: GET token (Insomnia)
│   └── csrf_submit_insomnia.png       # CSRF demo: POST with token (Insomnia)
├── src/
│   ├── main/
│   │   ├── java/com/blogging_platform/
│   │   │   ├── Main.java              # Spring Boot entry point (@EnableCaching)
│   │   │   ├── ApiResponse.java       # Standard API response wrapper
│   │   │   ├── aop/                   # Cross‑cutting concerns
│   │   │   │   └── ServiceLoggingAspect.java
│   │   │   ├── classes/               # Data records and utilities
│   │   │   │   ├── PagedResult.java
│   │   │   │   ├── PostRecord.java
│   │   │   │   ├── CommentRecord.java
│   │   │   │   ├── ReviewRecord.java
│   │   │   │   ├── TagRecord.java
│   │   │   │   └── UserRecord.java
│   │   │   ├── controller/            # REST + GraphQL controllers
│   │   │   │   ├── PostController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── CommentController.java
│   │   │   │   ├── ReviewController.java
│   │   │   │   └── TagController.java
│   │   │   ├── repository/            # Spring Data JPA repositories (BEM‑06)
│   │   │   │   ├── PostRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── CommentRepository.java
│   │   │   │   ├── TagRepository.java
│   │   │   │   └── ReviewRepository.java
│   │   │   ├── service/               # Business logic layer
│   │   │   │   ├── PostService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── CommentService.java
│   │   │   │   ├── TagService.java
│   │   │   │   └── ReviewService.java
│   │   │   ├── model/                 # Domain models (JPA entities)
│   │   │   │   ├── Post.java
│   │   │   │   ├── User.java
│   │   │   │   ├── Comment.java
│   │   │   │   ├── Review.java
│   │   │   │   └── Tag.java
│   │   │   └── exceptions/            # Custom exception hierarchy
│   │   │       ├── BloggingPlatformException.java
│   │   │       ├── DatabaseException.java
│   │   │       ├── GlobalExceptionHandler.java      # REST error handling
│   │   │       └── GraphQLExceptionHandler.java     # GraphQL error handling
│   │   └── resources/
│   │       ├── application.properties             # Default Spring Boot config
│   │       ├── application-dev.properties         # Dev profile config
│   │       ├── application-test.properties        # Test profile config
│   │       ├── application-prod.properties        # Prod profile config
│   │       └── graphql/schema.graphqls            # GraphQL schema (queries + mutations)
│   └── test/
│       └── java/com/blogging_platform/            # Unit and integration tests
├── pom.xml                            # Maven configuration
├── README.md                          # This file
├── .env                               # Environment variables (copy from .env.example)
└── .env.example                       # Template for required env vars
```

## Architecture & Performance

### Cross‑Cutting Concerns (AOP)

The service layer is instrumented with Spring AOP to provide centralized logging and performance monitoring (Epic **BEM‑05 AOP Logging & Monitoring**):

- A `ServiceLoggingAspect` applies `@Before`, `@AfterReturning`, and `@Around` advice to all public methods in `com.blogging_platform.service.*`.
- The `@Around` advice measures execution time and logs slow calls as warnings; all calls log their duration in milliseconds.

### Persistence, Search & Caching

- **Repositories**: All entities (`Post`, `User`, `Comment`, `Review`, `Tag`) use Spring Data JPA repositories for CRUD and derived queries.
- **Search**:
  - `PostRepository` exposes a JPQL query (`searchByTitleAuthorOrTag`) that joins `user` and `tags` to search by title, author name, or tag.
  - `PostService.getPosts(query, Pageable)` uses this query and returns `PostRecord` DTOs.
  - Pagination and sorting are handled via Spring Data’s `Pageable` and `Sort`.
- **Transactions**:
  - Write methods in services (`PostService`, `CommentService`, `ReviewService`, `TagService`) are annotated with `@Transactional` and class-level rollback rules for domain exceptions to ensure atomic create/update/delete operations.
- **Spring Cache**:
  - `PostService` caches:
    - Single posts by id (`postsById`)
    - All posts and paged lists (`posts`)
  - `TagService` caches:
    - All tags (`tags`)
    - Tags by post (`tagsByPost`)
  - `UserService` caches:
    - The user list (`users`)
  - Cache entries are automatically evicted on relevant create/update/delete operations.

## Recent Updates

### Latest Changes (2026)

- **Update/Delete error clarity (comments, posts, reviews)**:
  - Comment, post, and review update/delete now return distinct HTTP status and messages: **404** when the resource or user is not found, **403** when the user is not the author, **400** when the body is invalid (e.g. post id does not match). OpenAPI annotations and this README document these responses.
- **BEM‑05 Web & GraphQL**: Spring Boot REST controllers and GraphQL mappings for posts, users, comments, reviews, and tags.
- **BEM‑05 Validation & Exceptions**: Centralized validation and error handling via `GlobalExceptionHandler` and `GraphQLExceptionHandler`, with bean validation constraints.
- **BEM‑05 AOP Logging & Monitoring**: `ServiceLoggingAspect` for request tracing and performance metrics on service methods.
- **BEM‑06 Spring Data JPA Integration**:
  - Introduced Spring Data JPA repositories for User, Post, Comment, Tag, and Review.
  - Updated entities to use UUID primary keys with proper column mappings.
  - Controllers and services now delegate all persistence to repositories.
- **BEM‑06 Pagination, Sorting, and JPQL Queries**:
  - Implemented pageable `/posts` endpoint (and GraphQL `getPosts`) using `Pageable` and `Sort`.
  - Added JPQL query to search posts by title, author name, or tag name.
- **BEM‑06 Transactions & Rollback**:
  - Service classes use `@Transactional` with rollback rules for domain-specific exceptions.
- **BEM‑06 Caching & Performance**:
  - Replaced the custom in‑memory `CacheManager` with Spring Cache.
  - Added caches for popular posts, users, and tags, with automatic eviction on writes.

## Documentation

All classes, methods, and interfaces are documented with Javadoc comments following Java best practices. Generate documentation using:

```bash
mvn javadoc:javadoc
```

The generated documentation will be available in `target/site/apidocs/`.

## License

[Add your license information here]

