### GUVi HCL SDE AI Mini Project - 1

# Inventory & Order Management API

A secure REST API built with Spring Boot for managing products, categories, inventory, and customer orders.

## Tech Stack
- Java 17
- Spring Boot 4.x
- Spring Security + JWT Authentication
- Spring Data JPA + Hibernate
- MySQL
- Swagger / OpenAPI

## Features
- JWT-based authentication with BCrypt password encryption
- Role-based access control (ADMIN / USER)
- Product & category management with many-to-many relationships
- Order management with automatic stock updates
- Pagination, sorting, filtering, and search
- Global exception handling with structured error responses

## Getting Started

### Prerequisites
- Java 17+
- MySQL
- Maven

### Setup
1. Clone the repository
   git clone https://github.com/yourusername/inventory-order-api.git

2. Create a MySQL database
   CREATE DATABASE inventory_db;

3. Set environment variables
   DB_URL=jdbc:mysql://localhost:3306/inventory_db
   DB_USERNAME=your_username
   DB_PASSWORD=your_password
   JWT_SECRET=your_secret_key_min_32_chars
   JWT_EXPIRATION_MINUTES=1440

4. Run the application
   mvn spring-boot:run

## API Documentation
https://yijuchoo.github.io/guvi-inventory-order-mgt/

Available at http://localhost:9000/swagger-ui/index.html after running the app.

## Default Admin Account
Email: admin@example.com

Password: admin123
