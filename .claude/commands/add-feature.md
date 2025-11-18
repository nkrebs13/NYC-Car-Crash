# Add New Feature

Guide for implementing a new feature in this Android app.

## Instructions

When the user requests a new feature: $ARGUMENTS

Follow this implementation workflow:

### 1. Analysis Phase
- Understand the feature requirements
- Identify which layers need changes (UI, ViewModel, Repository, DataSource, Database)
- Check existing code patterns

### 2. Implementation Order

1. **Data Layer** (if needed)
   - Add database entities to `db/entity/`
   - Update DAO in `CarCrashDao.kt`
   - Update database version in `CarCrashDatabase.kt`
   - Add migrations if modifying existing tables

2. **Network Layer** (if needed)
   - Add API models to `network/`
   - Update network data source

3. **Repository Layer**
   - Add methods to `CarCrashRepository.kt` interface
   - Implement in `CarCrashRepositoryImpl.kt`

4. **ViewModel Layer**
   - Update `CarCrashViewModel.kt`
   - Add state properties and methods

5. **UI Layer**
   - Create Composables in `ui/compose/`
   - Follow existing patterns for state handling
   - Use Material Design components

6. **DI Updates**
   - Update `appModuleDi.kt` if new dependencies added

### 3. Testing
- Add unit tests for ViewModel and Repository
- Consider UI tests for new Composables

### 4. Documentation
- Update CLAUDE.md if architecture changes
- Update README.md if user-facing features added
