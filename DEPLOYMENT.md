# Gym Management System - Deployment Guide

## Prerequisites

### For All Platforms
- **Docker**: Version 20.10 or higher
- **Docker Compose**: Version 2.0 or higher
- **System Requirements**:
  - Minimum 4GB RAM
  - 10GB free disk space
  - Ports 3000, 8080, and 3306 available

### Installation

#### Windows
1. Download and install [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/)
2. Docker Compose is included with Docker Desktop
3. Ensure WSL 2 is enabled

#### Linux (Ubuntu/Debian)
```bash
# Install Docker
sudo apt update
sudo apt install -y docker.io docker-compose

# Add user to docker group
sudo usermod -aG docker $USER
newgrp docker

# Enable Docker service
sudo systemctl enable docker
sudo systemctl start docker
```

#### macOS
1. Download and install [Docker Desktop for Mac](https://www.docker.com/products/docker-desktop/)
2. Docker Compose is included with Docker Desktop

---

## Quick Start

### 1. Navigate to Project Directory
```bash
cd d:/Study/GymManagement/GymManagement
```

### 2. Start the Application
```bash
docker-compose up -d
```

This command will:
- Build Docker images for backend and frontend
- Start MySQL database
- Start backend service
- Start frontend service
- Create persistent volumes for database

### 3. Access the Application
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **MySQL**: localhost:3306

**Default Login Credentials**:
- Username: `admin`
- Password: `admin`

---

## Auto-Start Configuration

### Option 1: Docker Restart Policy (All Platforms)

The `docker-compose.yml` file is already configured with `restart: unless-stopped` for all services. This means:
- Services will automatically restart if they crash
- Services will start automatically when Docker daemon starts
- Services will NOT restart if you manually stop them

**To enable Docker to start on system boot:**

#### Windows
1. Open Docker Desktop
2. Go to Settings → General
3. Enable "Start Docker Desktop when you log in"

#### Linux
```bash
sudo systemctl enable docker
```

#### macOS
1. Open Docker Desktop
2. Go to Preferences → General
3. Enable "Start Docker Desktop when you log in"

### Option 2: Systemd Service (Linux Only)

For production Linux servers, use systemd for more control:

#### 1. Copy Application to System Directory
```bash
sudo mkdir -p /opt/gym-management
sudo cp -r . /opt/gym-management/
cd /opt/gym-management
```

#### 2. Install Systemd Service
```bash
sudo cp deployment/gym-management.service /etc/systemd/system/
sudo systemctl daemon-reload
```

#### 3. Enable and Start Service
```bash
sudo systemctl enable gym-management
sudo systemctl start gym-management
```

#### 4. Check Service Status
```bash
sudo systemctl status gym-management
```

#### 5. View Logs
```bash
sudo journalctl -u gym-management -f
```

---

## Application Management

### Start Application
```bash
docker-compose up -d
```

### Stop Application
```bash
docker-compose down
```

### Restart Application
```bash
docker-compose restart
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### Check Service Status
```bash
docker-compose ps
```

### Rebuild After Code Changes
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

---

## Database Management

### Access MySQL Console
```bash
docker exec -it gym-mysql mysql -u gymuser -p
# Password: gympass123
```

### Backup Database
```bash
docker exec gym-mysql mysqldump -u gymuser -pgympass123 gym_management > backup.sql
```

### Restore Database
```bash
docker exec -i gym-mysql mysql -u gymuser -pgympass123 gym_management < backup.sql
```

### View Database Data
```bash
docker exec -it gym-mysql mysql -u gymuser -pgympass123 gym_management -e "SELECT * FROM members;"
```

---

## Troubleshooting

### Port Already in Use
If ports 3000, 8080, or 3306 are already in use:

1. Find the process using the port:
   ```bash
   # Windows
   netstat -ano | findstr :3000
   
   # Linux/Mac
   lsof -i :3000
   ```

2. Either stop that process or modify `docker-compose.yml` to use different ports:
   ```yaml
   ports:
     - "3001:80"  # Change frontend port
     - "8081:8080"  # Change backend port
     - "3307:3306"  # Change MySQL port
   ```

### Backend Cannot Connect to MySQL
1. Check MySQL is running:
   ```bash
   docker-compose ps mysql
   ```

2. Check MySQL health:
   ```bash
   docker-compose logs mysql
   ```

3. Verify network connectivity:
   ```bash
   docker exec gym-backend ping mysql
   ```

### Frontend Cannot Connect to Backend
1. Check backend is running:
   ```bash
   docker-compose ps backend
   ```

2. Test backend API:
   ```bash
   curl http://localhost:8080/api/auth/validate
   ```

3. Check Nginx configuration:
   ```bash
   docker exec gym-frontend cat /etc/nginx/conf.d/default.conf
   ```

### Clear All Data and Start Fresh
```bash
docker-compose down -v
docker-compose up -d
```

**Warning**: This will delete all database data!

### View Container Resource Usage
```bash
docker stats
```

---

## Production Deployment Checklist

- [ ] Change default MySQL passwords in `docker-compose.yml`
- [ ] Update hardcoded admin credentials in backend
- [ ] Enable HTTPS with SSL certificates
- [ ] Configure firewall rules
- [ ] Set up regular database backups
- [ ] Configure log rotation
- [ ] Set up monitoring and alerts
- [ ] Review and harden security settings
- [ ] Test auto-start after system reboot

---

## Uninstallation

### Remove Application
```bash
cd d:/Study/GymManagement/GymManagement
docker-compose down -v
```

### Remove Docker Images
```bash
docker rmi gym-management-backend gym-management-frontend
```

### Remove Systemd Service (Linux)
```bash
sudo systemctl stop gym-management
sudo systemctl disable gym-management
sudo rm /etc/systemd/system/gym-management.service
sudo systemctl daemon-reload
```

---

## Support

For issues or questions:
1. Check the logs: `docker-compose logs -f`
2. Verify all services are running: `docker-compose ps`
3. Review this deployment guide
4. Check Docker and Docker Compose versions

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    User Browser                          │
└────────────────────┬────────────────────────────────────┘
                     │ http://localhost:3000
                     ▼
┌─────────────────────────────────────────────────────────┐
│              Frontend (Nginx + React)                    │
│                  Port: 3000 → 80                         │
└────────────────────┬────────────────────────────────────┘
                     │ /api/* → http://backend:8080
                     ▼
┌─────────────────────────────────────────────────────────┐
│           Backend (Spring Boot + Java 17)                │
│                  Port: 8080                              │
└────────────────────┬────────────────────────────────────┘
                     │ JDBC Connection
                     ▼
┌─────────────────────────────────────────────────────────┐
│              Database (MySQL 8.0)                        │
│                  Port: 3306                              │
│            Volume: mysql-data (persistent)               │
└─────────────────────────────────────────────────────────┘
```

All services communicate via Docker network: `gym-network`
