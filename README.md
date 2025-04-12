# Job Application System

This is a **Job Application System** built using **Spring Boot Microservices Architecture**. It enables companies to post job openings and candidates to register and apply for jobs through REST APIs.

---

## 🧩 Modules

This project is divided into the following microservices:

- **API Gateway** – Handles all incoming requests and routes them to appropriate services.
- **Authentication Service** – Manages user login, registration, and JWT-based authentication.
- **Company Service** – Handles company registration and job posting.
- **Job Service** – Manages job applications and job search.
- **Rating Service** – Allows users to rate and review companies.
- **Service Registry (Eureka)** – Registers all services and enables service discovery.

---

## ⚙️ Tech Stack

- **Java**
- **Spring Boot**
- **Spring Cloud** (Eureka, Gateway, OpenFeign)
- **Spring Security** with JWT
- **Maven**
- **Git**

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven
- STS or IntelliJ
- Postman (for testing APIs)

### Run Steps

1. Clone the repository.
2. Import all services as Maven projects in Spring Tool Suite.
3. Start the **Eureka Server (Service Registry)**.
4. Run each microservice one by one.
5. Use Postman or browser to test endpoints.

---

## 📌 Features

- Modular microservices structure
- REST APIs for each service
- JWT-based authentication
- API Gateway as single entry point
- Eureka for service discovery
- Feign client for interservice communication

---

## 🧪 Testing

You can use [Postman](https://www.postman.com/) to test the following:

- Register/Login users
- Post jobs
- Apply for jobs
- Fetch job listings
- Submit company ratings

---

## 🙌 Author

**Meet Patel**  
Final Year Student | Spring Boot Developer

---

## 📄 License

This project is licensed for educational and personal use.
