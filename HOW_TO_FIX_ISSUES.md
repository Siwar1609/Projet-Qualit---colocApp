# How to Fix SonarQube Issues in Expense Module

This document provides examples of how to fix the intentionally added code quality issues.

## Security Issues

### 1. Fix Hardcoded Credentials

**❌ Bad (Current):**
```java
private static final String DB_PASSWORD = "admin123";
private static final String DB_URL = "jdbc:mysql://localhost:3306/expenses";
private String apiKey = "sk-1234567890abcdef";
```

**✅ Good (Fixed):**
```java
// Use Spring's @Value annotation
@Value("${database.url}")
private String dbUrl;

@Value("${database.password}")
private String dbPassword;

@Value("${api.key}")
private String apiKey;
```

**application.properties:**
```properties
database.url=${DB_URL:jdbc:mysql://localhost:3306/expenses}
database.password=${DB_PASSWORD}
api.key=${API_KEY}
```

**Environment Variables:**
```bash
export DB_PASSWORD=your_secure_password
export API_KEY=your_secure_api_key
```

### 2. Fix SQL Injection

**❌ Bad (Current):**
```java
String query = "SELECT * FROM expenses WHERE colocation_id = " + colocationId;
PreparedStatement stmt = conn.prepareStatement(query);
```

**✅ Good (Fixed):**
```java
// Remove the entire direct SQL code and use JPA repository
// Or if you must use SQL, use PreparedStatement properly:
String query = "SELECT * FROM expenses WHERE colocation_id = ?";
PreparedStatement stmt = conn.prepareStatement(query);
stmt.setLong(1, colocationId);
```

**✅ Best (Use Spring Data JPA):**
```java
// Already exists in the code, just use it:
List<ExpenseDTO> expenses = expenseService.getExpensesForColocation(colocationId);
```

### 3. Fix CORS Misconfiguration

**❌ Bad (Current):**
```java
@CrossOrigin(origins = "*")
```

**✅ Good (Fixed):**
```java
@CrossOrigin(origins = "${allowed.origins}", maxAge = 3600)
```

**✅ Better (Global Configuration):**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${allowed.origins}")
    private String allowedOrigins;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
```

## Reliability Issues

### 4. Fix Method Returning Null

**❌ Bad (Current):**
```java
@Override
public ExpenseDTO getExpenseById(Long id) {
    System.out.println("Getting expense by id: " + id);
    return null;
}
```

**✅ Good (Fixed):**
```java
@Override
public ExpenseDTO getExpenseById(Long id) {
    logger.debug("Getting expense by id: {}", id);
    Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    return mapper.map(expense, ExpenseDTO.class);
}
```

### 5. Fix Empty Catch Blocks

**❌ Bad (Current):**
```java
try {
    dto = expenseService.getExpenseById(id);
} catch (Exception e) {
    // Empty catch block
}
```

**✅ Good (Fixed):**
```java
try {
    dto = expenseService.getExpenseById(id);
} catch (ResourceNotFoundException e) {
    logger.error("Expense not found: {}", id, e);
    throw e;
} catch (Exception e) {
    logger.error("Unexpected error getting expense: {}", id, e);
    throw new ServiceException("Error retrieving expense", e);
}
```

**✅ Better (Use Spring's Exception Handling):**
```java
// In Controller - let exceptions propagate
@GetMapping("/{id}")
public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable Long id) {
    ExpenseDTO dto = expenseService.getExpenseById(id);
    return ResponseEntity.ok(dto);
}

// Handle globally
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }
}
```

### 6. Fix Generic Exceptions

**❌ Bad (Current):**
```java
throw new RuntimeException("Expense not found");
throw new RuntimeException("Not authorized to delete this expense");
```

**✅ Good (Fixed):**
```java
// Create specific exceptions
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

// Use them
throw new ResourceNotFoundException("Expense not found");
throw new UnauthorizedException("Not authorized to delete this expense");
```

### 7. Fix Null Pointer Exceptions

**❌ Bad (Current):**
```java
dto.setColocationId(expense.getColocation().getId());
```

**✅ Good (Fixed):**
```java
if (expense.getColocation() != null) {
    dto.setColocationId(expense.getColocation().getId());
} else {
    logger.warn("Expense {} has no colocation", expense.getId());
    throw new IllegalStateException("Expense must have a colocation");
}
```

**✅ Better (Use Optional):**
```java
dto.setColocationId(
    Optional.ofNullable(expense.getColocation())
            .map(Colocation::getId)
            .orElseThrow(() -> new IllegalStateException("Expense must have a colocation"))
);
```

## Maintainability Issues

### 8. Replace System.out.println with Logger

**❌ Bad (Current):**
```java
System.out.println("Share param: " + share);
System.out.println("Creating expense: " + dto.getLabel());
```

**✅ Good (Fixed):**
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExpenseServiceImpl.class);
    
    // Use logger
    logger.debug("Share param: {}", share);
    logger.info("Creating expense: {}", dto.getLabel());
}
```

### 9. Replace printStackTrace with Logger

**❌ Bad (Current):**
```java
} catch (SQLException e) {
    e.printStackTrace();
}
```

**✅ Good (Fixed):**
```java
} catch (SQLException e) {
    logger.error("Database error: ", e);
    throw new DatabaseException("Error accessing database", e);
}
```

### 10. Remove Unused Variables and Fields

**❌ Bad (Current):**
```java
private String unusedConfig = "some-config";
private int unusedCounter = 0;

String debugString = "Debug mode enabled";
int counter = 0;
double temp = share.getAmount() * 1.0;
```

**✅ Good (Fixed):**
```java
// Simply remove them - they serve no purpose
```

### 11. Fix Magic Numbers

**❌ Bad (Current):**
```java
if (expenses.size() > 1000) {
    System.out.println("Warning: Large dataset");
}
```

**✅ Good (Fixed):**
```java
private static final int LARGE_DATASET_THRESHOLD = 1000;

if (expenses.size() > LARGE_DATASET_THRESHOLD) {
    logger.warn("Large dataset detected: {} expenses", expenses.size());
}
```

## Performance Issues

### 12. Fix Loading All Records

**❌ Bad (Current):**
```java
List<Expense> allExpenses = expenseRepository.findAll();
// Then filter in application
```

**✅ Good (Fixed):**
```java
// Create specific repository methods with filtering
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("SELECT e FROM Expense e JOIN e.shares s WHERE s.userId = :userId AND s.paid = :paid")
    List<Expense> findByUserIdAndPaidStatus(@Param("userId") String userId, 
                                            @Param("paid") boolean paid);
}

// Use it
List<Expense> expenses = expenseRepository.findByUserIdAndPaidStatus(userId, paid);
```

**✅ Better (Add Pagination):**
```java
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Page<Expense> findByUserIdAndPaidStatus(String userId, boolean paid, Pageable pageable);
}

// Use it
Pageable pageable = PageRequest.of(page, size, Sort.by("dateLimit").descending());
Page<Expense> expensePage = expenseRepository.findByUserIdAndPaidStatus(userId, paid, pageable);
```

### 13. Fix Unnecessary Object Creation

**❌ Bad (Current):**
```java
Random random = new Random();
random.nextInt();
```

**✅ Good (Fixed):**
```java
// If you need Random, make it a field
private static final Random random = new Random();

// But if you don't need it, just remove it
```

### 14. Fix Nested Loops

**❌ Bad (Current):**
```java
for (Expense expense : expenses) {
    for (ExpenseShare share : expense.getShares()) {
        // Process
        for (int i = 0; i < 10; i++) {
            double temp = share.getAmount() * 1.0;
        }
    }
}
```

**✅ Good (Fixed):**
```java
for (Expense expense : expenses) {
    for (ExpenseShare share : expense.getShares()) {
        // Process only once
        processShare(share);
    }
}
```

### 15. Fix Duplicate Iterations

**❌ Bad (Current):**
```java
double paid = expense.getShares().stream()
        .filter(ExpenseShare::getPaid)
        .mapToDouble(ExpenseShare::getAmount)
        .sum();

double unpaid = expense.getShares().stream()
        .filter(share -> !share.getPaid())
        .mapToDouble(ExpenseShare::getAmount)
        .sum();

long totalShares = expense.getShares().stream().count();
```

**✅ Good (Fixed):**
```java
// Iterate once and calculate all values
Map<Boolean, Double> sharesByPaidStatus = expense.getShares().stream()
        .collect(Collectors.groupingBy(
                ExpenseShare::getPaid,
                Collectors.summingDouble(ExpenseShare::getAmount)
        ));

double paid = sharesByPaidStatus.getOrDefault(true, 0.0);
double unpaid = sharesByPaidStatus.getOrDefault(false, 0.0);
long totalShares = expense.getShares().size(); // Don't use stream().count()
```

### 16. Fix Inefficient Stream Usage

**❌ Bad (Current):**
```java
long totalShares = expense.getShares().stream().count();

List<Expense> tempList = new ArrayList<>(expenses);
return tempList.stream().map(this::mapToDTO).collect(Collectors.toList());
```

**✅ Good (Fixed):**
```java
long totalShares = expense.getShares().size(); // Direct size() call

return expenses.stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList()); // No intermediate collection needed
```

## Code Smells

### 17. Fix Code Duplication

**❌ Bad (Current):**
```java
// Two methods doing the same thing
private ExpenseDTO toDto(Expense expense) { /* ... */ }
private ExpenseDTO mapToDTO(Expense expense) { /* ... */ }
```

**✅ Good (Fixed):**
```java
// Keep only one method and use it everywhere
private ExpenseDTO toDto(Expense expense) {
    if (expense == null) return null;
    
    ExpenseDTO dto = new ExpenseDTO();
    dto.setId(expense.getId());
    dto.setLabel(expense.getLabel());
    // ... other fields
    
    if (expense.getColocation() != null) {
        dto.setColocationId(expense.getColocation().getId());
    }
    
    List<ExpenseShareDTO> shareDTOs = expense.getShares().stream()
            .map(this::shareToDto)
            .collect(Collectors.toList());
    dto.setShares(shareDTOs);
    
    return dto;
}

// Update all references to use toDto
```

**✅ Better (Use MapStruct):**
```java
@Mapper(componentModel = "spring")
public interface ExpenseMapper {
    ExpenseDTO toDto(Expense expense);
    List<ExpenseDTO> toDtoList(List<Expense> expenses);
    ExpenseShareDTO toDto(ExpenseShare share);
}
```

### 18. Fix High Cognitive Complexity

**❌ Bad (Current):**
```java
for (ExpenseShareDTO shareDTO : dto.getShares()) {
    if (existingShare != null) {
        if (shareDTO.getPaid()) {
            if (shareDTO.getAmount() != null && shareDTO.getAmount() > 0) {
                if (existingShare.getDatePaid() == null) {
                    existingShare.setDatePaid(LocalDate.now());
                }
            }
        }
    }
}
```

**✅ Good (Fixed):**
```java
for (ExpenseShareDTO shareDTO : dto.getShares()) {
    if (existingShare == null) continue;
    
    updateShareFromDto(existingShare, shareDTO);
}

private void updateShareFromDto(ExpenseShare share, ExpenseShareDTO dto) {
    share.setUserEmail(dto.getUserEmail());
    share.setAmount(dto.getAmount());
    share.setPaid(dto.getPaid());
    
    if (shouldSetDatePaid(share, dto)) {
        share.setDatePaid(LocalDate.now());
    }
}

private boolean shouldSetDatePaid(ExpenseShare share, ExpenseShareDTO dto) {
    return dto.getPaid() 
            && dto.getAmount() != null 
            && dto.getAmount() > 0 
            && share.getDatePaid() == null;
}
```

## Summary Checklist

- [ ] Replace all hardcoded credentials with configuration
- [ ] Fix SQL injection vulnerabilities
- [ ] Configure CORS properly
- [ ] Replace System.out.println with logger
- [ ] Replace printStackTrace with logger
- [ ] Remove unused variables and fields
- [ ] Create specific exception classes
- [ ] Add null checks or use Optional
- [ ] Use database-level filtering instead of application-level
- [ ] Add pagination for large datasets
- [ ] Remove duplicate code
- [ ] Reduce cognitive complexity
- [ ] Fix inefficient loops and streams
- [ ] Use specific exceptions instead of generic ones
- [ ] Remove empty catch blocks

## Testing After Fixes

```bash
# 1. Compile the code
./mvnw.cmd clean compile

# 2. Run tests
./mvnw.cmd test

# 3. Run SonarQube analysis
./mvnw.cmd sonar:sonar -Dsonar.login=YOUR_TOKEN

# 4. Check SonarQube dashboard - all issues should be resolved!
```

