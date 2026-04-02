@echo off
echo ========================================
echo   Wookie Books - Docker Build
echo ========================================
echo.

echo Building Docker image: wookie-marketplace-app:latest
docker build -t wookie-marketplace-app:latest .

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   Build Successful!
    echo ========================================
    echo.
    echo Image: wookie-marketplace-app:latest
    echo.
    echo Next steps:
    echo   1. Ensure .env.docker exists
    echo   2. Run: docker-start.bat
    echo.
) else (
    echo.
    echo ========================================
    echo   Build Failed!
    echo ========================================
    echo.
)
pause