# docker-test-hub.ps1
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Wookie Books - Test Docker Hub Deployment" -ForegroundColor Cyan
Write-Host "  Repo: vladbogdadocker/wookie-marketplace-app" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Checking container status..." -ForegroundColor Yellow
Write-Host ""

# Check MySQL container
$mysqlRunning = docker ps --filter "name=wookie_mysql_hub" --format "table {{.Names}}" | Select-String "wookie_mysql_hub"
if ($mysqlRunning) {
    Write-Host "[OK] MySQL container is running" -ForegroundColor Green
} else {
    Write-Host "[FAIL] MySQL container is NOT running" -ForegroundColor Red
}

# Check App container
$appRunning = docker ps --filter "name=wookie_app_hub" --format "table {{.Names}}" | Select-String "wookie_app_hub"
if ($appRunning) {
    Write-Host "[OK] App container is running" -ForegroundColor Green
} else {
    Write-Host "[FAIL] App container is NOT running" -ForegroundColor Red
}

Write-Host ""
Write-Host "Testing API endpoints..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8085/api/wookie_books" -UseBasicParsing -TimeoutSec 10
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  TEST PASSED! API is working!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "API Response (first 200 chars):" -ForegroundColor Cyan
    Write-Host $response.Content.Substring(0, [Math]::Min(200, $response.Content.Length)) -ForegroundColor White
    Write-Host ""
    Write-Host "API is accessible at: http://localhost:8085" -ForegroundColor Green
} catch {
    Write-Host ""
    Write-Host "ERROR: API not responding at http://localhost:8085" -ForegroundColor Red
    Write-Host ""
    Write-Host "Check container logs:" -ForegroundColor Yellow
    Write-Host "  docker-compose -f docker-compose-hub.yml logs -f app"
}

Write-Host ""
Read-Host "Press Enter to continue"