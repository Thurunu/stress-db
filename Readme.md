
# MySQL Stress Test Tool

A multi-threaded Java application designed to perform stress testing on MySQL databases by executing concurrent CRUD operations (Create, Read, Update, Delete).

## Features

- **Multi-threaded execution**: Configurable number of concurrent threads for maximum database load
- **CRUD operations**: Performs INSERT, SELECT, UPDATE, and DELETE operations randomly
- **Automatic table creation**: Creates a test table if it doesn't exist
- **Graceful shutdown**: Handles CTRL+C interruption cleanly
- **High performance**: Optimized for maximum throughput with minimal overhead

## Prerequisites

- Java 21 or higher
- MySQL server running and accessible
- MySQL JDBC driver (mysql-connector-java)

## Database Setup

1. Ensure your MySQL server is running
2. Create a database named `testdb` (or modify the URL in the code)
3. Create a user with appropriate permissions:
