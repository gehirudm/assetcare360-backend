# AssetCare360 Backend

This repository contains the backend code for the AssetCare360 application.

## Project Structure

```
assetcare360-backend/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── assetcare360/
│   │               └── App.java
│   └── test/
│       └── java/
│           └── com/
│               └── assetcare360/
│                   └── AppTest.java
└── pom.xml
```

## Prerequisites

- Java JDK 8 or higher
- Maven 3.6 or higher

## Building the Project

To build the project, run the following command in the project root directory:

```bash
mvn clean compile
```

This will compile the Java source code and place the compiled classes in the `target/classes` directory.

## Running the Application

You can run the application using Maven's exec plugin:

```bash
mvn exec:java
```

Or you can combine the compile and run steps:

```bash
mvn compile exec:java
```

## Running Tests

To run the tests, use:

```bash
mvn test
```

## Creating a JAR File

To package the application into a JAR file:

```bash
mvn package
```

This will create a JAR file in the `target` directory.

## Running the JAR File

After packaging, you can run the JAR file using:

```bash
java -jar target/assetcare360-backend-1.0-SNAPSHOT.jar
```

## Development

The main application entry point is in `src/main/java/com/assetcare360/App.java`.

Unit tests are located in `src/test/java/com/assetcare360/AppTest.java`.