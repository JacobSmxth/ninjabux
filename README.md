# Code Ninjas Bux

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen?logo=spring)
![React](https://img.shields.io/badge/React-18-blue?logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue?logo=typescript)
![License](https://img.shields.io/badge/License-Proprietary-red)

A gamified progress tracking and rewards system for Code Ninjas students.

## Overview

Code Ninjas Bux is a full-stack web application that allows students to earn virtual currency (Bux) as they progress through the Code Ninjas curriculum. Students can spend their earned Bux in a virtual shop, compete on leaderboards, and unlock achievements.

## Tech Stack

### Backend
- Java 21 with Spring Boot 3.5.7
- Spring Security with JWT authentication
- Spring Data JPA with H2 database
- WebSocket support for real-time notifications
- SLF4J logging

### Frontend
- React 18 with TypeScript
- React Router for navigation
- Axios for API communication
- WebSocket client for real-time updates
- Vite for build tooling

## Key Features

### Student Features
- Earn Bux by completing lessons and leveling up
- Browse and purchase items from the shop
- View leaderboards (top earners, top spenders, most improved, quiz champions)
- Answer weekly quiz questions for bonus Bux
- Unlock and display achievements with badges
- Real-time notifications for progress updates and achievements
- Auto-logout after 15 minutes of inactivity

### Admin Features
- Manage students (create, edit, lock/unlock accounts)
- Track student progress with detailed history
- Adjust Bux balances and handle refunds
- Manage shop items with purchase limits and restrictions
- Create and review quiz questions
- Award achievements manually
- View analytics and engagement metrics
- Monitor ninja login activity
- Send announcements to all students
- Full audit trail of all admin actions
- JWT authentication with 30-minute token expiration

### Security
- JWT-based stateless authentication
- Role-based access control (NINJA vs ADMIN)
- Session management with automatic timeout
- Login tracking for security monitoring
- Password-protected admin operations

## Architecture

### Authentication Flow
- Students log in with username only
- Admins log in with username and password
- JWT tokens issued on successful authentication
- Tokens stored in sessionStorage (cleared on browser close)
- Inactivity timer tracks user activity
- Auto-logout after timeout period

### Real-time Updates
- WebSocket connections for live notifications
- Lock status synchronization
- Achievement unlocks
- Progress updates
- System announcements

### Database
- H2 in-file database for development
- JPA entities for domain model
- Audit logging for admin actions
- Login tracking for security

## Project Structure

```
ninjabux/
├── src/main/java/com/example/NinjaBux/
│   ├── config/          # Spring configuration
│   ├── controller/      # REST endpoints
│   ├── domain/          # JPA entities
│   ├── dto/             # Data transfer objects
│   ├── exception/       # Custom exceptions
│   ├── repository/      # Data access layer
│   ├── security/        # JWT and auth security
│   ├── service/         # Business logic
│   └── util/            # Utility classes
├── frontend/src/
│   ├── components/      # React components
│   ├── context/         # React context providers
│   ├── hooks/           # Custom React hooks
│   ├── pages/           # Page components
│   ├── services/        # API clients
│   ├── types/           # TypeScript definitions
│   └── utils/           # Utility functions
└── data/                # H2 database files
```

## Running the Application

### Backend
```bash
gradle bootRun
```
Server starts on http://localhost:8080

### Frontend
```bash
cd frontend
npm install
npm run dev
```
Development server starts on http://localhost:5173

## Setup

On first launch, the system prompts for initial admin account creation. The admin can then create student accounts and configure shop items.

## Development Notes

- Backend uses constructor-based dependency injection
- Services extend base classes to reduce code duplication
- DTOs prevent Hibernate proxy serialization issues
- Frontend uses context API for state management
- Protected routes enforce authentication requirements
