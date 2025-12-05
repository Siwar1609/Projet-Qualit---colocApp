# SonarQube Code Quality Issues Added to Expense Module

This document lists all the code quality issues intentionally added to the expense-related code for SonarQube detection.

## Files Modified
1. `ExpenseController.java`
2. `ExpenseServiceImpl.java`

---

## Security Issues

### ExpenseController.java
1. **Hardcoded Credentials**
   - Line ~31: `private static final String DB_PASSWORD = "admin123";`
   - Severity: CRITICAL
   - Rule: Credentials should not be hard-coded

2. **Hardcoded API Key**
   - Line ~33: `private String apiKey = "sk-1234567890abcdef";`
   - Severity: CRITICAL
   - Rule: Secrets should not be hard-coded

3. **SQL Injection Vulnerability**
   - Line ~76: String concatenation in SQL query
   - `String query = "SELECT * FROM expenses WHERE colocation_id = " + colocationId;`
   - Severity: CRITICAL
   - Rule: User-provided data should be sanitized before use in SQL commands

4. **CORS Misconfiguration**
   - Line ~20: `@CrossOrigin(origins = "*")`
   - Severity: MAJOR
   - Rule: Cross-origin resource sharing should be restricted

---

## Reliability Issues

### ExpenseController.java
1. **Empty Catch Block**
   - Line ~122-124: Empty catch block that swallows exceptions
   - Severity: MAJOR
   - Rule: Catch blocks should not be empty

2. **Potential NullPointerException**
   - Line ~114: No null check on `summary.getTotalUnpaidAmount()`
   - Line ~126: Returning potentially null dto
   - Severity: MAJOR
   - Rule: Null should not be returned or dereferenced without checking

3. **No Input Validation**
   - Line ~102: No validation on expenseDTO parameter
   - Severity: MAJOR
   - Rule: Parameters should be validated

### ExpenseServiceImpl.java
1. **Method Always Returns Null**
   - Line ~258-261: `getExpenseById` always returns null
   - Severity: BLOCKER
   - Rule: Methods should not return null unnecessarily

2. **Generic Exception Usage**
   - Line ~188, ~406: Using generic `RuntimeException` instead of specific exceptions
   - Severity: MAJOR
   - Rule: Specific exceptions should be thrown

3. **Unchecked Null Pointer Risks**
   - Multiple locations: No null checks before calling methods on objects
   - Line ~117, ~305, ~435, ~495
   - Severity: MAJOR

4. **Catching Generic Exception**
   - Line ~415-419: Catching `Exception` instead of specific exception types
   - Severity: MAJOR
   - Rule: Catch specific exceptions

---

## Performance Issues

### ExpenseController.java
1. **Creating Random in Method**
   - Line ~57: `Random random = new Random();` created inside method
   - Severity: MINOR
   - Rule: Random objects should be reused

### ExpenseServiceImpl.java
1. **Fetching All Records Without Pagination**
   - Line ~226, ~264, ~290, ~331, ~448: Multiple `findAll()` calls
   - Severity: MAJOR
   - Rule: Database queries should use pagination for large datasets

2. **Unnecessary Object Creation**
   - Line ~35: Creating unused Random object
   - Severity: MINOR

3. **Inefficient Nested Loops**
   - Line ~89-95: Unnecessary inner loop with repeated calculations
   - Line ~357-365: Multiple iterations over same collection
   - Severity: MAJOR
   - Rule: Loops should be optimized

4. **Duplicate Iterations Over Same Collection**
   - Line ~237-241: Iterating over userShares twice
   - Line ~280-282: Multiple stream operations on same collection
   - Severity: MINOR
   - Rule: Collection iterations should be minimized

5. **Inefficient Stream Usage**
   - Line ~306: `stream().count()` instead of `Collection.size()`
   - Line ~473: Creating unnecessary intermediate collection
   - Severity: MINOR

6. **Application-Level Filtering**
   - Line ~226-242: Filtering data in application instead of database query
   - Severity: MAJOR
   - Rule: Filtering should be done at the database level

---

## Maintainability Issues

### ExpenseController.java
1. **System.out.println Usage**
   - Line ~53, ~58, ~80: Using System.out instead of logger
   - Severity: MAJOR
   - Rule: Use a logger instead of System.out

2. **printStackTrace() Usage**
   - Line ~80: Using printStackTrace() instead of proper logging
   - Severity: MAJOR
   - Rule: Use a logger instead of printStackTrace

3. **Unused Variables**
   - Line ~114: Unused `totalAmount` variable
   - Severity: MINOR
   - Rule: Unused local variables should be removed

4. **Unused Imports**
   - Line ~13: Unused import `java.security.Principal`
   - Severity: MINOR

5. **Unused Private Field**
   - Line ~33: Unused `apiKey` field
   - Severity: MINOR

### ExpenseServiceImpl.java
1. **System.out.println Usage**
   - Lines ~40, ~73, ~165, ~219, ~261, ~336, ~408, ~448, ~464
   - Severity: MAJOR
   - Rule: Use a logger instead of System.out

2. **printStackTrace() Usage**
   - Line ~419: Using printStackTrace()
   - Severity: MAJOR

3. **Unused Private Fields**
   - Line ~28: `unusedConfig` field
   - Line ~29: `unusedCounter` field
   - Severity: MINOR
   - Rule: Unused fields should be removed

4. **Unused Variables**
   - Line ~95: Unused `temp` variable in loop
   - Line ~134: Unused `incomingUserIds` variable
   - Line ~306: Unused `totalShares` variable
   - Line ~364: Unused `temp` variable
   - Line ~466-467: Unused `debugString` and `counter` variables
   - Line ~510: Unused `temp` variable in lambda
   - Severity: MINOR

5. **Magic Numbers**
   - Line ~70: Magic number `1000` without constant
   - Severity: MINOR
   - Rule: Magic numbers should be replaced with named constants

6. **Unused Imports**
   - Line ~5: Unused `Colocation` import
   - Line ~12: Unused `AccessDeniedException` import
   - Severity: MINOR

---

## Code Smells

### ExpenseServiceImpl.java
1. **Code Duplication**
   - Methods `toDto()` (line ~427) and `mapToDTO()` (line ~488) are duplicated
   - Similar nested if logic in `updateExpense()` and `updateExpenseShares()`
   - Severity: MAJOR
   - Rule: Duplicated code should be refactored

2. **Cognitive Complexity**
   - Line ~139-168: Complex nested conditions in `updateExpense()`
   - Line ~189-219: Complex nested conditions in `updateExpenseShares()`
   - Severity: MAJOR
   - Rule: Methods should not be too complex

3. **Too Many Responsibilities**
   - `ExpenseServiceImpl` class has too many methods doing different things
   - Severity: MAJOR
   - Rule: Classes should follow Single Responsibility Principle

---

## Summary Statistics

### By Severity
- **BLOCKER**: 1 issue
- **CRITICAL**: 4 issues (Security)
- **MAJOR**: 30+ issues (Reliability, Performance, Maintainability)
- **MINOR**: 15+ issues (Code smells, unused variables)

### By Category
- **Security**: 4 critical issues
- **Reliability**: 8 major issues
- **Performance**: 10+ issues
- **Maintainability**: 20+ issues
- **Code Smells**: 5+ issues

---

## Expected SonarQube Rules Triggered

1. squid:S106 - Standard outputs should not be used directly
2. squid:S1148 - printStackTrace should not be called
3. squid:S2068 - Credentials should not be hard-coded
4. squid:S1313 - IP addresses should not be hardcoded
5. squid:S2077 - SQL queries should not be vulnerable to injection attacks
6. squid:S5122 - CORS should be properly configured
7. squid:S1481 - Unused local variables should be removed
8. squid:S1068 - Unused private fields should be removed
9. squid:S1144 - Unused private methods should be removed
10. squid:S2789 - Random objects should be reused
11. squid:S108 - Nested blocks of code should not be left empty
12. squid:S1854 - Dead stores should be removed
13. squid:S2583 - Conditions should not always evaluate to true or false
14. squid:S3776 - Cognitive Complexity should not be too high
15. squid:S1192 - String literals should not be duplicated
16. squid:S1141 - Try-catch blocks should not be nested
17. squid:S112 - Generic exceptions should not be thrown
18. squid:S1166 - Exception handlers should preserve the original exceptions
19. squid:S2259 - Null pointers should not be dereferenced
20. squid:S1488 - Local Variables should not be declared and then immediately returned

---

## How to Detect These Issues

1. **Run SonarQube Scanner:**
   ```bash
   mvn clean verify sonar:sonar
   ```

2. **Check SonarQube Dashboard:**
   - Navigate to SonarQube server
   - View the project dashboard
   - Check Security, Reliability, Maintainability tabs

3. **Filter by Severity:**
   - Critical and Blocker issues should appear first
   - Major issues will be shown with high priority

---

## Notes
- All issues are intentionally added for demonstration and testing purposes
- These issues should be fixed in production code
- Use this as a training example for code quality practices

