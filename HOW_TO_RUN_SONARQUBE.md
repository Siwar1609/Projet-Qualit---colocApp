# How to Run SonarQube Analysis on Expense Module

This guide explains how to detect the intentionally added code quality issues using SonarQube.

## Prerequisites

1. **SonarQube Server Running**
   - Local: http://localhost:9000
   - Or use SonarCloud: https://sonarcloud.io

2. **Maven Installation**
   - The project uses Maven wrapper (mvnw.cmd)

3. **SonarQube Token**
   - Generate from SonarQube: User > My Account > Security > Generate Token

## Method 1: Using Maven with Local SonarQube

### Step 1: Start SonarQube Server
```bash
# If using Docker
docker run -d --name sonarqube -p 9000:9000 sonarqube:latest

# Wait for startup, then access http://localhost:9000
# Default credentials: admin/admin
```

### Step 2: Run SonarQube Analysis
```bash
cd "C:\Users\chedl\Desktop\Quality PFA\backend"

# Run analysis
./mvnw.cmd clean verify sonar:sonar \
  -Dsonar.projectKey=pfa-backend \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=YOUR_TOKEN_HERE
```

## Method 2: Using SonarCloud

```bash
cd "C:\Users\chedl\Desktop\Quality PFA\backend"

./mvnw.cmd clean verify sonar:sonar \
  -Dsonar.projectKey=your-org_pfa-backend \
  -Dsonar.organization=your-org \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.login=YOUR_SONARCLOUD_TOKEN
```

## Method 3: Add to pom.xml

Add SonarQube properties to your `pom.xml`:

```xml
<properties>
    <sonar.projectKey>pfa-backend</sonar.projectKey>
    <sonar.host.url>http://localhost:9000</sonar.host.url>
    <sonar.java.coveragePlugin>jacoco</sonar.java.coveragePlugin>
    <sonar.sources>src/main/java</sonar.sources>
    <sonar.tests>src/test/java</sonar.tests>
</properties>
```

Then run:
```bash
./mvnw.cmd clean verify sonar:sonar -Dsonar.login=YOUR_TOKEN
```

## Expected Results

After running the analysis, you should see:

### Security Issues (4 Critical)
- Hardcoded credentials (DB_PASSWORD)
- Hardcoded API key (apiKey)
- SQL injection vulnerability
- CORS misconfiguration

### Reliability Issues (8+ Major)
- Method returning null (getExpenseById)
- Empty catch blocks
- Generic exceptions
- Potential null pointer exceptions
- No input validation

### Maintainability Issues (20+ Major)
- System.out.println instead of logger
- printStackTrace usage
- Unused variables and fields
- Unused imports
- Magic numbers

### Performance Issues (10+ Major)
- Loading all records without pagination
- Unnecessary object creation
- Inefficient nested loops
- Duplicate iterations
- Application-level filtering

### Code Smells (5+)
- Code duplication (toDto vs mapToDTO)
- High cognitive complexity
- Nested conditions

## Viewing Results

1. **Open SonarQube Dashboard**
   - Navigate to http://localhost:9000 (or your SonarQube URL)
   - Click on your project: `pfa-backend`

2. **Check Issues Tab**
   - Filter by file: `ExpenseController.java` or `ExpenseServiceImpl.java`
   - Filter by severity: Blocker, Critical, Major
   - Filter by type: Bug, Vulnerability, Code Smell

3. **View Metrics**
   - Security Rating
   - Reliability Rating
   - Maintainability Rating
   - Technical Debt
   - Code Coverage

## Specific Issues to Look For

### In ExpenseController.java:
- Line ~31: Hardcoded password
- Line ~33: Hardcoded API key
- Line ~76: SQL injection
- Line ~53, 58, 80: System.out.println
- Line ~80: printStackTrace
- Line ~122: Empty catch block

### In ExpenseServiceImpl.java:
- Line ~28-29: Unused fields
- Line ~258: Method returning null
- Line ~226, 264, 290, 331: findAll() performance issues
- Line ~40, 73, 165, etc.: Multiple System.out.println
- Line ~419: printStackTrace
- Line ~427 vs ~488: Code duplication

## Generate HTML Report (Optional)

```bash
# Generate SonarQube report locally
./mvnw.cmd sonar:sonar -Dsonar.analysis.mode=preview \
  -Dsonar.issuesReport.html.enable=true
```

## CI/CD Integration

### GitHub Actions Example:
```yaml
- name: SonarQube Scan
  run: |
    mvn clean verify sonar:sonar \
      -Dsonar.projectKey=pfa-backend \
      -Dsonar.host.url=${{ secrets.SONAR_HOST_URL }} \
      -Dsonar.login=${{ secrets.SONAR_TOKEN }}
```

### GitLab CI Example:
```yaml
sonarqube-check:
  stage: test
  script:
    - mvn clean verify sonar:sonar
      -Dsonar.projectKey=pfa-backend
      -Dsonar.host.url=${SONAR_HOST_URL}
      -Dsonar.login=${SONAR_TOKEN}
```

## Fixing Issues Guide

See `SONARQUBE_ISSUES_SUMMARY.md` for detailed information about each issue and how to fix them.

## Common Problems

### 1. Connection Refused
- Make sure SonarQube server is running
- Check the URL is correct (http://localhost:9000)

### 2. Authentication Error
- Verify your token is correct
- Token might have expired, generate a new one

### 3. Project Not Found
- Create the project in SonarQube first
- Or use `-Dsonar.projectKey` to create automatically

### 4. No Issues Detected
- Make sure analysis completed successfully
- Check SonarQube server logs
- Verify the correct files were scanned

## References

- [SonarQube Documentation](https://docs.sonarqube.org/)
- [SonarQube Rules for Java](https://rules.sonarsource.com/java)
- [Maven SonarQube Plugin](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-maven/)

