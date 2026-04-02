# Run commands in order (Windows PowerShell):

### 1. First, create .env.docker
place into this file credentials:
```
# Database Configuration
DB_USERNAME=wookie_user
DB_PASSWORD=your_secure_password_here

# JWT Configuration
JWT_SECRET_KEY=your-256-bit-secret-key-for-docker-min-32-chars
JWT_EXPIRATION=86400000

# Business rules
RESTRICTED_USERNAME=DarthVader
```

### 2. Build the Docker image
```
.\docker-build.bat
```

### 3. Start the containers
```
.\docker-start.bat
```

### 4. Test the setup
```
.\docker-test.bat
```

### 5. View logs (optional)
```
docker-compose logs -f app
```

### 6. Stop when done
```
.\docker-stop.bat
```

### 7. Push to DockerHub repo
need to create a repo first on DockerHub! (vladbogdadocker/wookie-marketplace-app)
```
.\docker-push.bat
```

---

## Summary (PowerShell):

### 1) LOCAL DEVELOPMENT (Build and run locally):

 - .\docker-build.bat          # Builds local image
 - .\docker-start.bat          # Starts on port 8084 (MySQL: 3307)
 - .\docker-test.bat           # Tests local deployment
 - .\docker-stop.bat           # Stops local containers

### 2) DOCKER HUB DEPLOYMENT (Pull from Hub):

**First time: Push image to Hub**
 - .\docker-push.bat           # Pushes to vladbogdadocker/wookie-marketplace-app

**Then run from Hub**
 - .\docker-start-hub.ps1      # Starts on port 8085 (MySQL: 3308)
 - .\docker-test-hub.ps1       # Tests Hub deployment
 - .\docker-stop-hub.ps1       # Stops Hub containers

### 3) Port Summary:

| Environment | API Port | MySQL Port | Container Names                  |
|-------------|----------|------------|----------------------------------|
| Local       | 8084     | 3307       | wookie_mysql, wookie_app         |
| Docker Hub  | 8085     | 3308       | wookie_mysql_hub, wookie_app_hub |

	