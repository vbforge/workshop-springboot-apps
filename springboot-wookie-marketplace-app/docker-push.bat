@echo off
echo ========================================
echo   Wookie Books - Docker Push to Hub
echo ========================================
echo.

set /p DOCKER_USER="Enter Docker Hub username: "

echo.
echo Building image: %DOCKER_USER%/wookie-marketplace-app:latest
docker build -t %DOCKER_USER%/wookie-marketplace-app:latest .

if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Pushing to Docker Hub...
docker push %DOCKER_USER%/wookie-marketplace-app:latest

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   Push Successful!
    echo ========================================
    echo.
    echo Image: %DOCKER_USER%/wookie-marketplace-app:latest
    echo.
    echo To pull:
    echo   docker pull %DOCKER_USER%/wookie-marketplace-app:latest
    echo.
) else (
    echo.
    echo Push failed! Make sure you're logged in:
    echo   docker login
    echo.
)
pause