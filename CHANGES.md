# Bug Fixes & Improvements Summary

## 🔴 Critical Fixes

### 1. Hardcoded Secrets Removed
- **Files**: `application.properties`, `JwtUtil.java`, `JwtAuthFilter.java`
- Secret key and DB password now read from environment variables (`JWT_SECRET`, `DB_PASSWORD`)
- Set these as OS env vars in production. Local dev falls back to default values.

### 2. Duplicate Secret Key Eliminated
- `JwtUtil` is now a Spring `@Component` injected with `@Value`
- `JwtAuthFilter` uses `@Autowired JwtUtil` instead of defining its own key
- One source of truth for the JWT secret

### 3. JWT Filter Returns 401 on Missing/Invalid Token
- Previously: passed the request through silently if no token
- Now: returns HTTP 401 with a clear error message
- Invalid/expired tokens also return 401 instead of silent passthrough

### 4. Register Endpoint No Longer Returns Password Hash
- Added `RegisterResponse` DTO with only: `userId`, `fullName`, `email`, `role`
- Prevents password hash from being exposed in the API response

### 5. Self-Registration as ADMIN Blocked
- Users who try to register with role=ADMIN are automatically downgraded to STUDENT

---

## 🟡 Security & Logic Fixes

### 6. Capacity Enforced on Registration
- **File**: `EventRegistrationService.java`
- Registration now checks `countByEvent(event) >= event.getCapacity()`
- Throws a clear "Event is full" error when at capacity

### 7. Organizer Ownership Check on Event Creation
- **File**: `EventService.createEventForClub()`
- Organizer can only post events for their own club
- Throws exception if they try to post to another club

### 8. Organizer Ownership Check on Event Publishing
- **File**: `EventService.publishEvent()`
- Organizer can only publish events belonging to their own club
- Admin has a separate `adminPublishEvent()` that bypasses this check

### 9. `getPublishedEvents()` Bug Fixed
- **File**: `EventController.java`
- Was calling `getAllEvents()` (returned ALL events including drafts)
- Now correctly calls `getPublishedEvents()` (only PUBLISHED status)

### 10. QR Code Lookup Uses DB Query
- **File**: `EventRegistrationService.findByQrCode()`
- Was loading ALL registrations into memory and streaming to find one QR
- Now uses `findByQrCode(String)` repository method — single DB query

### 11. Redundant Manual Role Checks Removed from AdminController
- Security is already enforced by `SecurityConfig` for `/api/admin/**`
- Removed duplicate `isAdmin` checks from each method

---

## 🟠 New Features Added

### 12. GET /api/events/{eventId} — Single Event by ID
- Students and organizers can now fetch one event by ID

### 13. PUT /api/events/{eventId} — Update Event
- Organizer can update their own event (title, description, date, location, capacity, fee)
- Ownership verified before any update

### 14. DELETE /api/events/{eventId} — Cancel Event
- Organizer can cancel their own event (sets status to CANCELLED)

### 15. DELETE /api/registrations/{registrationId} — Cancel Registration
- Student can cancel their own registration
- Ownership verified before deletion

### 16. PUT /api/admin/clubs/{id}/reject — Reject Club
- Admin can now reject a pending club (sets status to REJECTED)
- Added `REJECTED` value to `ClubStatus` enum

### 17. GET /api/admin/events — Admin Views All Events
- Admin can see all events across all clubs/statuses

### 18. PUT /api/admin/events/{eventId}/publish — Admin Publishes Any Event
- Admin can publish any event, bypassing organizer ownership

---

## 🧹 Code Quality

### 19. Replaced All System.out.println with SLF4J Logger
- `JwtAuthFilter`, `EventController`, `ClubController`
- Use `log.debug()` for auth info, `log.warn()` for JWT failures

### 20. Login Response Includes userId and fullName
- Frontend no longer needs a second request to get basic user info after login
