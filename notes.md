# Default Functions from JpaRepository

```kotlin
// CREATE/UPDATE
save(user: User): User                    // Insert or update single entity
saveAll(users: Iterable<User>): List<User> // Batch save

// READ
findById(id: Long): Optional<User>        // Find by primary key
findAll(): List<User>                     // Get all records
findAllById(ids: Iterable<Long>): List<User> // Find multiple by IDs
existsById(id: Long): Boolean             // Check if exists
count(): Long                             // Count all records

// DELETE
delete(user: User)                        // Delete entity
deleteById(id: Long)                      // Delete by ID
deleteAll()                               // Delete all records
deleteAll(users: Iterable<User>)          // Delete multiple entities
deleteAllById(ids: Iterable<Long>)        // Delete by multiple IDs
```
