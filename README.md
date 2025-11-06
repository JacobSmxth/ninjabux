# NinjaBux System

A belt-based progression and reward system for Code Ninjas students. Students earn Bux currency by completing lessons, participate in weekly quizzes, unlock achievements, and spend their earnings in the shop.

## Core Features

### Currency & Progression
- **Dual Ledger System**: Immutable transaction log tracking all Bux movement with quarter-precision (0.25 increments)
- **Cached Balances**: Performance-optimized balance calculation with on-demand recalculation
- **Belt Progression**: WHITE → YELLOW → ORANGE → GREEN → BLUE with variable lesson counts per belt/level
- **Progress History**: Immutable event log of all student progression with correction support

### Earning Mechanics
- **Lesson Completion**: Belt-based rates from 1.0 Bux (White) to 3.0 Bux (Blue) per lesson
- **Alternating Payouts**: Half-belts (Yellow/Green) alternate between floor and ceiling values
- **Milestone Rewards**: Level-up bonuses (2-6 Bux) and belt-up bonuses (5 Bux flat)
- **Quiz Rewards**: 1 Bux per correct answer on weekly quiz questions
- **Admin Awards**: Manual Bux adjustments with full audit trail

### Shop System
- **Purchase States**: PURCHASED → REDEEMED/REFUNDED/CANCELED
- **Refund System**: Full refunds via ledger reversal without deleting records
- **Purchase Limits**: Configurable per-student, per-day, lifetime, and concurrent limits
- **Item Categories**: break-time, rewards, premium with availability toggle

### Achievement System
- **Achievement Types**: Progress, Quiz, Purchase, Social, Special, Veteran
- **Auto-Unlock**: Criteria-based unlocking via JSON configuration
- **Progress Tracking**: Percentage completion for locked achievements
- **Hidden Achievements**: Only visible after unlocking
- **Leaderboard Badge**: User-selected achievement for display

### Quiz System
- **Weekly Questions**: Monday-Sunday cycles with multiple choice format
- **Student Suggestions**: PENDING → APPROVED/REJECTED workflow with notifications
- **Answer Tracking**: One answer per question with accuracy statistics
- **Historical Access**: View past questions with answer status

### Leaderboards
- **Time Periods**: Lifetime, daily, weekly, monthly
- **Categories**: Top Earners, Top Spenders, Most Improved, Quiz Champions
- **Features**: Shows top 3 achievements plus leaderboard badge per student

### Analytics Dashboard
- **Progress Metrics**: Velocity tracking, stall detection, session counting
- **Economy Health**: Circulation tracking, spend/earn ratio, balance distribution
- **Engagement**: Quiz participation, shop activity, achievement progress
- **Item Popularity**: Purchase counts with price optimization suggestions

### Account Management
- **Lock System**: Prevents login and all actions with reason tracking
- **Real-time Notifications**: WebSocket alerts for progress, achievements, and account changes
- **Admin Notes**: Internal-only notes field for tracking

## Authentication & Authorization
- **Admin System**: BCrypt password hashing with role-based permissions
- **Ninja Login**: Username-based authentication
- **Audit Logging**: Tracks all admin actions with timestamps and details

## Data Integrity
- **Immutable Ledger**: Never deletes transactions, only adds corrections
- **Progress Validation**: Checks belt/level/lesson combinations against system limits
- **Balance Verification**: Prevents over-spending via ledger verification
- **Correction System**: Links corrections to original entries with balance adjustments

## Real-time Features
- **WebSocket Notifications**: Personal and broadcast notifications for milestones
- **Auto-dismiss**: Live session only, no persistence

## Known Issues

### Critical
- Race conditions in progress updates require early ninja save to prevent ledger confusion
- Cached balances may desync if ledger transactions fail after ninja save
- Level 0 / Lesson 0 validation exists but historical issues with 0-indexing

### High Priority
- Analytics lesson counting uses complex backward calculation with multiple fallback paths
- Alternator state persists between lessons but multi-lesson jump logic unclear
- Purchase refunds work on redeemed purchases (should probably block)
- Achievement progress calculation returns 0 on any exception

### Medium Priority
- Purchase limit enforcement incomplete
- Memory inefficiency loading unlimited ledger transactions then limiting in-memory
- N+1 query patterns in achievements, questions, and history loading
- Error handling swallows exceptions with basic console logging

### Low Priority
- Magic numbers hardcoded (120 initial grant, 5 belt reward) instead of configuration
- God services violating single responsibility principle (800+ line services)
- WebSocket error handling fails silently without affecting business logic

### Security Concerns
- Admin authentication uses unverified X-Admin-Username header
- H2 console enabled with web-allow-others=true
- No rate limiting on purchases, quiz submissions, or achievement unlocks

### Data Integrity Risks
- No automated reconciliation process for balance drift
- Correction chains have no depth limit
- Achievement revoke deletes progress entries entirely

## TODO

### Leaderboards
- Fix all-time top earners listing
- Fix top spenders leaderboard
- Review and fix styling across all leaderboards

### Achievements
- Implement Veteran 1, 2, 3 achievements
- Add achievement progress tracking in admin panel

### UI/UX
- Fix frontend alerts vs modal popups inconsistency
- Fix white belt colors (poor contrast)
- Clean up glow effects on badges
- Add back button on admin login
- Improve manage Bux UI form in ninja details page
- Fix Bux balance decimal display (remove .00)

### Features
- Add purchase limits enforcement
- Better shop items selection
- Remove belt adjustment block to prevent accidental over-leveling

### Technical Debt
- Fix lesson tracking for most improved calculation
- Test analytics system thoroughly
- Review and refactor AI-generated frontend code
- Improve error handling throughout

## Active Bugs
- Achievement notifications sometimes fail to show
- Leaderboard displays stale data occasionally
- Shop purchase limits fail under heavy load
- Quiz submission requires multiple attempts on slow connections
- Progress history editing creates duplicate entries
