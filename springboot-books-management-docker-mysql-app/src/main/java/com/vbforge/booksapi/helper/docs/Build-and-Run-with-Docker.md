# Build and Run with Docker

### Docker Decktop should be running

### Build and start all services
docker-compose up --build

### Or run in detached mode
docker-compose up --build -d

### Check logs
docker-compose logs -f app

### Stop containers
docker-compose down

### Stop and remove volumes (clears database)
docker-compose down -v

### Next time to start (assume image exist)
docker-compose up -d