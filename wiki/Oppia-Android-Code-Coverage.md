## Table of Contents

- [Overview](#overview)
- [Understanding code coverage](#understanding-code-coverage)
- [Why is Code Coverage Important?](#why-is-code-coverage-important)
- [How to use the code coverage tool?](#how-to-use-the-code-coverage-tool)
    - [Code Coverage in CI Environment](#code-coverage-in-ci-environment)
    - [Code Coverage in local Development](#code-coverage-in-local-development)
        - [Generating Html Reports](#generating-html-reports)
- [Limitations of the code coverage tool](#limitations-of-the-code-coverage-tool)

# Overview
In software engineering, code coverage, also called test coverage, is a percentage measure of the degree to which the source code of a program is executed when a particular test suite is run. A program with high code coverage has more of its source code executed during testing, which suggests it has a lower chance of containing undetected software bugs compared to a program with low code coverage.

# Understanding code coverage
Code coverage measures the extent to which the code is tested by the automated tests. It indicates whether all parts of the code are examined to ensure they function correctly.

Let's take a look at how code coverage works with a simple Kotlin function.

Consider a Kotlin function designed to determine if a number is positive or negative:

```kotlin
fun checkSign(n: Int): String {
    return if (n >= 0) {
        "Positive" 
    } else { 
        "Negative"
    }
}
```

A test is created to verify that this function works correctly:
Please refer to the [Oppia Android testing documentation](https://github.com/oppia/oppia-android/wiki/Oppia-Android-Testing) to learn more about writing tests.

```
import org.junit.Test
import kotlin.test.assertEquals

class checkSignTest {
    @Test
    fun testCheckSign_withPositiveInteger_returnsPositive() {
        assertEquals("Positive", checkSign(4))
    }
}
```

This test checks whether passing a positive integer '+4' `checkSign(4)` correctly returns "Positive" as output.
However, it doesn’t test how the function handles negative numbers. This means that only a portion of the function’s behavior is being tested, leaving the handling of negative numbers unverified.

For thorough testing, the tests should also check how the function responds to negative inputs, this can be done by adding a test case by passing a negative integer to expect a return of "Negative" as output.

```diff
import org.junit.Test
import kotlin.test.assertEquals

class checkSignTest {
    @Test
    fun testCheckSign_withPositiveInteger_returnsPositive() {
        assertEquals("Positive", checkSign(4))
    }

+  @Test
+  fun testCheckSign_withNegativeInteger_returnsNegative() {
+      assertEquals("Negative", checkSign(-7))
+  }
}
```

# Why is code coverage important?
### 1. Minimizes the Risk of Bugs
High code coverage means your code is thoroughly tested. This helps find and fix bugs early.

### 2. Maintains Code Stability
When you make changes to your code, high coverage helps ensure that those changes don’t break existing functionality.

### 3. Facilitates Continuous Integration and Deployment
With high code coverage, you can confidently integrate and deploy code changes more frequently. Automated tests act as a safety check, allowing you to release updates automatically with reduced risk.

### 4. Encourages Comprehensive Testing
Striving for high code coverage encourages to write tests for all parts of the code, including edge cases and less common scenarios, while preparing the application to handle unexpected conditions gracefully.

### 5. Provides Confidence in Code Quality
Achieving a high percentage of code coverage gives confidence in the quality of the code. It also helps in maintaining a high standard of code quality over time.

#
**The Goal is to reach 100% Code Coverage.**
#
