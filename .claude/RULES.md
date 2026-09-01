# Project rules (Ticketmaster)

Conventions this codebase already follows. A PR review should flag *deviations*
from these, not re-litigate the conventions themselves. Each rule states the
existing pattern and where to see it, so "why" is always traceable to real code.

## Architecture & layering

- Package by layer: `controller/`, `service/`, `model/`, `model/repository/`,
  `dto/`, `event/`, `notification/`, `search/`. New classes go in the matching
  package — JPA entities in `model/`, Spring Data repos in `model/repository/`,
  request/response payloads in `dto/`.
- Controllers depend only on services (and `EventSearchService`), never on
  repositories directly.
- Controllers never return a JPA `@Entity` from an endpoint — always map to a
  `dto/*Response` first (see `EventController.toResponse`). Flag any new
  endpoint whose return/`ResponseEntity` type is an entity class.
- Keep controllers thin: parameter binding + DTO mapping only. Business logic
  belongs in the service layer.

## Dependency injection

- Constructor injection only: `private final` fields + `@RequiredArgsConstructor`.
  No `@Autowired` field injection anywhere.

## DTOs & entity exposure

- DTOs are flat Lombok classes (`@Builder`, `@Getter`/`@Setter`); no JPA
  annotations in `dto/`.
- Any `@ManyToOne`/`@OneToMany` back-reference that shouldn't round-trip
  through JSON must be `@JsonIgnore` (see `Ticket.event`, `Ticket.booking`).
  Flag a new association missing it.

## Transactions

- Service methods that write across more than one repository, or that
  read-check-then-write (a state check followed by a mutation), must be
  `@Transactional` — see `BookingService.bookTickets` / `confirmBooking`.
  Flag multi-repository writes without it.
- Don't add `@Transactional` to pure read methods unless it's needed for
  lazy-loading.

## Error handling

- Convention in place: `IllegalArgumentException` = "not found",
  `IllegalStateException` = invalid state transition (e.g. ticket not
  available, lock held by someone else). Keep using these two — there is no
  `@ControllerAdvice`/custom exception hierarchy yet. That's a known gap, not
  something a PR needs to fix incidentally.
- Every `Optional`-returning repository lookup must be resolved with
  `.orElseThrow(...)`, and the message should include the id being looked up
  (matches the existing style, e.g. `"User not found: " + id`).

## Concurrency & locking

- Anything that reserves or mutates a `Ticket`'s availability must go through
  `TicketLockService` (Redis lock) before touching ticket state — mirrors
  `BookingService.reserveTicket` / `confirmBooking`. A new reservation/booking
  path that flips `TicketStatus` without acquiring the lock first is a
  double-booking race — flag it as high severity.
- A lock acquired via `tryLock` must be released via `releaseLock` on every
  exit path (success and failure) once the reservation resolves. Check new
  reservation flows for a lock that's acquired but not released on an error
  branch.

## Security

- `PaymentDetails` (`cardNumber`, `cvv`) — and any object containing it — must
  never appear in a log statement (`log.info/debug/warn/error`) or exception
  message. Flag any new log call whose arguments include a `PaymentDetails`,
  `BookingConfirmRequest`, or raw `cardNumber`/`cvv` field.
- Don't persist raw card number/CVV. `PaymentService` is currently a stub; a
  real gateway integration must tokenize before anything touches the database.
- Booking/reservation endpoints currently take `userId` as a plain request
  field with no check that it matches an authenticated caller (IDOR risk).
  This is a known, pre-existing gap — don't block a PR on it, but don't extend
  the pattern to new endpoints without flagging it, and never treat `userId`
  from the request body as authorization.

## Eventing & async

- Cross-cutting side effects (email, search indexing) go through
  `ApplicationEventPublisher` + a listener (`@EventListener` or
  `@SqsListener`), not called inline from the service that triggered them —
  mirrors `BookingConfirmedEvent` → `BookingConfirmationEventListener` /
  `BookingConfirmationQueueConsumer`, and `EventCreatedEvent`/
  `EventDeletedEvent` → `EventSearchIndexListener`. Flag a service calling
  `EmailService` or the search index directly instead of publishing an event.
- `@SqsListener` consumers must be idempotent — SQS redelivers a message after
  its visibility timeout if the handler throws.

## Search sync

- Postgres is the source of truth; Elasticsearch (`EventDocument`) is a
  derived index kept in sync by `EventSearchIndexListener`. Any new `Event`
  field that should be searchable/filterable needs to be added to
  `EventDocument` and the indexing path too — flag drift between the two.

## Testing

- Test coverage is currently minimal (`TicketmasterApplicationTests` only).
  Don't demand full coverage on every PR, but do call out new branching logic
  in booking, payment, or locking code specifically (highest blast radius)
  that ships with no test.

## Style

- No wildcard imports.
- Prefer stream terminal ops already used in the codebase (e.g. `.toList()`
  in `BookingService.getBookingsForUser`) over manual accumulation loops when
  equivalent.
