@echo off
REM NinjaBux Startup Script for Windows
REM This script starts the backend and frontend

echo Starting NinjaBux...
echo.

REM Get the local network IP address
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4 Address"') do (
    set IP_TEMP=%%a
    goto :found_ip
)
:found_ip
REM Trim leading spaces
for /f "tokens=* delims= " %%a in ("%IP_TEMP%") do set NETWORK_IP=%%a
if "%NETWORK_IP%"=="" set NETWORK_IP=your-ip-address

REM Check if backend is already running on port 8080
netstat -ano | findstr :8080 | findstr LISTENING >nul
if %errorlevel% equ 0 (
    echo Backend already running on port 8080
) else (
    echo Starting backend...
    start "NinjaBux Backend" /MIN cmd /c "gradlew.bat bootRun"
    echo Backend starting in background window...
    timeout /t 8 /nobreak >nul
)

REM Check if frontend is already running on port 5173
netstat -ano | findstr :5173 | findstr LISTENING >nul
if %errorlevel% equ 0 (
    echo Frontend already running on port 5173
) else (
    echo Starting frontend...
    start "NinjaBux Frontend" /MIN cmd /c "cd frontend && npm run dev"
    echo Frontend starting in background window...
)

echo.
echo ==========================================
echo NinjaBux is starting up!
echo ==========================================
echo.
echo Local Access:
echo   Backend:       http://localhost:8080
echo   Frontend:      http://localhost:5173
echo   H2 Console:    http://localhost:8080/h2-console
echo.
echo Network Access (from other devices):
echo   Backend:       http://%NETWORK_IP%:8080
echo   Frontend:      http://%NETWORK_IP%:5173
echo.
echo Logs:
echo   Output goes to console windows instead of log files
echo.
echo To stop all services, run: stop.bat
echo.
