# Run Tests

Run the project tests.

## Instructions

Run the appropriate test command based on the argument:

- No argument or `unit`: Run unit tests with `./gradlew test`
- `android` or `instrumented`: Run instrumented tests with `./gradlew connectedAndroidTest`
- `all`: Run both unit and instrumented tests

After tests complete:
1. Report the number of tests run, passed, and failed
2. If any tests failed, show the failure details
3. Suggest fixes for failing tests

## Arguments

- `unit` - Unit tests only (default)
- `android` - Instrumented tests only
- `all` - All tests
