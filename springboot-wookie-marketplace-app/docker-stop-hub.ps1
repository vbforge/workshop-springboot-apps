# docker-stop-hub.ps1
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Wookie Books - Stop Docker Hub Containers" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Stopping containers from Docker Hub..." -ForegroundColor Yellow
docker-compose -f docker-compose-hub.yml down

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  Containers Stopped Successfully" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "ERROR: Failed to stop containers" -ForegroundColor Red
}
Read-Host "Press Enter to continue"