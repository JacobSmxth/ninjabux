## Changes

### Refactoring & Code Quality
* Refactored backend code to eliminate redundancies and improve maintainability
* Created NinjaServiceBase to eliminate duplicate findNinja logic
* Added DateUtils class for centralized date operations
* Added AdminUtils class for admin-related utilities
* Replaced System.out/err with proper loggers throughout codebase
* Cleaned up leaderboard logic
* Removed empty CategoryRestrictions method
* Optimized delete code in NinjaAdminService

### JWT Authentication & Security
* Added JWT-based authentication system
* Created JwtUtil service for token handling
* Created JwtAuthenticationFilter for request validation
* Updated SecurityConfig with JWT integration
* Created AuthController for login/token endpoints
* Updated AdminController.login() to return JWT tokens
* Added PasswordPromptModal for secure admin operations
* Implemented auto-logout based on token expiration
* Added Ninja login tracking to review access logs

### Frontend Improvements
* Refactored UI components for consistency
* Normalized colors across application
* Cleaned up lint errors
* Updated icon usage to be consistent
* Refactored ninja creation form with corrected level defaults
* Improved App.tsx structure with auth integration
* Updated Login and AdminLogin pages to use JWT auth

### Backend Refactoring (Continued)
* Removed deprecated balance tracking fields (totalBuxEarned/totalBuxSpent) to eliminate dual source of truth
* Removed ledger cache fields (cachedBuxBalanceQuarters/cachedLegacyBalance) to prevent stale balance issues under concurrent updates
* Removed lesson counter fields (lessonsAllTime/lessonsSinceConversion/postOnboardLessonCount/postLegacyAlternator) to simplify domain model
* Removed lock metadata fields (lockReason/lockedAt) to reduce domain complexity
* Removed adminNote field to simplify admin interface
* Removed Progress Velocity analytics feature (calculateProgressVelocity method, ProgressVelocityMetrics DTO, UI section)
* Removed Price Optimization analytics feature (price optimization calculation, PriceOptimizationData DTO, UI section)
* Always recalculate balance from ledger instead of using cache in getBuxBalanceQuarters
* Simplified createNinja by always calling onboardNinjaWithLegacy regardless of starting position
* Simplified lock checks by removing lockReason dependency
* Centralized timestamp initialization using @PrePersist hook
* Added domain methods (lock/unlock/recordAnswer) to encapsulate state transitions

### Shop & Configuration
* Cleaned up shop CSS styling
* Updated default prices in DataInitializer

### Architecture & Belt System
* Refactored belt enum and created beltSpec to better encapsulate levels and lessons
* Improved belt specification structure

## Up Next

### Immediate fixes
* Fix compilation errors (will fix as I go back over my work)
* Update DTOs to remove deprecated fields (totalBuxEarned/totalBuxSpent/lockReason/lockedAt/adminNote)
* Fix pagination sort by cachedBuxBalanceQuarters/cachedLegacyBalance in NinjaService.getNinjasPaginated
* Add database migration to drop removed columns

### Service split (write side)
* ProgressService - authoritative progress moves, validate/apply belt/level/lesson changes, emit domain events
* EarningsService - stateless pure functions to derive bux from progress (lesson/level-up/belt-up rules)
* LedgerFacade - single entry-point for all ledger writes, guard invariants, idempotency tokens
* ProgressHistoryService - append-only audit trail, subscribes to ProgressAdvancedEvent
* LockoutService - lock/unlock policy and notifications
* PurchasePolicyService - enforce purchase limits/restrictions before PurchaseService
* PurchaseService - orchestrate purchase (policy → ledger → persist)
* AchievementOrchestrator - listen to progress/quiz events, decouple from NinjaService
* AdminCorrectionService - single place for history corrections + ledger offsets

### Query services (read side)
* BalanceQueryService - fast consistent reads of balances, delegates to LedgerFacade
* LeaderboardService - projection-based top lists with repo aggregates (SQL SUM/GROUP BY)
* AnalyticsService - precompute most improved, streaks, quiz stats with tight queries

### Cross-cutting utilities
* BeltCatalogService - load/validate belt specs from YAML/JSON
* ProgressValidator - stateless validator for belt/level/lesson combos
* DomainEventBus - transactional event publishing (@TransactionalEventListener AFTER_COMMIT)
* IdempotencyService - prevent double-writes on retries (purchases, admin awards)

### Repository improvements
* Add deleteByNinja methods to all repositories (avoid findAll().stream().filter)
* Add aggregate queries for leaderboards (SUM/GROUP BY in SQL)
* Add focused finders by ninja/date range

## Risks

*Note: These are known temporary issues that will be addressed during the broader refactoring effort.*

* RISK: Performance impact - getBuxBalanceQuarters now always queries ledger instead of using cache (will be optimized via BalanceQueryService with caching strategy)
* RISK: Breaking change - NinjaResponse DTO still exposes removed fields, frontend may break if not updated (will update DTOs in immediate fixes phase)
* RISK: Data migration - existing ninjas may have cached balance values that are now ignored (will add migration script to drop columns)

## Notes

* Out of scope: Frontend updates, database schema migrations, API versioning

