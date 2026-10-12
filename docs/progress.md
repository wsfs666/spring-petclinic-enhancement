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





# 2026-10-08 Progress

## Main Goal
Completed Owner REST API create/update validation and controller testing.

## Completed
- Implemented and tested `POST /api/owners`
  - Valid request returns `201 Created`
  - Response includes generated Owner ID
  - Added request DTO validation

- Implemented and tested `PUT /api/owners/{ownerId}`
  - Existing Owner can be updated successfully
  - Existing ID is preserved
  - Nonexistent Owner returns `404 Not Found`
  - Invalid update request returns `400 Bad Request`

- Added / used validation annotations:
  - `@Valid`
  - `@NotBlank`
  - `@Size`
  - `@Pattern`

- Clarified validation flow:
  - JSON → `@RequestBody`
  - `@Valid` triggers request DTO validation
  - Invalid request is rejected before controller logic executes
  - Invalid request should not call `repository.save()`

- Improved controller tests with MockMvc / Mockito
  - Valid POST
  - Blank required field
  - Nonexistent Owner update
  - Invalid PUT request
  - Verified `repository.save()` is not called for invalid requests

- Fixed POST generated-ID test
  - Learned that Mockito does not automatically generate database IDs
  - Mocked `repository.save()` to return a saved Owner with an ID
  - Used the return value of `save()` to build `OwnerResponse`

- Clarified `JpaRepository.save()` behavior
  - New entity with null ID → create / insert
  - Existing entity with ID → update
  - Update should not delete and recreate the entity

## Test Result
- `OwnerRestControllerTests`
- Tests run: 7
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

## Key Understanding
- Validation rules belong on the request DTO.
- `@Valid` triggers validation at the controller boundary.
- `400` means invalid request data.
- `404` means the requested resource does not exist.
- `201` is used for successful resource creation.
- `200` is appropriate for successful update with response body.
- `save()` return value should be used when the persisted entity may contain generated values such as ID.
- MockMvc tests can verify both HTTP behavior and whether persistence logic was invoked.

## Current Owner REST API Status
- GET by ID: Complete
- Search by last name: Complete
- POST create: Complete
- PUT update: Complete
- Request validation: Complete
- 400 handling: Complete
- 404 handling: Complete
- Controller tests: Complete
- PATCH: Not started
- DELETE: Not started










##2026-10-09
已完成：
- Pet GET/POST/PUT implementation
- PetRequest / PetResponse
- PetType lookup
- Owner-Pet relationship handling

未完成：
- Pet controller tests
- AI/ML

Blocker：
- 无

Evidence：
- commit:
- compile/test result:
- GitHub push:

明天是否顺延：
- 是，只顺延 Pet tests



# 2026-10-10 Progress

## Main Goal
Completed Pet REST API GET / PUT controller tests and request validation cleanup.

## Completed

- Completed `PetRestControllerTests` for:
  - `GET /api/owners/{ownerId}/pets`
  - `PUT /api/owners/{ownerId}/pets/{petId}`

- GET test coverage:
  - Owner exists and has pets
    - returns `200 OK`
    - response contains expected pet fields
  - Owner exists but has no pets
    - returns `200 OK`
    - response is an empty list
  - Owner does not exist
    - returns `404 Not Found`

- PUT test coverage:
  - Valid Owner + valid Pet + valid PetType
    - returns `200 OK`
    - pet ID remains unchanged
    - name, birthDate, and pet type are updated correctly
  - Owner does not exist
    - returns `404 Not Found`
  - Pet does not exist under the Owner
    - returns `404 Not Found`
  - PetType does not exist
    - returns `404 Not Found`
  - Invalid request body
    - returns `400 Bad Request`

- Cleaned up Pet request validation:
  - `@NotBlank` for `String petName`
  - `@NotNull` for `LocalDate birthDate`
  - `@NotNull` for `Integer typeId`

- Clarified validation behavior:
  - `@NotBlank` should only be used for String / CharSequence
  - `@NotNull` should be used for object types such as `LocalDate` and `Integer`
  - validation happens before the controller method body executes
  - if validation fails, Spring returns `400` before repository/controller business logic continues

- Clarified MockMvc / Mockito test behavior:
  - `@MockitoBean` must be declared separately for each dependency
  - mocked repository `save()` does not actually persist data
  - test fixtures are built in memory using objects and `given(...).willReturn(...)`
  - unnecessary mock `save()` calls were identified for cleanup

- Clarified REST API testing concepts:
  - UI flow and REST API contract are different
  - backend endpoints must still handle invalid/missing resources even if the normal UI would not generate those requests
  - `400` = invalid request data
  - `404` = requested resource or referenced resource does not exist

## Test Status

`PetRestControllerTests`

- GET cases: complete
- PUT cases: complete
- Validation cases: complete
- Missing Owner / Pet / PetType cases: complete

All current tests are passing.

## Key Understanding

- `@Valid` validates the DTO before entering controller logic.
- `@NotBlank` is for text fields.
- `@NotNull` is for non-String object fields such as `LocalDate` and `Integer`.
- A valid JSON body can still fail Bean Validation and return `400`.
- Malformed JSON and validation failure are both `400`, but they fail at different stages.
- Nested resource endpoints must validate both the parent resource and the target resource.
- Controller tests should verify API behavior, not simulate real database persistence.

## Current Pet REST API Status

- GET pets by Owner: Complete
- PUT Pet update: Complete
- Request validation: Complete
- 400 handling: Complete
- 404 handling: Complete
- Controller tests: Complete



