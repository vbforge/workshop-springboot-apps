# docker-start-hub.ps1
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Wookie Books - Start from Docker Hub" -ForegroundColor Cyan
Write-Host "  Repo: vladbogdadocker/wookie-marketplace-app" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if .env.docker exists
if (-not (Test-Path ".env.docker")) {
    Write-Host "ERROR: .env.docker file not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please create .env.docker with your credentials" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Starting containers from Docker Hub..." -ForegroundColor Yellow
docker-compose -f docker-compose-hub.yml --env-file .env.docker up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  Wookie Books is Running from Docker Hub!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "API: http://localhost:8085" -ForegroundColor Cyan
    Write-Host "MySQL: localhost:3308" -ForegroundColor Cyan
    Write-Host "Image: vladbogdadocker/wookie-marketplace-app:latest" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "To view logs:" -ForegroundColor Yellow
    Write-Host "  docker-compose -f docker-compose-hub.yml logs -f app"
    Write-Host ""
    Write-Host "To stop:" -ForegroundColor Yellow
    Write-Host "  .\docker-stop-hub.ps1"
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "ERROR: Failed to start containers" -ForegroundColor Red
    Write-Host ""
    Write-Host "Make sure you're logged into Docker Hub:" -ForegroundColor Yellow
    Write-Host "  docker login" -ForegroundColor White
}
Read-Host "Press Enter to continue"