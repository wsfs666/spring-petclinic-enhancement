## getPetVisitHistory

### Input
- petId: integer

### Output
- petId
- petName
- visits
  - id
  - date
  - description
  - petId
  - ownerId

### Semantics
- Pet exists with visits → return visit history
- Pet exists without visits → return `visits = []`
- Pet does not exist → NOT_FOUND

### Data Flow
PetVisitHistoryTool
→ VisitService
→ OwnerRepository
→ PostgreSQL