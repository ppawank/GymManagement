# Gym Management System

A production-ready, full-stack gym management application with member management, attendance tracking, and payment recording capabilities. Built with Spring Boot, React, and MySQL, fully containerized with Docker for easy on-premise deployment.

## 🚀 Features

### Member Management
- Register new gym members
- Activate/deactivate member accounts
- View member details and status
- Search and filter members

### Attendance Tracking
- Mark daily attendance for active members
- One attendance record per member per day
- View attendance history
- Attendance restricted to active members only

### Payment Management
- Record monthly membership payments
- One payment per member per month
- Track payment history
- Prevent duplicate payments

### Authentication
- Simple session-based authentication
- Secure API endpoints
- Easy to extend to Spring Security + JWT

## 🛠️ Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **MySQL 8.0**
- **Maven**

### Frontend
- **React 18**
- **React Router**
- **Axios**
- **Vite**
- **Modern CSS with Gradients**

### Deployment
- **Docker & Docker Compose**
- **Nginx**
- **Multi-stage builds**
- **Auto-start configuration**

## 📋 Prerequisites

- Docker 20.10+
- Docker Compose 2.0+
- 4GB RAM minimum
- 10GB free disk space

## 🚀 Quick Start

1. **Clone or navigate to the project directory**
   ```bash
   cd d:/Study/GymManagement/GymManagement
   ```

2. **Start the application**
   ```bash
   docker-compose up -d
   ```

3. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Default credentials: `admin` / `admin`

4. **Stop the application**
   ```bash
   docker-compose down
   ```

## 📁 Project Structure

```
GymManagement/
├── backend/                    # Spring Boot backend
│   ├── src/main/java/
│   │   └── com/gym/management/
│   │       ├── controller/     # REST controllers
│   │       ├── service/        # Business logic
│   │       ├── repository/     # Data access
│   │       ├── entity/         # JPA entities
│   │       ├── dto/            # Data transfer objects
│   │       ├── exception/      # Exception handling
│   │       ├── config/         # Configuration
│   │       └── interceptor/    # Auth interceptor
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── application-docker.properties
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                   # React frontend
│   ├── src/
│   │   ├── components/         # React components
│   │   ├── services/           # API services
│   │   ├── App.jsx             # Main app component
│   │   └── index.jsx           # Entry point
│   ├── package.json
│   ├── Dockerfile
│   └── nginx.conf
├── database/
│   └── schema.sql              # MySQL schema
├── deployment/
│   └── gym-management.service  # Systemd service
├── docker-compose.yml          # Docker orchestration
├── DEPLOYMENT.md               # Deployment guide
└── README.md                   # This file
```

## 🔐 Default Credentials

- **Username**: admin
- **Password**: admin

> ⚠️ **Important**: Change these credentials in production!

## 📖 Documentation

- [Deployment Guide](DEPLOYMENT.md) - Complete deployment and auto-start instructions
- [Database Schema](database/schema.sql) - MySQL database structure

## 🏗️ Architecture

### Layered Backend Architecture
```
Controller → Service → Repository → Database
```

### Business Rules
- **Members**: Email must be unique, status can be ACTIVE or INACTIVE
- **Attendance**: Only ACTIVE members can mark attendance, one record per member per day
- **Payments**: One payment per member per month, amount must be positive

### Auto-Start
- Docker restart policy: `unless-stopped`
- Systemd service for Linux servers
- Docker Desktop auto-start for Windows/macOS

## 🔧 Development

### Backend Development
```bash
cd backend
mvn spring-boot:run
```

### Frontend Development
```bash
cd frontend
npm install
npm run dev
```

### Database Access
```bash
docker exec -it gym-mysql mysql -u gymuser -p
# Password: gympass123
```

## 📊 API Endpoints

### Authentication
- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout
- `GET /api/auth/validate` - Validate session

### Members
- `POST /api/members` - Register member
- `GET /api/members` - List all members
- `GET /api/members/{id}` - Get member details
- `PUT /api/members/{id}/activate` - Activate member
- `PUT /api/members/{id}/deactivate` - Deactivate member

### Attendance
- `POST /api/attendance` - Mark attendance
- `GET /api/attendance` - List all attendance
- `GET /api/attendance/member/{memberId}` - Member attendance history

### Payments
- `POST /api/payments` - Record payment
- `GET /api/payments` - List all payments
- `GET /api/payments/member/{memberId}` - Member payment history

## 🐛 Troubleshooting

See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed troubleshooting guide.

## 📝 License

This project is created for educational and commercial use.

## 👨‍💻 Author

Built as a production-ready gym management solution with modern technologies and best practices.
