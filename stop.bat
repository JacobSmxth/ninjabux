@echo off
REM NinjaBux Stop Script for Windows
REM This script stops the backend and frontend services

echo Stopping NinjaBux services...
echo.

REM Kill Gradle bootRun processes
echo Stopping backend...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
)

REM Kill Vite/Node processes on port 5173
echo Stopping frontend...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5173 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
)

REM Also kill any gradle or node processes by name as backup
taskkill /F /IM java.exe /FI "WINDOWTITLE eq NinjaBux Backend*" >nul 2>&1
taskkill /F /IM node.exe /FI "WINDOWTITLE eq NinjaBux Frontend*" >nul 2>&1

echo.
echo NinjaBux services stopped.
echo.
