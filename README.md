# 🍽️ MealMesh — Hyper-Scale Food Delivery Platform

MealMesh is an enterprise-grade, full-stack food delivery and restaurant management ecosystem built with **Spring Boot 3 (Java 17)**, **PostgreSQL 15**, **Redis 7**, **Apache Kafka**, **Docker**, and modern **React (TypeScript + Tailwind CSS + Vite)**.

---

## 🚀 Architecture Overview

```
mealmesh/
├── backend/                  # Spring Boot 3.2.4 Microservice Core
│   ├── src/main/java/com/mealmesh/
│   │   ├── audit/            # Compliance & AOP Audit Logging
│   │   ├── auth/             # JWT, BCrypt, Security Context, RBAC
│   │   ├── cart/             # Cart State & Pricing Engine
│   │   ├── checkout/         # Order Calculation & Checkout
│   │   ├── coupon/           # Discounts, Promo Codes, Quota Rules
│   │   ├── delivery/         # Driver Routing, Fleet Batching, Live GPS Breadcrumbs
│   │   ├── kafka/            # Event Consumers, Producers, Dead-Letter Queues
│   │   ├── loyalty/          # Reward Points, Multi-Tier Gamification
│   │   ├── menu/             # Categories, Menu Items, Dietary Filters
│   │   ├── notification/     # In-App, Email & SMS Lifecycle Notifications
│   │   ├── order/            # Order State Machine (CREATED -> DELIVERED)
│   │   ├── outbox/           # Transactional Outbox Event Bus
│   │   ├── payment/          # Razorpay Gateway, Idempotency, Refunds
│   │   ├── recommendation/   # Collaborative Filtering & Reorder Engine
│   │   ├── restaurant/       # Geolocation, Catalog, Kitchen Portal
│   │   ├── review/           # Verified Reviews & Star Rating Aggregates
│   │   └── user/             # Customer, Partner & Merchant Profiles
│   ├── src/main/resources/
│   │   ├── db/migration/     # Flyway SQL Schemas (V1 to V14)
│   │   └── application.yml   # Production & Local Profiles
│   └── Dockerfile            # Multi-stage OpenJDK 17 Runtime
│
├── frontend/                 # React 18 + TypeScript + Vite SPA
│   ├── src/
│   │   ├── components/       # Reusable Glassmorphic UI Library
│   │   ├── context/          # AuthContext, CartContext
│   │   ├── pages/            # Home, Restaurants, Menu, Cart, Orders, Tracking
│   │   └── services/         # Axios API Client & Interceptors
│   ├── nginx.conf            # Production Edge Proxy & Asset Caching
│   └── Dockerfile            # Multi-stage Node + Nginx Alpine
│
└── docker-compose.yml        # Full Production Stack (Postgres, Redis, Kafka, Backend, Frontend)
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Backend Framework** | Spring Boot 3.2.4, Java 17 |
| **Persistence** | PostgreSQL 15, Spring Data JPA, Hibernate, Flyway |
| **Caching & In-Memory** | Redis 7, Lettuce, Spring Cache (`@Cacheable`, `@CacheEvict`) |
| **Event Streaming** | Apache Kafka 7.5, Transactional Outbox Pattern |
| **Security & Auth** | Spring Security 6, JWT (`jjwt 0.11.5`), BCrypt |
| **Resilience & Rate Limiting** | Redis Sliding-Window Rate Limiter, Spring AOP |
| **Frontend UI** | React 18, TypeScript, Tailwind CSS, Lucide Icons, Vite |
| **Web Server & Reverse Proxy** | Nginx Alpine, HTTP/2, Gzip, SSE Live Streaming |
| **Containerization** | Docker, Docker Compose |

---

## 📦 Quick Start with Docker Compose

To start the entire platform with all infrastructure services pre-configured:

```bash
# Clone and enter directory
cd mealmesh

# Start all containers in background
docker compose up -d --build
```

### Services Started:
- **Frontend SPA**: `http://localhost/`
- **Backend REST API**: `http://localhost:8080/api/`
- **PostgreSQL 15**: `localhost:5432` (Database: `mealmesh`)
- **Redis 7**: `localhost:6379`
- **Kafka & Zookeeper**: `localhost:9092` & `localhost:2181`

---

## 💻 Local Development Setup

### 1. Backend Setup (Spring Boot)
Ensure you have **Java 17+** and **Maven 3.8+** installed:

```bash
cd backend

# Build and verify all 28 modules
mvn clean compile

# Run tests
mvn test

# Run application
mvn spring-boot:run
```

### 2. Frontend Setup (React + Vite)
Ensure you have **Node.js 18+** and **npm** installed:

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
# -> Local server starts at http://localhost:5173/

# Build for production
npm run build
```

---

## 🔑 Core API Endpoints

### Authentication & Users
- `POST /api/auth/register` — Register a new customer, merchant, or driver
- `POST /api/auth/login` — Authenticate and receive JWT Bearer token
- `GET /api/users/profile` — Get current user profile and delivery addresses

### Restaurants & Menus
- `GET /api/restaurants` — Paginated directory of active restaurants
- `POST /api/restaurants/filter-search` — Multi-facet search (geospatial Haversine radius, cuisines, rating)
- `GET /api/restaurants/{id}/menu` — Cached restaurant menu categories and items

### Cart, Orders & Payments
- `GET /api/cart` / `POST /api/cart/items` — Active shopping cart operations
- `POST /api/checkout` — Calculate discounts, taxes, delivery fee and create order
- `POST /api/payments/create-order` — Initialize payment transaction
- `POST /api/payments/verify` — Verify gateway signature and transition order state

### Live Delivery Tracking & Fleet
- `GET /api/delivery/track/{orderId}` — Real-time driver location, route breadcrumbs, and ETA
- `POST /api/delivery/partner/status` — Driver online/availability status toggle
- `POST /api/delivery/partner/location` — Driver high-frequency GPS ping update

### Real-Time Live Feeds (SSE)
- `GET /api/realtime/orders/{orderId}` — Server-Sent Events stream for order status changes
- `GET /api/realtime/notifications` — Live user notification stream

### Loyalty & Recommendations
- `GET /api/loyalty/my` — View point balance, tier (`BRONZE` to `PLATINUM`), and rewards
- `POST /api/loyalty/redeem` — Redeem points for order discounts
- `GET /api/recommendations/personalized` — Personalized meal recommendations and one-click reorders

---

## 📄 License
This project is licensed under the MIT License.