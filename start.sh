#!/bin/bash

# NinjaBux Startup Script
# This script starts the backend and frontend

echo "Starting NinjaBux..."

# Get the local network IP address
NETWORK_IP=$(ip route get 1.1.1.1 2>/dev/null | awk '{print $7}' | head -1)
if [ -z "$NETWORK_IP" ]; then
    NETWORK_IP=$(ip addr show | grep -oP '(?<=inet\s)\d+(\.\d+){3}' | grep -v '127.0.0.1' | head -1)
fi
if [ -z "$NETWORK_IP" ]; then
    NETWORK_IP="<your-ip-address>"
fi

BACKEND_RUNNING=$(lsof -Pi :8080 -sTCP:LISTEN -t)
FRONTEND_RUNNING=$(lsof -Pi :5173 -sTCP:LISTEN -t)

if [ -n "$BACKEND_RUNNING" ] || [ -n "$FRONTEND_RUNNING" ]; then
    echo "NinjaBux appears to be running."
    read -p "Would you like to restart it? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Stopping existing processes..."
        pkill -f 'gradle.*bootRun' && pkill -f 'vite'
        sleep 2 # Give processes time to die
    else
        echo "Exiting."
        exit 0
    fi
fi


echo "Starting backend..."
./gradlew bootRun > back.log &
BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"
sleep 5

echo "Starting frontend..."
cd frontend
npm run dev &
FRONTEND_PID=$!
echo "Frontend PID: $FRONTEND_PID"
cd ..


echo ""
echo "=========================================="
echo "NinjaBux is starting up!"
echo "=========================================="
echo ""
echo "Local Access:"
echo "  Backend:       http://localhost:8080"
echo "  Frontend:      http://localhost:5173"
echo "  H2 Console:    http://localhost:8080/h2-console"
echo ""
echo "Network Access (from other devices):"
echo "  Backend:       http://${NETWORK_IP}:8080"
echo "  Frontend:      http://${NETWORK_IP}:5173"
echo ""
echo "Logs:"
echo "  Output goes to console instead of log files"
echo ""
echo "To stop all services:"
echo "  pkill -f 'gradle.*bootRun' && pkill -f 'vite'"
echo ""
