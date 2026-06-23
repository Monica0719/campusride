# 🚗 CampusRide — College Carpooling Platform

A real-time, production-style backend for a college carpooling platform. Built to demonstrate practical Java backend engineering — authentication, caching, event-driven messaging, real-time communication, API documentation, and containerization, all working together in one cohesive system.

## 🌍 The Problem

Every day, students travel the same routes to campus — alone, paying full fare. CampusRide connects drivers with empty seats to riders going the same way, splitting the cost and reducing traffic.

## 🛠️ Tech Stack

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-9.7-blue)
![Redis](https://img.shields.io/badge/Redis-Cache-red)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-Messaging-black)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED)
![JWT](https://img.shields.io/badge/Auth-JWT-yellow)
![Swagger](https://img.shields.io/badge/Docs-Swagger-85EA2D)

## ✨ Features

- **JWT Authentication** — secure register/login with BCrypt password hashing
- **Smart Ride Search** — Haversine formula to find nearby rides within a radius
- **Atomic Seat Booking** — Redis-backed atomic decrement prevents double-booking under concurrent requests
- **Real-Time Notifications** — Kafka producer/consumer pipeline fires booking events to drivers
- **Live Location Tracking** — WebSocket (STOMP + SockJS) streams driver location to subscribed riders
- **Interactive API Docs** — Swagger UI with built-in JWT authorization
- **Fully Containerized** — Docker Compose spins up MySQL, Redis, Kafka, and the app with one command

## 🏗️ Architecture

Client (Postman/App)

│

▼

Spring Boot App (JWT Filter → Controller → Service → Repository)

│

┌────┼────────────┬─────────────┐

▼    ▼            ▼             ▼

MySQL  Redis      Kafka       WebSocket

(data) (seats)  (notifications) (live location)

## 📡 Key API Endpoints

| Method | Endpoint                          | Description                     |
|--------|------------------------------------|----------------------------------|
| POST   | `/api/auth/register`              | Register a new user             |
| POST   | `/api/auth/login`                 | Login and receive JWT token     |
| POST   | `/api/rides/{driverId}`           | Driver posts a new ride         |
| GET    | `/api/rides/search`               | Search nearby rides (Haversine) |
| POST   | `/api/bookings/{rideId}/rider/{riderId}` | Rider books a seat       |
| PUT    | `/api/bookings/{id}/confirm`       | Driver confirms a booking        |

Full interactive documentation available via Swagger UI (see below).

## 🚀 Getting Started

### Option 1 — Run with Docker (Recommended)

```bash
git clone https://github.com/Monica0719/campusride.git
cd campusride/campusride
docker-compose up --build
```

App will be available at `http://localhost:8081`
Swagger UI at `http://localhost:8081/swagger-ui/index.html`

### Option 2 — Run Locally

1. Copy `application.properties.example` → `application.properties` and fill in your local MySQL credentials
2. Ensure MySQL, Redis, and Kafka are running locally
3. Run:
```bash
mvn clean spring-boot:run
```

## 📌 Status

🔨 Actively developed — frontend integration and additional notification channels in progress.

## 👤 Author

Monica Pingali