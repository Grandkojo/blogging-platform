# Blogging Platform

A desktop blogging platform built with JavaFX and MySQL, featuring user authentication, post management with tags, review system (ratings), commenting, and advanced in-memory search/sort capabilities using data structures and algorithms (hashing, caching, QuickSort).

## Features

- **User Management**: Registration and authentication with role-based access (Admin/Regular)
- **Post Management**: Create, edit, delete, and publish blog posts with draft/published status
- **Tag System**: Categorize posts with tags; create tags (Admin only); link tags to posts
- **Review System**: Rate posts (1-5 stars) with messages; view average ratings; edit/delete reviews (author or admin post-owner)
- **Comment System**: Add, edit, and delete comments on posts (author-only edit/delete)
- **Advanced Search & Sort**: 
  - In-memory search by title, author, or tag (cache-based, no DB queries)
  - QuickSort-based sorting by date, title, or author (ascending/descending)
  - Search and sort on home page and admin post list
- **Caching**: In-memory cache with hash index (O(1) lookup) and tag index for fast search
- **Session Management**: Secure session handling for authenticated users
- **Custom Exception Handling**: Comprehensive exception hierarchy for error management
- **DAO/Service Architecture**: Clean separation with DAO interfaces, JDBC implementations, and service layer

## Prerequisites

- **Java Development Kit (JDK) 21** or higher
- **Maven 3.6+** for dependency management
- **MySQL 8.0+** database server
- **JavaFX 21** (included via Maven dependencies)

## Database Schema

The application uses a MySQL database with the following structure:

### Entity-Relationship Diagram
See the complete ERD diagram at: [`docs/blogging_platform_erd.png`](docs/blogging_platform_erd.png)

### Database Tables

- **users**: Stores user accounts with name, email, role, and hashed password
- **posts**: Stores blog posts with title, content, status (DRAFT/PUBLISHED/DELETED), and publication date
- **comments**: Stores user comments on posts with metadata support
- **tags**: Stores available tags for categorizing posts
- **post_tags**: Junction table for many-to-many relationship between posts and tags
- **reviews**: Stores post reviews with rating (1-5), message, and timestamps (one review per user per post)

### SQL Scripts

The complete database schema and setup scripts are available at:
- [`docs/blogging_platform.sql`](docs/blogging_platform.sql) - MySQL schema with indexes and foreign keys

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
   
   # On Windows, start MySQL service from Services panel
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
   Or execute the SQL file contents directly in your MySQL client.

### 3. Configuration

Create a `.env` file in the project root directory with the following variables:

```env
DB_NAME=blogging_platform
USERNAME=your_mysql_username
PASSWORD=your_mysql_password
```

**Example:**
```env
DB_NAME=blogging_platform
USERNAME=root
PASSWORD=mypassword123
```

**Note**: The application will first check for a `.env` file, then fall back to system environment variables if the file is not found.

### 4. Install Dependencies

Maven will automatically download all required dependencies when you build or run the project:

```bash
mvn clean install
```

## Dependencies

The project uses the following key dependencies (managed via Maven):

### Core Dependencies
- **JavaFX 21**: Desktop UI framework
  - `javafx-base`, `javafx-controls`, `javafx-fxml`, `javafx-graphics`, `javafx-media`, `javafx-web`
- **MySQL Connector/J 9.0.0**: MySQL database driver
- **jBCrypt 0.4**: Password hashing library

### Test Dependencies
- **JUnit Jupiter 5.10.0**: Unit testing framework
- **Mockito 5.7.0**: Mocking framework for tests
- **TestFX 4.0.18**: JavaFX testing utilities

All dependencies are defined in [`pom.xml`](pom.xml) and will be automatically resolved by Maven.

## Execution Instructions

### Running the Application

#### Option 1: Using Maven (Recommended)

```bash
mvn javafx:run
```

#### Option 2: Using Java directly

1. **Compile the project:**
   ```bash
   mvn clean compile
   ```

2. **Run the application:**
   ```bash
   java --module-path <path-to-javafx-libs> --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -cp target/classes com.blogging_platform.App
   ```

#### Option 3: Using IDE

1. **IntelliJ IDEA / Eclipse:**
   - Import the project as a Maven project
   - Set the main class to `com.blogging_platform.App`
   - Add VM options: `--enable-native-access=javafx.graphics`
   - Run the `App` class

2. **VS Code:**
   - Use the Java Extension Pack
   - Open the project folder
   - Run the "Launch Blogging Platform" configuration from `.vscode/launch.json`

### Application Flow

1. **Login Screen**: The application starts at the login screen
2. **Registration**: New users can register with name, email, role (Admin/Regular), and password
3. **Post Home**: After login, users see published posts with:
   - Search by title, author, or tag (in-memory, cache-based)
   - Sort by date (newest/oldest), title (A-Z/Z-A), or author (A-Z/Z-A)
   - Post cards showing tags, comment count, and average rating
4. **Post Management**: 
   - Admins can access "My Posts" to create, edit, and delete posts
   - Posts can be saved as Draft or Published
   - Tags can be assigned when creating or editing posts
   - Admin can create new tags
5. **Post Viewing**: Click on any post to view:
   - Full content, tags, and average rating
   - Comments (add, edit, delete - author only)
   - Click rating to view all reviews
6. **Review Page**: View and manage reviews for a post:
   - See all reviews with author, rating, and message
   - Add a review (one per user per post)
   - Edit/delete your own review
   - Admin post-owner can delete any review on their post
7. **Commenting**: Authenticated users can add, edit (author only), and delete (author only) comments

## Testing

### Running Tests

Execute all tests using Maven:

```bash
mvn test
```

### Test Structure

Tests are located in `src/test/java/com/blogging_platform/`:

- **Controller Tests**: Validation and business logic tests for each controller
  - `LoginUserControllerTest.java`
  - `RegisterUserControllerTest.java`
  - `AddPostControllerTest.java`
  - `EditPostControllerTest.java`
  - `PostListControllerTest.java`
  - `SinglePostControllerTest.java`
  - `PostHomeControllerTest.java`

- **Exception Tests**: Tests for custom exception hierarchy
  - Located in `src/test/java/com/blogging_platform/exceptions/`

### Test Requirements

- JavaFX Platform must be initialized for controller tests
- Tests use reflection to initialize FXML fields
- Database connection is not required for unit tests (they test validation and business logic)

## Project Structure

```
blogging_platform/
├── docs/
│   ├── blogging_platform_erd.png      # Entity-Relationship Diagram
│   ├── blogging_platform.sql          # MySQL database schema
│   └── performance_review.sql         # Performance optimization queries
├── src/
│   ├── main/
│   │   ├── java/com/blogging_platform/
│   │   │   ├── App.java               # Main application entry point
│   │   │   ├── Config.java            # Configuration management (.env loader)
│   │   │   ├── DBConnection.java      # JDBC connection management
│   │   │   ├── MySQLDriver.java       # Legacy database operations
│   │   │   ├── BaseController.java    # Base controller for all views
│   │   │   ├── classes/               # Data models and utilities
│   │   │   │   ├── CacheManager.java  # In-memory cache with search/sort
│   │   │   │   ├── SessionManager.java # User session management
│   │   │   │   ├── PostRecord.java    # DTOs for data transfer
│   │   │   │   ├── CommentRecord.java
│   │   │   │   ├── ReviewRecord.java
│   │   │   │   ├── TagRecord.java
│   │   │   │   └── ...
│   │   │   ├── dao/                   # Data Access Layer
│   │   │   │   ├── interfaces/        # DAO interfaces
│   │   │   │   │   ├── PostDAO.java
│   │   │   │   │   ├── UserDAO.java
│   │   │   │   │   ├── CommentDAO.java
│   │   │   │   │   ├── TagDAO.java
│   │   │   │   │   └── ReviewDAO.java
│   │   │   │   └── implementation/   # JDBC implementations
│   │   │   │       ├── JdbcPostDAO.java
│   │   │   │       ├── JdbcUserDAO.java
│   │   │   │       ├── JdbcCommentDAO.java
│   │   │   │       ├── JdbcTagDAO.java
│   │   │   │       └── JdbcReviewDAO.java
│   │   │   ├── service/               # Business logic layer
│   │   │   │   ├── PostService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── CommentService.java
│   │   │   │   ├── TagService.java
│   │   │   │   └── ReviewService.java
│   │   │   ├── model/                 # Domain models
│   │   │   │   ├── Post.java
│   │   │   │   ├── User.java
│   │   │   │   ├── Comment.java
│   │   │   │   ├── Review.java
│   │   │   │   └── Tag.java
│   │   │   ├── exceptions/            # Custom exception hierarchy
│   │   │   │   ├── BloggingPlatformException.java
│   │   │   │   ├── DatabaseException.java
│   │   │   │   └── ...
│   │   │   └── [Controller classes]  # UI controllers (FXML)
│   │   └── resources/com/blogging_platform/
│   │       └── *.fxml                 # JavaFX UI layouts
│   └── test/
│       └── java/com/blogging_platform/
│           ├── [Controller tests]
│           └── exceptions/
│               └── [Exception tests]
├── pom.xml                            # Maven configuration
├── README.md                          # This file
└── .env                               # Environment configuration (create this)
```

## Architecture & Performance

### Data Structures & Algorithms Integration

The application implements several D&S concepts:

- **Hashing/Caching**: 
  - `CacheManager` uses `ConcurrentHashMap` for O(1) post lookup by id (hash index)
  - In-memory cache stores published posts and tag associations
  - Cache invalidation after comment/review create/update/delete

- **Sorting**: 
  - QuickSort algorithm implemented in `CacheManager.sortPosts()` for O(n log n) average performance
  - Supports sorting by date, title, or author (ascending/descending)

- **Searching**: 
  - In-memory linear search on cached posts (filters by title, author, or tag)
  - No database queries for search - all filtering happens in memory
  - Tag index (`postIdToTagNames`) enables fast tag-based search

- **Indexing Concept**: 
  - Hash index (`postByIdCache`) analogous to database primary key index
  - Tag index (`postIdToTagNames`) for efficient tag lookups
  - In-memory structures mirror database indexing principles

### Search & Sort Implementation

- **Home Page & Post List**: Search and sort run entirely in memory using cached data
- **No Database Queries**: Search filters cached posts; sort uses QuickSort on filtered results
- **Performance**: O(n) search + O(n log n) sort = efficient for typical post counts

### Legacy MySQL FULLTEXT Search

The application previously used MySQL FULLTEXT search (still available in `MySQLDriver`):

- FULLTEXT indexes on `posts.title` and `users.name`
- Uses `MATCH() AGAINST()` in NATURAL LANGUAGE MODE
- Relevance-based sorting
- Expected 10-100x faster on large datasets (1000+ posts)

See [`docs/performance_review.sql`](docs/performance_review.sql) for optimization details.

**Note**: Current implementation prefers cache-based in-memory search/sort for better responsiveness.

## Recent Updates

### Latest Changes (2026)

- **Documentation**: Comprehensive Javadocs added to all classes, methods, and interfaces
- **Code Cleanup**: Removed commented-out code and debug statements
- **Cache-Based Search & Sort**: In-memory search by title/author/tag with QuickSort (D&S integration)
- **Tags & Reviews**: Full tag management and review system (ratings 1-5) with authorization
- **Cache Invalidation**: Automatic cache refresh after comment/review create/update/delete
- **Edit Post Tags**: Tag field added to edit post screen
- **Architecture**: Migrated to DAO/Service layer pattern for better separation of concerns

See git log for detailed commit history.

## Troubleshooting

### Database Connection Issues

1. **Check MySQL is running:**
   ```bash
   sudo systemctl status mysql
   ```

2. **Verify database exists:**
   ```sql
   SHOW DATABASES;
   ```

3. **Check credentials in `.env` file:**
   - Ensure `DB_NAME`, `USERNAME`, and `PASSWORD` are correct
   - Remove any quotes around values

### JavaFX Runtime Issues

- Ensure JavaFX dependencies are properly downloaded: `mvn dependency:resolve`
- For Java 21+, ensure `--enable-native-access=javafx.graphics` VM option is set
- Check that JavaFX modules are accessible in module path

### Build Issues

- Clean and rebuild: `mvn clean install`
- Check Java version: `java -version` (should be 21+)
- Verify Maven installation: `mvn -version`

## Documentation

All classes, methods, and interfaces are fully documented with Javadoc comments following Java best practices. Generate documentation using:

```bash
mvn javadoc:javadoc
```

The generated documentation will be available in `target/site/apidocs/`.

## License

[Add your license information here]