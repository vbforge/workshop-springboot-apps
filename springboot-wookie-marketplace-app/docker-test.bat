@echo off
echo ========================================
echo   Wookie Books - Docker Test
echo ========================================
echo.

echo Checking container status...
echo.

REM Check MySQL container
docker ps | findstr wookie_mysql > nul
if %errorlevel% equ 0 (
    echo [OK] MySQL container is running
) else (
    echo [FAIL] MySQL container is NOT running
)

REM Check App container
docker ps | findstr wookie_app > nul
if %errorlevel% equ 0 (
    echo [OK] App container is running
) else (
    echo [FAIL] App container is NOT running
)

echo.
echo Testing API endpoints...
echo.

REM Wait for app to be ready
echo Waiting for app to start (15 seconds)...
timeout /t 15 /nobreak > nul

REM Test public endpoint
echo.
echo Testing GET /api/wookie_books
curl -s http://localhost:8084/api/wookie_books
echo.

echo.
echo ========================================
echo   Test Complete
echo ========================================
echo.
echo To view full logs:
echo   docker-compose logs -f app
echo.
pause