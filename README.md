# 🏋️‍♂️ Gym Management System

A production-ready, full-stack gym management application featuring member administration, attendance tracking, payment verification, and real-time operations dashboards. 

Powered by a modern event-driven architecture using **Spring Boot 3**, **React**, **PostgreSQL**, **Elasticsearch**, **Apache Kafka**, and **Debezium CDC (Change Data Capture)**, containerized with **Docker** for effortless local or on-premise deployment.

---

## ✨ Features

### 👥 Member Management
- Register and manage gym members.
- Activate/deactivate membership status.
- Assign members to specific gym branches.

### 📅 Attendance & Class Booking
- Mark daily attendance for active members (restricted to active status, one check-in per day).
- Live class occupancy tracking with max capacity constraints.

### 💳 Payment & Subscriptions
- Record monthly membership payments with duplicate prevention.
- Subscription plan tier tracking (Basic, Premium, VIP) with start/end date validation.
- Secure, admin-verified payment system.

### ⚡ Real-Time Events & Analytics (WebSockets)
- **Live Branch Occupancy:** Dynamic progress metrics on how full a gym branch is.
- **Instant Check-ins:** Broadcasted notifications when members scan in.
- **Trainer Availability:** Real-time trainer status updates (Available/Busy).
- **Class Updates:** Live-syncing class booking metrics.

### 🔍 Instant Search (Elasticsearch)
- High-performance, full-text instant member and class searches powered by a dedicated Elasticsearch cluster.

### 🔄 Change Data Capture (CDC Pipeline)
- Outbox pattern and real-time database CDC synchronization utilizing **Debezium Connect** streaming changes from **PostgreSQL** to **Apache Kafka**.

---

## 🛠️ Technology Stack

| Component | Technology | Version |
| :--- | :--- | :--- |
| **Backend Framework** | Spring Boot | 3.2.1 |
| **Database** | PostgreSQL | 15 (Alpine) |
| **Search Engine** | Elasticsearch | 7.17.10 |
| **Message Broker** | Apache Kafka / Zookeeper | 7.3.0 (Confluent) |
| **CDC Connector** | Debezium Connect | 2.4 |
| **Frontend UI** | React / Vite | 18 |
| **Styling** | Vanilla CSS (Modern Gradients) | - |
| **Containerization** | Docker / Docker Compose | 3.8 schema |

---

## 📋 Prerequisites

- **Docker Desktop** (Version 20.10+) or Docker Engine
- **Docker Compose** (Version 2.0+)
- **System Memory:** Minimum 4GB RAM (8GB recommended for Kafka + ES + Debezium)
- **Disk Space:** 10GB free disk space
- **Available Ports:** 3000 (Frontend), 8080 (Backend), 5432 (Postgres), 9200 (Elasticsearch), 29092 (Kafka), 8083 (Debezium Connect)

---

## 🚀 Quick Start

1. **Clone or navigate to the project directory**
   ```bash
   cd d:/Study/GymManagement/GymManagement
   ```

2. **Start the environment (All Services)**
   Ensure Docker Desktop is running, then execute:
   ```bash
   docker-compose up -d --build
   ```
   *This single command builds local Spring Boot/React images and boots PostgreSQL, Kafka, Elasticsearch, and Debezium.*

3. **Access the application**
   - **Web UI:** [http://localhost:3000](http://localhost:3000)
   - **Backend API Docs / Base URL:** [http://localhost:8080](http://localhost:8080)
   - **Default Credentials:**
     - **Username:** `admin`
     - **Password:** `admin`
     - *Please change default passwords in production setups.*

4. **Shutdown the application**
   ```bash
   docker-compose down -v
   ```
   *(Add `-v` to also reset database volumes if you want a fresh start).*

---

## 📁 Project Structure

```
GymManagement/
├── backend/                    # Spring Boot backend application
│   ├── src/main/java/
│   │   └── com/gym/management/
│   │       ├── config/         # System, Web, WS, and Jackson config
│   │       ├── controller/     # REST Endpoints
│   │       ├── service/        # Business Logic & Real-time broadcasts
│   │       ├── repository/     # JPA Data Access Interfaces
│   │       ├── entity/         # PostgreSQL JPA entities
│   │       └── dto/            # REST and Event payloads
│   ├── pom.xml                 # Maven configuration
│   └── Dockerfile              # Multi-stage production build
├── frontend/                   # React Single Page App
│   ├── src/
│   │   ├── components/         # Modular UI views & widgets
│   │   ├── services/           # Axios HTTP request clients
│   │   └── App.jsx             # React router and core layout
│   ├── package.json            # Node project requirements
│   └── Dockerfile              # Production Nginx host build
├── database/
│   └── schema.sql              # Database DDL initialization script
├── docker-compose.yml          # Docker Compose orchestration script
└── DEPLOYMENT.md               # Advanced production deployment manual
```

---

## 🏗️ Architecture

```
[React Frontend] (Port 3000)
       │ (REST APIs & WebSockets)
       ▼
[Spring Boot Backend] (Port 8080) ◄────── (CDC Sync) ──────┐
       │                                                   │
       ├─► [PostgreSQL] (Port 5432) ──(WAL logs)──► [Debezium Connect] (Port 8083)
       │                                                   │
       ├─► [Elasticsearch] (Port 9200)                     ▼
       │                                            [Apache Kafka] (Port 29092)
       └─► [WebSocket Clients] (/topic/*)
```

### Event-Driven Highlights
- **Postgres Write-Ahead Log (WAL):** Captured continuously by Debezium.
- **Kafka Topics:** Real-time event streams populated from table updates.
- **Spring Boot Consumer:** Listens to Kafka event topics to update the client dashboard dynamically.

---

## 🔧 Local Development (Without Docker Containerization)

If you prefer to run services manually on your local system:

### 1. Database (PostgreSQL)
Ensure Postgres is running on port 5432 and run the DDL schema in `database/schema.sql`.

### 2. Backend Application
Configure local credentials in `backend/src/main/resources/application.properties` and run:
```bash
cd backend
mvn spring-boot:run
```

### 3. Frontend Application
```bash
cd frontend
npm install
npm run dev
```
Navigate to [http://localhost:5173](http://localhost:5173).

### 4. Database CLI Access (Docker-backed)
If using Docker, access the Postgres terminal directly:
```bash
docker exec -it gym-postgres psql -U gymuser -d gym_management
```

---

## 📊 Core API Endpoints

### 🔐 Authentication
- `POST /api/auth/login` - Authenticate administrator/user.
- `POST /api/auth/logout` - Clear active session.
- `GET /api/auth/validate` - Session validation check.

### 👥 Member Management
- `POST /api/members` - Register a new member.
- `GET /api/members` - Retrieve all members.
- `GET /api/members/{id}` - View profile details.
- `PUT /api/members/{id}/activate` - Set member status to ACTIVE.
- `PUT /api/members/{id}/deactivate` - Set member status to INACTIVE.

### 📅 Attendance Tracker
- `POST /api/attendance` - Log a member check-in.
- `GET /api/attendance` - Query all check-ins.
- `GET /api/attendance/member/{memberId}` - Historical attendance logs.

### 💳 Payments
- `POST /api/payments` - Record a new payment.
- `GET /api/payments` - Retrieve payment logs.
- `GET /api/payments/member/{memberId}` - Check payment history of a specific member.

---

## 📝 License

This project is created for educational and commercial use.

---

## 👨‍💻 Author

Built as a production-ready gym management solution using modern micro-interaction capabilities and event-driven patterns.
