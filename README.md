# Blogging Platform

A desktop blogging platform built with JavaFX and MySQL, featuring user authentication, post management, commenting system, and full-text search capabilities.

## Features

- **User Management**: Registration and authentication with role-based access (Admin/Regular)
- **Post Management**: Create, edit, delete, and publish blog posts with draft/published status
- **Comment System**: Add, edit, and delete comments on posts
- **Search Functionality**: Full-text search across post titles and author names
- **Session Management**: Secure session handling for authenticated users
- **Caching**: Post caching for improved performance
- **Custom Exception Handling**: Comprehensive exception hierarchy for error management

## Prerequisites

- **Java Development Kit (JDK) 25** or higher
- **Maven 3.6+** for dependency management
- **MySQL 8.0+** database server
- **JavaFX 25** (included via Maven dependencies)

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
- **JavaFX 25**: Desktop UI framework
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
3. **Post Home**: After login, users see published posts with search functionality
4. **Post Management**: 
   - Admins can access "My Posts" to create, edit, and delete posts
   - Posts can be saved as Draft or Published
5. **Post Viewing**: Click on any post to view full content and comments
6. **Commenting**: Authenticated users can add, edit, and delete comments

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
│   │   │   ├── Config.java            # Configuration management
│   │   │   ├── MySQLDriver.java       # Database operations
│   │   │   ├── BaseController.java    # Base controller for all views
│   │   │   ├── classes/               # Data models and utilities
│   │   │   │   ├── User.java
│   │   │   │   ├── SessionManager.java
│   │   │   │   ├── CacheManager.java
│   │   │   │   └── ...
│   │   │   ├── exceptions/            # Custom exception hierarchy
│   │   │   │   ├── BloggingPlatformException.java
│   │   │   │   ├── DatabaseException.java
│   │   │   │   └── ...
│   │   │   └── [Controller classes]    # UI controllers
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

## Search Performance Optimization

The application implements MySQL FULLTEXT search for improved performance:

### Before Optimization
- Used `LOWER(title) LIKE '%term%'` queries
- Full table scans, no index usage
- Linear performance degradation with data growth

### After Optimization
- FULLTEXT indexes on `posts.title` and `users.name`
- Uses `MATCH() AGAINST()` in NATURAL LANGUAGE MODE
- Relevance-based sorting
- Expected 10-100x faster on large datasets (1000+ posts)

See [`docs/performance_review.sql`](docs/performance_review.sql) for optimization details.

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
- For Java 25, ensure `--enable-native-access=javafx.graphics` VM option is set
- Check that JavaFX modules are accessible in module path

### Build Issues

- Clean and rebuild: `mvn clean install`
- Check Java version: `java -version` (should be 25+)
- Verify Maven installation: `mvn -version`