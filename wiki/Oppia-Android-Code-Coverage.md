## Table of Contents

- [Overview](#overview)
- [Understanding Code Coverage](#understanding-code-coverage)
- [Why is Code Coverage Important?](#why-is-code-coverage-important)
- [How to use the code coverage tool?](#how-to-use-the-code-coverage-tool)
    - [Continuous Itegration Checks on Pull Request](#1-continuous-integration-checks-on-pull-requests)
        - [Understanding the CI Coverage Report](#11-understanding-the-ci-coverage-report)
    - [Local Command Line Interface Tools](#2-local-command-line-interface-cli-tools)
        - [Understanding the Html Reports](#21-understanding-the-ci-coverage-report)
- [Increasing Code Coverage Metrics](#increasing-code-coverage-metrics)
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

#
>The ultimate Goal is to reach 100% Code Coverage.
#

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

# How to use the code coverage tool?

Oppia supports code coverage analysis through two primary methods:
1. Continuous Integration (CI) Checks on Pull Requests (PRs)
2. Local Command-Line Interface (CLI) Tools

## 1. Continuous Integration Checks on Pull Requests

Once a pull request (PR) is created, the Continuous Integration (CI) system automatically triggers a coverage check on all modified files.

Note: The coverage checks are initiated only after the unit tests have successfully passed.

![Screenshot (1596)](https://github.com/user-attachments/assets/5bacbb09-cf75-4811-b24c-84692f34916e)

Following the completion of the coverage analysis, a detailed coverage analysis report will be uploaded as a comment on your PR.

![image](https://github.com/user-attachments/assets/f0b5ae9d-9e64-4431-b493-c35eae94d2c1)

## 1.1 Understanding the CI Coverage Report


Let's look at a sample coverage report to understand the details it provides.

## Coverage Report

### Results
Number of files assessed: 5 <br>
Overall Coverage: **94.26%** <br>
Coverage Analysis: **FAIL** :x: <br>
##

### Failure Cases

| File | Failure Reason | Status |
|------|----------------|:------:|
| File.kt | No appropriate test file found for File.kt. | :x: |

### Failing coverage

| File | Coverage | Lines Hit | Status | Min Required |
|------|:--------:|----------:|:------:|:------------:|
| <details><summary>Fail1.kt</summary>scripts/src/java/org/oppia/android/scripts/coverage/Fail1.kt</details> | 51.38% | 148 / 288 | :x: | 70% |
| <details><summary>Fail2.kt</summary>utility/src/main/java/org/oppia/android/util/Fail2.kt</details> | 77.78% | 7 / 9 | :x: | 80% _*_ |
| <details><summary>Fail3.kt</summary>domain/src/main/java/org/oppia/android/domain/classify/Fail3.kt</details> | 10.00% | 1 / 10 | :x: | 30% _*_ |

>**_*_** represents tests with custom overridden pass/fail coverage thresholds

### Passing coverage

<details>
<summary>Files with passing code coverage</summary><br>

| File | Coverage | Lines Hit | Status | Min Required |
|------|:--------:|----------:|:------:|:------------:|
| <details><summary>MathTokenizer.kt</summary>utility/src/main/java/org/oppia/android/util/math/MathTokenizer.kt</details> | 94.26% | 197 / 209 | :white_check_mark: | 70% |
</details>

### Exempted coverage
<details><summary>Files exempted from coverage</summary> <br>

| File | Failure Reason |
|------|----------------|
| <details><summary>TestExemptedFile.kt</summary>app/src/main/java/org/oppia/android/app/activity/TestExemptedFile.kt</details> | This file is exempted from having a test file; skipping coverage check. |
| <details><summary>SourceIncompatible.kt</summary>app/src/main/java/org/oppia/android/app/activity/SourceIncompatible.kt</details> | This file is incompatible with code coverage tooling; skipping coverage check. |

</details>

#

The coverage report header provides an overview of the coverage analysis:

### 1. Report Overview Section

#

### Results
Number of files assessed: 5 <br>
Overall Coverage: **94.26%** <br>
Coverage Analysis: **FAIL** :x: <br>

#

- **Number of Files Assessed:** <br>This displays the number of Kotlin files included in the coverage analysis. Files with other extensions, such as .xml, .json, or .md, are excluded from this analysis.

- **Overall Coverage:** <br>This reflects the average code coverage percentage for the files modified in the current pull request. It provides a snapshot of how thoroughly the new code is tested.

- **Coverage Analysis:** <br>This indicates whether the pull request passes the coverage analysis. A check mark denotes a passing result, while an X mark indicates a failure.

### 2. Report Details Section

#

**2.a. Failure Cases**

### Failure Cases

| File | Failure Reason | Status |
|------|----------------|:------:|
| File.kt | No appropriate test file found for File.kt. | :x: |

#

This section lists files that failed to acquire coverage data. Failures may occur due to various reasons, including:

- The absence of an appropriate test file for the respective source file, preventing coverage analysis.
- Bazel failing to collect coverage data for the source file.
- Bazel lacking a reference to the required source file in the collected data.
- Other potential reasons are still under exploration.
  The table has two columns:

1. **File:** Displays the file for which the error occurred (Clicking the drop-down reveals the file's path in the codebase).
2. **Failure Reason:** Describes the reason for the failure.

Note: If this table or section is not present in the coverage report, it indicates that no changes exhibited failure

#

**2.b. Failing coverages**

### Failing coverage

| File | Coverage | Lines Hit | Status | Min Required |
|------|:--------:|----------:|:------:|:------------:|
| <details><summary>Fail1.kt</summary>scripts/src/java/org/oppia/android/scripts/coverage/Fail1.kt</details> | 51.38% | 148 / 288 | :x: | 70% |
| <details><summary>Fail2.kt</summary>utility/src/main/java/org/oppia/android/util/Fail2.kt</details> | 77.78% | 7 / 9 | :x: | 80% _*_ |
| <details><summary>Fail3.kt</summary>domain/src/main/java/org/oppia/android/domain/classify/Fail3.kt</details> | 10.00% | 1 / 10 | :x: | 30% _*_ |

>**_*_** represents tests with custom overridden pass/fail coverage thresholds

#

This section highlights files that have failed to meet the minimum coverage percentage. Any file that does not meet the minimum threshold percentage is considered to have a failing status. The minimum threshold is set to a standard value (currently as 70%) and displayed alongside the status for each file.

Files with overridden coverage thresholds are indicated by an asterisk (*) and include the specific required percentage.

Note: Files listed may have met the standard minimum threshold but still fail if they do not achieve the required overridden coverage percentage. These files are strictly obligated to meet their specific required percentage and do not consider the minimum threshold.

For a complete list of files with overridden coverage thresholds, refer to the [test_file_exemptions.textproto](https://github.com/oppia/oppia-android/blob/develop/scripts/assets/test_file_exemptions.textproto) file.

The data presented includes:

1. **File:** Name of the file that failed to meet the coverage threshold (Clicking the drop-down reveals the file's path in the codebase).
2. **Coverage Percentage:** Percentage of coverage achieved for the file.
3. **Lines Covered:** Number of lines covered by the test cases, shown in "lines hit / lines found" format.
4. **Status:** Indicates whether the coverage status is a pass or fail.
5. **Required Percentage:** Minimum percentage required to pass the coverage check for the file.

#

**2.c. Passing coverages**

### Passing coverage

<details open>
<summary>Files with passing code coverage</summary><br>

| File | Coverage | Lines Hit | Status | Min Required |
|------|:--------:|----------:|:------:|:------------:|
| <details><summary>Pass.kt</summary>utility/src/main/java/org/oppia/android/util/Pass.kt</details> | 94.26% | 197 / 209 | :white_check_mark: | 70% |
</details>

#

This section highlights files that have met the minimum coverage requirements. Files with overridden coverage thresholds are marked with an asterisk (*), and their specific required percentages are provided.

These files have meet their overridden minimum coverage to achieve a passing status.

For a complete list of files with overridden coverage thresholds, refer to the [test_file_exemptions.textproto](https://github.com/oppia/oppia-android/blob/develop/scripts/assets/test_file_exemptions.textproto) file.

The data presented includes:

1. **File:** Name of the file that met the coverage threshold (Clicking the drop-down reveals the file's path in the codebase).
2. **Coverage Percentage:** Percentage of coverage achieved for the file.
3. **Lines Covered:** Number of lines covered by the test cases, shown in "lines hit / lines found" format.
4. **Status:** Indicates whether the coverage status is a pass or fail.
5. **Required Percentage:** Minimum percentage required to pass the coverage check for the file.

**2.d. Exempted coverages**

### Exempted coverage
<details open><summary>Files exempted from coverage</summary> <br>

| File | Failure Reason |
|------|----------------|
| <details><summary>TestExemptedFile.kt</summary>app/src/main/java/org/oppia/android/app/activity/TestExemptedFile.kt</details> | This file is exempted from having a test file; skipping coverage check. |
| <details><summary>SourceIncompatible.kt</summary>app/src/main/java/org/oppia/android/app/activity/SourceIncompatible.kt</details> | This file is incompatible with code coverage tooling; skipping coverage check. |

</details>

#

Certain files are exempt from coverage checks. These exemptions include:

1. **Test File Exemptions:** Files that do not have corresponding test files are exempt from coverage checks. Since no test files are available for these sources, coverage analysis cannot be performed, and these files are therefore skipped.

2. **Source File Incompatibility Exemptions:** Some files are currently incompatible with Bazel coverage execution ([see tracking issue #5481](https://github.com/oppia/oppia-android/issues/5481)) and are temporarily excluded from coverage checks.

You can find the complete list of exemptions in this file: [test_file_exemptions.textproto](https://github.com/oppia/oppia-android/blob/develop/scripts/assets/test_file_exemptions.textproto)

This section appears at the bottom of the report, as a drop-down. It includes a table listing the exemptions as,

1. **File:** Displays the file name that is exempted (Clicking the drop-down reveals the file's path in the codebase).
2. **Failure Reason:** Describes the specific reason for its exemption.

#

With this report, you can review the coverage percentages for various files and identify which files pass or fail the coverage check.

#

## 2. Local Command-Line Interface (CLI) Tools

While the CI check provides an overview of coverage, you might want to visualize how tests cover specific files locally, including which lines are covered and which are not. For this, Oppia's local command-line coverage tooling is useful.

Note: Follow these [Bazel setup instructions](https://github.com/oppia/oppia-android/wiki/Oppia-Bazel-Setup-Instructions) if Bazel isn't yet set up on your device:

Oppia Android allows you to generate coverage reports in HTML format using the command:

### Run Coverage

```
bazel run //scripts:run_coverage -- <path_to_root> <list_of_relative_path_to_files>
```

- <path_to_root>: Your root directory.
- <list_of_relative_path_to_files>: Files you want to generate coverage reports for.

For example, to analyze coverage for the file MathTokenizer.kt, use the relative path:

```
bazel run //scripts:run_coverage -- $(pwd) utility/src/main/java/org/oppia/android/util/math/MathTokenizer.kt
```

By default, this will generate an HTML report in the coverage_reports directory. For the given file, the report will be saved as **coverage_reports/utility/src/main/java/org/oppia/android/util/math/MathTokenizer/coverage.html**

A list of files can be provided as an input to generate coverage reports for all the files.

```
bazel run //scripts:run_coverage -- $(pwd) 
utility/src/main/java/org/oppia/android/util/math/MathTokenizer.kt 
utility/src/main/java/org/oppia/android/util/parser/math/MathModel.kt
```

### Process Timeout

The default wait time for a process to complete is 300 seconds (5 minutes). If the process does not finish within this period, you may encounter a "Process Did Not Finish Within Intended Time" error. To extend the wait time, you can modify the timeout setting by adding the flag --processTimeout=15 (The Time unit is minutes).

```
bazel run //scripts:run_coverage -- $(pwd)
utility/src/main/java/org/oppia/android/util/parser/math/MathModel.kt --processTimeout=15
```

## 2.1 Understanding the CI Coverage Report

Let's examine the coverage.html report located at coverage_reports/utility/src/main/java/org/oppia/android/util/math/MathTokenizer/coverage.html to understand its contents.

**Header Section:** At the top of the report, you'll find information including:

![image](https://github.com/user-attachments/assets/08c4f0a0-51b5-4d8e-a2f3-ff13d8b6cb2f)

#

- **File Path:** The relative path of the file to the oppia-android codebase.
- **Coverage Percentage:** The percentage of code covered by tests.
- **Line Coverage Data:** Details on the coverage of individual lines.
- **Main Content:**

Visual Coverage Representation: The report visually highlights which lines are covered and which are not.

![image](https://github.com/user-attachments/assets/0e36fde3-639b-4874-b809-59d33827388d)


#

- **Red Lines:** Indicate lines not covered by test cases.
- **Green Lines:** Represent lines that are covered by test cases.

These generated html reports are be used to identify areas of your code that may need additional testing.

## Increasing Code Coverage Metrics

While figuring out which lines are covered and which are not, you can increase code coverage by ensuring that each line is thoroughly tested. To enhance code coverage effectiveness, locate the test files associated with the source file (typically found in the same package but within the test directories).

Note: Some files in the app module may have multiple test files, located in the sharedTest and test packages, all testing a single source file. (eg: The file StateFragment.kt has 2 test files StateFragmentTest.kt under sharedTest and StateFragmentLocalTest.kt under test folder)

By identifying and adding appropriate test scenarios to cover previously uncovered lines, you will boost the coverage percentage and improve the overall quality and reliability of your code.

## Limitations of the code coverage tool


