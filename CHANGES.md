## Changes

* Remove deprecated balance tracking fields (totalBuxEarned/totalBuxSpent) to eliminate dual source of truth
* Remove ledger cache fields (cachedBuxBalanceQuarters/cachedLegacyBalance) to prevent stale balance issues under concurrent updates
* Remove lesson counter fields (lessonsAllTime/lessonsSinceConversion/postOnboardLessonCount/postLegacyAlternator) to simplify domain model
* Remove lock metadata fields (lockReason/lockedAt) to reduce domain complexity
* Remove adminNote field to simplify admin interface
* Remove Progress Velocity analytics feature (calculateProgressVelocity method, ProgressVelocityMetrics DTO, UI section)
* Remove Price Optimization analytics feature (price optimization calculation, PriceOptimizationData DTO, UI section)
* Always recalculate balance from ledger instead of using cache in getBuxBalanceQuarters
* Simplify createNinja by always calling onboardNinjaWithLegacy regardless of starting position
* Simplify lock checks by removing lockReason dependency
* Centralize timestamp initialization using @PrePersist hook
* Add domain methods (lock/unlock/recordAnswer) to encapsulate state transitions

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

