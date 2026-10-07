##20261005
- Established local baseline
- Verified Maven build
- 56 tests passed
- Verified Owner / Pet / Visit / Vet flows
- Confirmed H2 baseline database
- Reviewed project structure

##20261007
## Completed
- Implemented `GET /api/owners/{id}`
- Added `404 Not Found` handling for missing owner
- Implemented `GET /api/owners?lastName=...`
- Added pagination with `Pageable` / `PageRequest`
- Used `Page.map()` to convert `Page<Owner>` → `Page<OwnerResponse>`
- Added `OwnerResponse` DTO mapping
- Added `OwnerRestControllerTests`
- Tested:
  - successful owner lookup by ID
  - owner not found
  - owner search by last name
  - HTTP status
  - JSON response fields
- Fixed incorrect `firstName` / `lastName` DTO mapping detected by test
- Fixed Spring Boot 4 testing imports and compilation issues

## New APIs / Annotations Learned
- `@RestController`
- `@GetMapping`
- `@PathVariable`
- `@RequestParam`
- `@WebMvcTest`
- `@MockitoBean`
- `MockMvc`
- `jsonPath()`
- `status()`
- `Optional.orElseThrow()`
- `ResponseStatusException`
- `HttpStatus.NOT_FOUND`
- `Page<T>`
- `Pageable`
- `PageRequest.of()`
- `Page.map()`
- `PageImpl`
- `List.of()`
- Mockito `given()`
- Mockito `willReturn()`
- Mockito `eq()`
- Mockito `any()`

## Current Owner REST API Status
- GET by ID: Complete
- Search by last name: Complete
- 404 handling: Complete
- Controller tests: Complete
- POST create owner: Not started
- PUT/PATCH update owner: Not started
- DELETE owner: Not started
- Validation: Not started