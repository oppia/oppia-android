## Table of Contents

- [Writing Tests with Good Behavioral Coverage](##writing-tests-with-good-behavioral-coverage)
- [Why is Behavioral Coverage Necessary?](#why-is-behavioral-coverage-necessary)
- [Writing Effective Tests](#writing-effective-tests)
    - [Understand the Requirements](#1-understanding-the-requirements)
    - [Writing Clear and Descriptive Test Cases](#2-writing-clear-and-descriptive-test-cases)
    - [Covering Different Scenarios](#3-covering-different-scenarios)
    - [Exception Handling](#4-exception-handling)


# Writing Tests with Good Behavioral Coverage

Writing tests with good behavioral coverage involves ensuring that your code is tested thoroughly to meet its requirements and behave correctly in various scenarios. This approach aims to verify not only that your code works but also that it handles different inputs and edge cases as expected.

# Why is Behavioral Coverage Necessary?

Behavioral coverage is crucial because it helps ensure that your code functions correctly under all expected conditions, not just the typical use cases. By covering various scenarios, you can:

- Detect Bugs Early: Identify and fix issues before they affect users.
- Improve Code Quality: Ensure that the code meets its requirements and handles edge cases.
- Maintain Robustness: Ensure that changes or additions to the codebase do not introduce new issues.

For more details on testing methodologies specific to Oppia Android, please refer to the Oppia Testing Wiki.

# Writing Effective Tests

## 1. Understanding the Requirements

Before writing tests thoroughly review documentation, user stories, or specifications for the functionality you're testing.
**Example:**
If you're testing a function that checks if a number is positive or negative, make sure it returns "Positive" for numbers greater than or equal to zero and "Negative" for numbers less than zero.

```
@Test
fun testCheckSign_forPositiveInput_returnsPositive() {
    assertEquals("Positive", checkSign(5))
}

@Test
fun testCheckNumber_forNegativeInput_returnsNegative() {
    assertEquals("Negative", checkSign(-3))
}
```

# 2. Writing Clear and Descriptive Test Cases

Each test case should:

- Clearly describe the scenario and expected outcome.
- Use descriptive names for your test methods.

Naming Convention:
```
testAction_withOneCondition_withSecondCondition_hasExpectedOutcome
```

Example:
```
testCheckSign_forPositiveInput_returnsPositive()
testCheckSign_forNegativeInput_returnsNegative()
testCheckSign_forZeroInput_returnsNeitherPositiveNorNegative()
```

## 3. Covering Different Scenarios

Ensure tests cover:

- Positive Cases: Valid inputs that should pass.
- Negative Cases: Invalid inputs or edge cases.
- Boundary Cases: Inputs at the edge of acceptable ranges.

```
@Test
fun testCheckSign_forZeroInput_returnsNeitherPositiveNorNegative() {
  assertEquals("Neither Positive Nor Negative", checkSign(0)) // Boundary value
}

@Test
fun testCheckSign_forPositiveBoundaryInput_returnsPositive() {
  assertEquals("Positive", checkSign(1)) // Just above the boundary value
}

@Test
fun testCheckSign_forNegativeBoundaryInput_returnsNegative() {
  assertEquals("Negative", checkSign(-1)) // Just below the boundary value
}
```

## 4. Exception Handling

Exception Handling is a critical aspect of testing that ensures your code can handle and recover from error conditions gracefully.

- Ensure Error Conditions Are Managed Gracefully:
    - You need to verify that your application responds correctly when invalid or unexpected inputs are provided. For instance, if your function is designed to throw an exception when it receives null, you should test that it does so correctly.

- Verify Correct Exception Types:
    - Testing should confirm that the correct type of exception is thrown. This ensures that your error handling logic is specific and accurate.

```
@Test
fun testCheckSign_forNullInput_throwsIllegalArgumentException() {
    assertThrows<IllegalArgumentException> {
        checkSign(null)
    }
}
```

- Check for Proper Exception Messages:
    - It's important to ensure that exceptions include meaningful and informative messages that help diagnose issues. This can be tested by verifying the content of the exception message.

```
@Test
fun testCheckSign_forNullInput_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
        checkNumber(null)
    }
    assertThat(exception).contains("Value cannot be null")
}
```

- Ensure Exceptions Are Thrown at Correct Times:
    - Ensure that exceptions are thrown only under the right conditions and not during normal operations.

```
@Test
fun testCheckSign_forNullInput_throwsIllegalArgumentException() {
    // Normal case exception should not be thrown
    assertThrows<IllegalArgumentException> {
        checkSign(null)
    }
}

@Test
fun testCheckSign_forNullInput_throwsIllegalArgumentException() {
    // Null case exception should be thrown
    val exception = assertThrows<IllegalArgumentException> {
        checkNumber(null)
    }
    assertThat(exception).contains("Value cannot be null")
}
```

- Include Edge Cases:
    - Consider edge cases where exceptions might be thrown, such as boundary values or extreme input scenarios.

### WIP
