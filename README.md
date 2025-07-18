#  ecom-cart

This README is also available in [Spanish ES](./README.es.md)

**ecom-cart** is a backend API designed for an e-commerce system, 
built with Spring Boot. It includes all the basic features you'd expect 
from an online store. It provides a smooth shopping flow covering product 
management, JWT-based authentication, a shopping cart, as well as user and 
order management. It also includes Redis caching, unit testing, and Swagger
documentation.
---
## Technologies Used

| Tool               | Purpose                               |
|--------------------|----------------------------------------|
| Java 17            | Main programming language              |
| Spring Boot 3.4.5  | Backend framework                      |
| Spring Web         | REST API exposure                      |
| Spring Data JPA    | Persistence with Hibernate             |
| Spring Security + JWT | Authentication & authorization      |
| Redis + Redisson   | Product caching                        |
| MySQL 8            | Relational database                    |
| H2                 | In-memory database for testing         |
| Swagger (OpenAPI)  | Interactive API documentation          |
| Docker Compose     | Containers for MySQL and Redis         |
| JUnit, Mockito     | Unit & integration testing             |

---

## ✅ Features

- `JWT-based authentication` (sign up and login)
- `Product management` (create, paginate, search, update, disable)
- `Orders` (create, paginate, cancel, change status)
- `User management` (create, paginate, update, assign roles)
- `Order details` — view details by order or user
- `Redis cache` to improve performance
- `Stock tracking`: maintain real-time accurate control
- `Cart functionalities`:
    - Add/remove products
    - Prevent adding disabled products
---
## Implemented Validations
## Examples
- `Stock Control`: When creating or updating an order, the API validates 
whether there is enough stock available for the specific product.
- `Automatic Inventory Update`: When an order is placed, the product stock
is updated automatically.
- `Business Rules Enforcement`: The system prevents completed orders from 
being canceled and blocks state changes on canceled orders.
Four distinct order states are managed: Pending, Shipped, Completed, 
and Canceled.
- `Disabled Products`: Only users with appropriate roles (ADMIN or SELLER)
can modify products, while only CUSTOMER roles can purchase or add 
products to the cart.
- `Role-Based Management`: Disabled products cannot be added to the 
cart or included in orders.
---
## Project Structure
- `api/` — API controllers and route definitions
- `config/` — General configurations (Swagger, security, etc.)
- `domain/` — Entities and repositories for database interaction
- `infrastructure/` — Interfaces and business logic
- `resources/` — Configuration files
- `util/` — Utility classes (roles, custom exceptions, etc.)
- `test/` — Unit tests

### Architecture
The project follows a layered architecture inspired by the principles of
Clean Architecture. Responsibilities are clearly separated between the 
controllers (API), services (business logic), 
domain (entities and repositories), and configuration layers.
This structure enhances maintainability and scalability, 
while also making unit testing much easier to implement.

---

##  Local Installation

### 1. Prerequisites

- Java 17 installed
- Docker and Docker Compose
- Maven

### 2. Clone the repository
git clone https://github.com/alejandrorivera22/ecom-cart.git  
cd ecom-cart

### 3. Start MySQL and Redis
docker-compose up -d

### 4. Build and run the application
- ./mvnw clean install
- ./mvnw spring-boot:run

### 5. Access the API at:
- http://localhost:8080/ecom-cart

### 6. Access the Swagger UI documentation:
- http://localhost:8080/ecom-cart/swagger-ui/index.html
---

## Predefined Test Users

These users are preloaded into the database (`data.sql`)  
and can be used to simulate authentication and authorization  
according to the different roles available in the system.

| Rol      | Username    | Contraseña       |
|----------|-------------|------------------|
| Admin    | `admin`     | `adminpassword`  |
| Seller   | `seller `   | `sellerpassword` |
| Customer | `john_doe ` | `password123`    |

> Passwords are encrypted using BCrypt.  
> These credentials are provided for local testing purposes only.

---

###  How to Test JWT Authentication in Swagger

1. Open Swagger UI (`http://localhost:8080/ecom-cart/swagger-ui/index.html`) in your browser.
2. Go to `POST /auth/login` and authenticate using one of the predefined users.
3. Copy the JWT token found in the `token` field of the response.
4. Click on the **"Authorize"** button (lock icon).
5. Paste the token.

##  Author

**Alejandro Rivera**
- [![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue?logo=linkedin)](https://www.linkedin.com/in/alejandro-rivera-verdayes-443895375/)
- [![GitHub](https://img.shields.io/badge/GitHub-000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/alejandrorivera22)

