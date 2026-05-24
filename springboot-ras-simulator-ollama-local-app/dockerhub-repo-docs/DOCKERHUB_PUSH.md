# DockerHub Push — Step by Step

Run these commands from the `ras-simulator/` project root.

## 1. Login to DockerHub

```bash
docker login
# Enter your DockerHub username and password when prompted
```

## 2. Build the image with your username tag

```bash
docker build -t vladbogdadocker/ras-simulator:latest .
```

> Replace `vbforge` with your actual DockerHub username if different.
> The build takes 2-4 minutes first time (Maven downloads dependencies inside the container).

## 3. Push to DockerHub

```bash
docker push vladbogdadocker/ras-simulator:latest
```

## 4. Verify it's live

```bash
docker pull vladbogdadocker/ras-simulator:latest
```

Or visit: https://hub.docker.com/r/vladbogdadocker/ras-simulator

---

## What others need to run your app

They only need Docker installed. Then:

#### 1. Create docker-compose.yml with these contents:
####    (shared file for DockerHub description)

```bash
version: '3.9'
services:
  app:
    image: vladbogdadocker/ras-simulator:latest
    ports:
      - "8080:8080"
    environment:
      RAS_OLLAMA_BASE-URL: http://ollama:11434
    depends_on:
      ollama:
        condition: service_healthy
    networks:
      - ras-network

  ollama:
    image: ollama/ollama:latest
    ports:
      - "11434:11434"
    volumes:
      - ollama-data:/root/.ollama
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:11434/api/tags"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    networks:
      - ras-network

volumes:
  ollama-data:

networks:
  ras-network:
    driver: bridge
```

#### 2. Start containers
- docker-compose up -d

#### 3. Pull the AI model (first time only)
- docker exec -it ras-ollama ollama pull llama3.2:3b

#### 4. Open browser
- open http://localhost:8080

---



