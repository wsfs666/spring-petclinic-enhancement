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





2026-10-11 PetClinic Progress

已完成：
- 完成 Owner DELETE API
- 完成 Pet DELETE API
- 完成对应 DELETE controller tests
- 完成 Visit GET API
- 完成 Visit POST API
- 完成 Visit controller tests
- VisitRestControllerTests: 8/8 passed

未完成：
- Integration tests 留到后续阶段

Blocker：
- 无

关键理解：
- CascadeType.ALL 包含 REMOVE，可将删除操作从 Owner → Pet → Visit 向下传播
- orphanRemoval 和 cascade REMOVE 的作用不同
- Mockito 的 `save()` 不会真正执行数据库 generated ID
- `willReturn()` 只返回预设对象；`willAnswer()` 可以模拟 save 时给新 Visit 设置 ID
- null Integer 自动拆箱成 int 会导致 NullPointerException
- Controller test 验证 HTTP/controller 行为；真实 persistence/cascade 行为后续用 integration test 验证

Evidence：
- Owner/Pet DELETE controller tests: PASS
- VisitRestControllerTests: 8 tests, 0 failures, 0 errors, 0 skipped
- BUILD SUCCESS
- Commit: finished 
- GitHub push: finished 
- Full regression: finished 






# 2026-10-12 Progress

## Main Goal

Built real Spring Boot integration tests for Visit REST APIs and introduced a Service layer to establish a proper JPA transaction boundary.

## Completed

### 1. Visit Integration Test Environment

Created `VisitIntegrationTests` using:

- `@SpringBootTest`
- `@AutoConfigureMockMvc`
- real `MockMvc`
- real Spring Data JPA repositories
- H2 test database

Verified the full application path:

`MockMvc -> Controller -> Service -> Repository -> Hibernate/JPA -> H2`

Confirmed that real JPA persistence generates entity IDs automatically instead of requiring manually assigned IDs as in Mockito-based controller tests.

### 2. Visit GET / POST Integration Tests

Completed integration tests for:

- `GET /api/owners/{ownerId}/pets/{petId}/visits`
- `POST /api/owners/{ownerId}/pets/{petId}/visits`

Verified:

- real database reads
- real database writes
- generated Owner / Pet / Visit IDs
- correct HTTP status codes
- correct Visit response fields

### 3. Discovered JPA Persistence Lifecycle Issue

Integration testing exposed a problem that the existing `@WebMvcTest` + Mockito tests could not detect.

Observed:

- `saveAndFlush(owner)` successfully persisted the new Visit
- the managed copy contained a generated Visit ID
- the original `visit` object still had `id == null`

Debugging confirmed:

- original `Owner` / `Pet` objects and returned managed copies were different Java objects
- the saved Visit received a real database-generated ID
- the original transient Visit did not receive that ID when the detached entity graph was merged

Key concepts learned:

- managed vs detached entities
- `persist()` vs `merge()`
- `save()` vs `saveAndFlush()` vs `flush()`
- cascade persistence
- dirty checking
- generated IDs
- persistence context

### 4. Added Transaction Boundary

Introduced `@Transactional` to keep the complete Visit create operation inside one persistence context:

`find Owner -> find Pet -> add Visit -> flush -> generated Visit ID`

This allowed the newly created Visit to remain managed and receive its generated ID before constructing the response.

For read operations, identified `@Transactional(readOnly = true)` as the appropriate Service-layer transaction boundary when traversing entity relationships.

### 5. Introduced VisitService

Created a dedicated `VisitService` under the Owner feature package.

Architecture changed from:

`VisitRestController -> OwnerRepository`

to:

`VisitRestController -> VisitService -> OwnerRepository`

Responsibilities are now separated:

- Controller: HTTP mapping, request/response handling
- Service: business flow and transaction boundary
- Repository: persistence

### 6. Refactored Visit Controller Tests

Updated `VisitRestControllerTests` to mock `VisitService` instead of `OwnerRepository`.

Controller tests now focus on:

- URL / path variables
- request binding
- validation
- HTTP status
- JSON response
- correct Service invocation

Removed the need to manually construct full Owner/Pet entity graphs or simulate generated IDs with Mockito `willAnswer()`.

Also confirmed the semantic distinction:

- Owner missing -> `404`
- Pet missing -> `404`
- Pet exists but has no Visits -> `200 OK` with `[]`





