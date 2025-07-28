# AssetCare360 Backend

This is the backend server for the AssetCare360 application.

## Getting Started

### Prerequisites
- Java JDK 8 or higher
- Maven 3.6 or higher
- MySQL database

### Building the Project
Run the following command in the project root directory to compile the project:
```bash
mvn clean compile
```

### Running the Application
You can start the application with:
```bash
mvn exec:java
```

Alternatively, compile and run in one step:
```bash
mvn compile exec:java
```

### Running Tests
Execute the tests using:
```bash
mvn test
```

### Creating a JAR File
Package the application into a JAR file by running:
```bash
mvn package
```

### Running the JAR File
After packaging, run the application with:
```bash
java -jar target/assetcare360-backend-1.0-SNAPSHOT.jar
```

## Database Management

### Migrations

Migrations are used to manage database schema changes. Migration files are stored in `src/main/resources/migrations/` with the naming convention `migration_<table_name>_v<version>.sql`.

To run all pending migrations:

```bash
java -cp target/assetcare360-backend-1.0-SNAPSHOT.jar com.assetcare360.system.DatabaseManager migrate
```

### Seeders

Seeders are used to populate the database with initial data. Seeder files are stored in `src/main/resources/seeders/` with the naming convention `<table_name>_seeder.sql`.

To run all seeders:

```bash
java -cp target/assetcare360-backend-1.0-SNAPSHOT.jar com.assetcare360.system.DatabaseManager seed
```

### Factories

Factories are used to generate test data for models. They can be used in tests or to generate sample data.

Example usage in code:

```java
// Create a single test user
User user = FactoryManager.getFactory(User.class).create();

// Create a user with specific attributes
User admin = FactoryManager.getFactory(User.class).create("role", "admin", "username", "admin_user");

// Create multiple users
List<User> users = FactoryManager.getFactory(User.class).createMany(10);
```

## Project Structure

- `src/main/java/com/assetcare360/` - Java source code
  - `config/` - Configuration classes
  - `controllers/` - HTTP request handlers
  - `factories/` - Test data generators
  - `interfaces/` - Interface definitions
  - `migrations/` - Database migration management
  - `models/` - Data models
  - `seeders/` - Database seeder management
  - `stores/` - Data access layer
  - `system/` - Core system components
  - `util/` - Utility classes
- `src/main/resources/` - Resource files
  - `migrations/` - SQL migration files
  - `seeders/` - SQL seeder files
- `src/test/java/com/assetcare360/` - Test source code

## API Endpoints

### Users

- `GET /users` - Get all users
- `GET /users/{id}` - Get a user by ID
- `POST /users` - Create a new user
- `PUT /users/{id}` - Update a user
- `DELETE /users/{id}` - Delete a user