# MealMesh - Distributed Food Delivery & Logistics Platform

## Tagline: Intelligent, Event-Driven Food Delivery

## Overview

MealMesh is a complete, functional, professional full-stack application for distributed food delivery & logistics. It demonstrates real-world software engineering practices including event-driven architecture, microservice-ready modular monolith, and production-grade infrastructure.

## Technology Stack

### Backend
- Java 21+ / Spring Boot 3.x
- Spring Web, Spring Data JPA, Hibernate
- Spring Security, JWT authentication
- Spring Kafka, Spring Data Redis
- PostgreSQL, Flyway migrations
- Maven, Lombok, Actuator

### Frontend
- React, TypeScript, Vite
- Tailwind CSS
- React Router, TanStack Query
- React Hook Form, Zod
- Recharts, Lucide React
- Sonner

### Infrastructure
- PostgreSQL
- Redis
- Apache Kafka
- Docker, Docker Compose

### Testing
- JUnit 5, Mockito
- Spring Boot Test, Testcontainers
- Vitest, React Testing Library

### Documentation
- OpenAPI / Swagger UI
- Postman collections

## Quick Start

```bash
# Start all infrastructure
docker compose up --build

# Backend will start on http://localhost:8080
# Frontend will start on http://localhost:5173
```

## Documentation

- [Phase Completion Reports](architecture/Phases)
- [API Documentation](postman/MealMesh.postman_collection.json)
- [Database ER Diagram](database/er-diagram.md)
- [Architecture Overview](architecture/overview.md)

## Demo Accounts

| Role | Email | Password |
|------|-------|----------|
| Customer | customer@mealmesh.com | password123 |
| Restaurant | restaurant@mealmesh.com | password123 |
| Delivery Partner | delivery@mealmesh.com | password123 |
| Admin | admin@mealmesh.com | password123 |

## System Requirements

- Docker Desktop (for containerized setup)
- Java 21+ (for local backend development)
- Node.js 20+ (for local frontend development)

## License

Proprietary - All rights reserved.