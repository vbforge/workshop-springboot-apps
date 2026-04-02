@echo off
echo ========================================
echo   Wookie Books - Docker Start
echo ========================================
echo.

REM Check if .env.docker exists
if not exist .env.docker (
    echo ERROR: .env.docker file not found!
    echo.
    echo Please create .env.docker from .env.template
    echo Example:
    echo   copy .env.template .env.docker
    echo.
    echo Then edit .env.docker with your credentials
    pause
    exit /b 1
)

echo Starting containers...
docker-compose --env-file .env.docker up -d

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   Wookie Books is Running!
    echo ========================================
    echo.
    echo API: http://localhost:8084
    echo MySQL: localhost:3307
    echo.
    echo To view logs:
    echo   docker-compose logs -f app
    echo.
    echo To stop:
    echo   docker-stop.bat
    echo.
) else (
    echo.
    echo ERROR: Failed to start containers
    echo.
)
pause