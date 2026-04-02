@echo off
echo ========================================
echo   Wookie Books - Docker Stop
echo ========================================
echo.

echo Stopping containers...
docker-compose down

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   Containers Stopped Successfully
    echo ========================================
    echo.
) else (
    echo.
    echo ERROR: Failed to stop containers
    echo.
)
pause