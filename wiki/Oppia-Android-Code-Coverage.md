## Table of Contents

- [Overview](#overview)
- [Understanding Code Coverage](#understanding-code-coverage)
- [Why is Code Coverage Important?](#why-is-code-coverage-important)
- [How to use the code coverage tool?](#how-to-use-the-code-coverage-tool)
    - [1. Continuous Integration Checks on Pull Requests](#1-continuous-integration-checks-on-pull-requests)
        - [1.1 Understanding the CI Coverage Report](#11-understanding-the-ci-coverage-report)
    - [2. Local Command Line Interface (CLI) Tools](#2-local-command-line-interface-cli-tools)
        - [2.1 Understanding the CI Coverage Report](#21-understanding-the-ci-coverage-report)
- [Increasing Code Coverage Metrics](#increasing-code-coverage-metrics)
- [Unit-Centric Coverage Philosophy](#unit-centric-coverage-philosophy)
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
import com.google.common.truth.Truth.assertThat

class checkSignTest {
  @Test
  fun testCheckSign_withPositiveInteger_returnsPositive() {
    assertThat(checkSign(4)).isEqualTo("Positive")
  }
}
```

This test checks whether passing a positive integer '+4' `checkSign(4)` correctly returns "Positive" as output.
However, it doesn’t test how the function handles negative numbers. This means that only a portion of the function’s behavior is being tested, leaving the handling of negative numbers unverified.

For thorough testing, the tests should also check how the function responds to negative inputs, this can be done by adding a test case by passing a negative integer to expect a return of "Negative" as output.

```diff
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class checkSignTest {
  @Test
  fun testCheckSign_withPositiveInteger_returnsPositive() {
    assertThat(checkSign(4)).isEqualTo("Positive")
  }

+ @Test
+ fun testCheckSign_withNegativeInteger_returnsNegative() {
+   assertThat(checkSign(-7)).isEqualTo("Negative")
+ }
}
```

# Why is code coverage important?
- **Minimizes the Risk of Bugs:**
  High code coverage means your code is thoroughly tested. This helps find and fix bugs early.

- **Maintains Code Stability:**
  When you make changes to your code, high coverage helps ensure that those changes don’t break existing functionality.

- **Facilitates Continuous Integration and Deployment:**
  With high code coverage, you can confidently integrate and deploy code changes more frequently. Automated tests act as a safety check, allowing you to release updates automatically with reduced risk.

- **Encourages Comprehensive Testing:**
  Striving for high code coverage encourages to write tests for all parts of the code, including edge cases and less common scenarios, while preparing the application to handle unexpected conditions gracefully.

- **Provides Confidence in Code Quality:**
  Achieving a high percentage of code coverage gives confidence in the quality of the code. It also helps in maintaining a high standard of code quality over time.

# How to use the code coverage tool?

Oppia Android supports code coverage analysis through two primary methods:
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
| <details><summary>Pass.kt</summary>utility/src/main/java/org/oppia/android/util/math/Pass.kt</details> | 94.26% | 197 / 209 | :white_check_mark: | 70% |
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

  The table has three columns:

1. **File:** Displays the file for which the error occurred (Clicking the drop-down reveals the file's path in the codebase).
2. **Failure Reason:** Describes the reason for the failure.
3. **Status:** Indicates that the coverage status is a failure.

Note: If this table or section is not present in the coverage report, it indicates that no changes exhibited failure.

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

This section highlights files that have failed to meet the minimum coverage percentage. Any file that does not meet the minimum threshold percentage is considered to have a failing status. The minimum threshold is configured to a standard value, as specified in [CoverageReporter.kt](https://github.com/oppia/oppia-android/blob/develop/scripts/src/java/org/oppia/android/scripts/coverage/reporter/CoverageReporter.kt), and this value is shown alongside the status for each file.

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

1. **Test File Exemptions:** Files that are exempted from having corresponding test files are also exempted from coverage checks. Since no test files are available for these sources, coverage analysis cannot be performed, and these files are therefore skipped.

2. **Source File Incompatibility Exemptions:** Some files are currently incompatible with Bazel coverage execution (see tracking issue [#5481](https://github.com/oppia/oppia-android/issues/5481)) and are temporarily excluded from coverage checks.

You can find the complete list of exemptions in this file: [test_file_exemptions.textproto](https://github.com/oppia/oppia-android/blob/develop/scripts/assets/test_file_exemptions.textproto)

This section appears at the bottom of the report, as a drop-down. It includes a table listing the exemptions with columns:

1. **File:** Displays the file name that is exempted (Clicking the drop-down reveals the file's path in the codebase).
2. **Failure Reason:** Describes the specific reason for its exemption.

With this report, you can review the coverage percentages for various files and identify which files pass or fail the coverage check.


## 2. Local Command-Line Interface (CLI) Tools

While the CI check provides an overview of coverage, you might want to visualize how tests cover specific files locally, including which lines are covered and which are not. For this, Oppia's local command-line coverage tooling is useful.

Note: Follow these [Bazel setup instructions](https://github.com/oppia/oppia-android/wiki/Oppia-Bazel-Setup-Instructions) if Bazel isn't yet set up in your local development environment.

Oppia Android allows you to generate coverage reports in HTML format using the command:

### Run Coverage

```sh
bazel run //scripts:run_coverage -- <path_to_root> <list_of_relative_path_to_files>
```

- <path_to_root>: Your root directory.
- <list_of_relative_path_to_files>: Files you want to generate coverage reports for.

To get the relative path of a file:

1. Navigate to the Project view on the left-hand side in Android Studio.
2. Locate the file to analyze Code Coverage for.
3. Right click the file and select Copy Path. To get the path relative to the root.

Alternatively, the coverage report itself provides the relative paths. You can reveal this information by clicking on the drop-down that precedes the file name in the report.

| File | Coverage | Lines Hit | Status | Min Required |
|------|:--------:|----------:|:------:|:------------:|
| <details open><summary>MathTokenizer.kt</summary>utility/src/main/java/org/oppia/android/util/math/MathTokenizer.kt</details> | 94.26% | 197 / 209 | :white_check_mark: | 70% |

To analyze coverage for the file MathTokenizer.kt, use the relative path:

```sh
bazel run //scripts:run_coverage -- $(pwd) utility/src/main/java/org/oppia/android/util/math/MathTokenizer.kt
```

By default, this will generate an HTML report in the coverage_reports directory. For the given file, the report will be saved as **coverage_reports/utility/src/main/java/org/oppia/android/util/math/MathTokenizer/coverage.html**

A list of files can be provided as an input to generate coverage reports for each of the provided the files. An example of this is:

```sh
bazel run //scripts:run_coverage -- $(pwd) 
utility/src/main/java/org/oppia/android/util/math/MathTokenizer.kt 
utility/src/main/java/org/oppia/android/util/parser/math/MathModel.kt
```

### Process Timeout

The default wait time for a process to complete is 300 seconds (5 minutes). If the process does not finish within this period, you may encounter a "Process Did Not Finish Within Intended Time" error. To extend the wait time, you can modify the timeout setting by adding the flag --processTimeout=15 (The Time unit is minutes).

```sh
bazel run //scripts:run_coverage -- $(pwd)
utility/src/main/java/org/oppia/android/util/parser/math/MathModel.kt --processTimeout=15
```

## 2.1 Understanding the CI Coverage Report

Let's examine the example coverage.html report (generated using the sample command in the previous section) to understand its contents. The report should be located at ``coverage_reports/utility/src/main/java/org/oppia/android/util/math/MathTokenizer/coverage.html``.

The report is divided into two sections:
- Coverage Overview
- Line-by-Line Coverage Breakdown

#

### Coverage Overview

![image](https://github.com/user-attachments/assets/e366af1f-2f64-4f14-a7e4-f7a592688c6c)

In the top section of the report, you'll find the following details:

- **File Path:** The relative path of the file to the oppia-android codebase.
- **Coverage Percentage:** The percentage of code covered by tests.
- **Line Coverage Data:** Details on the coverage of individual lines.
- **Main Content:**

#

### Line-by-Line Coverage Breakdown

The subsequent section of the report visually represents the coverage status of each line of the source code.

- **Red Lines:** Indicate lines not covered by test cases.
- **Green Lines:** Represent lines that are covered by test cases.

![image](https://github.com/user-attachments/assets/c1d55cf8-94bf-4ab5-a02b-c95264e854db)

These generated html reports are be used to identify areas of your code that may need additional testing.

## Increasing Code Coverage Metrics

To improve code coverage, start by identifying which lines are covered and which are not. To effectively increase coverage, trace the uncovered lines back to their calling functions, and ensure these are executed and tested in your test suites. The corresponding test files for the source code are usually located in the same package within the test directories. Add tests that cover all relevant behavioral scenarios for the uncovered lines to achieve comprehensive testing.

For more guidance on best practices, refer to the [Writing Tests with Good Behavioral Coverage](https://github.com/oppia/oppia-android/wiki/Writing-Tests-With-Good-Behavioral-Coverage) wiki page.

Note: Some files in the app module may have multiple test files, located in the sharedTest and test packages, all testing a single source file. For example: [StateFragment.kt](https://github.com/oppia/oppia-android/blob/develop/app/src/main/java/org/oppia/android/app/player/state/StateFragment.kt) has 2 test files [StateFragmentTest.kt](https://github.com/oppia/oppia-android/blob/develop/app/src/sharedTest/java/org/oppia/android/app/player/state/StateFragmentTest.kt) under ``app/src/sharedTest`` and [StateFragmentLocalTest.kt](https://github.com/oppia/oppia-android/blob/develop/app/src/test/java/org/oppia/android/app/player/state/StateFragmentLocalTest.kt) under ``app/src/test``.

## Unit-Centric Coverage Philosophy

Oppia Android's approach to code coverage is focused on analyzing each source unit within the context of its own tests. This means that only the tests specifically written for a particular source file are considered when determining that file's coverage. Incidental coverage—when a source file is indirectly tested through another test file—is not counted towards the coverage metrics of the original file.

This approach ensures that coverage percentages are more meaningful, as they reflect deliberate and thorough testing of each source file. This makes it essential to write comprehensive tests for every source unit to achieve accurate and reliable coverage metrics.

### Example Scenario

To clarify the concept, consider the following example with source files and their corresponding test files:

| **Source File**              | **Tested By** | **Test File**                  |
|------------------------------|:-------------:|--------------------------------|
| `OppiaCommentBot.kt`         |       ->      |`OppiaCommentBotTest.kt`        |
| `UploadReviewComment.kt`     |       ->      |`UploadReviewCommentTest.kt`    |

**OppiaCommentBot.kt (Source file):**

It manages the logic for uploading comments and interacting with the Oppia project’s comment system.

```kotlin
class OppiaCommentBot {
  fun uploadComment(comment: String): String {
    // Uploads a comment to a repository
    return "Comment uploaded: $comment"
  }
}
```

**OppiaCommentBotTest.kt (Test file for OppiaCommentBot):**

```kotlin
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class OppiaCommentBotTest {
  @Test
  fun testUploadComment_returnsExpectedMessage() {
    val bot = OppiaCommentBot()
    val result = bot.uploadComment("Great job!")
    assertThat(result).isEqualTo("Comment uploaded: Great job!")
  }
}
```

**UploadReviewComment.kt (Another Source file):**

It handles the creation and submission of review comments, utilizing features from OppiaCommentBot.kt.

```kotlin
class UploadReviewComment {
  fun createAndUploadReview(comment: String) {
    // generates review
    val bot = OppiaCommentBot()
    bot.uploadComment(review)
  }
}
```

**UploadReviewCommentTest.kt (Test file for UploadReviewComment):**

```kotlin
import org.junit.Test

class UploadReviewCommentTest {
  @Test
  fun testCreateAndUploadReview_callsUploadComment() {
    val review = UploadReviewComment()
    review.createAndUploadReview("Needs revision")
  }
}
```

In this example, the `OppiaCommentBot` class is used in both `UploadReviewComment.kt` and `OppiaCommentBotTest.kt`. However, the code coverage tool only considers the tests in `OppiaCommentBotTest.kt` when calculating the coverage for `OppiaCommentBot.kt`. Tests in `UploadReviewCommentTest.kt` that indirectly call `OppiaCommentBot` do not count toward its coverage.

It’s essential to ensure that each source file is directly tested by its own corresponding test file to accurately reflect the unit's coverage. This approach helps maintain a high standard of code quality and ensures that the coverage metrics genuinely represent the code’s reliability.

## Limitations of the code coverage tool

1. **Incompatibility with Code Coverage Analysis:** Certain test targets in the Oppia-Android codebase fail to execute and collect coverage using the Bazel coverage command. The underlying issues are still being investigated (see tracking issue [#5481](https://github.com/oppia/oppia-android/issues/5481)), and these files are currently exempt from coverage checks. However, it's expected that all new test files should work without needing this exemption.

2. **Function and Branch Coverage:** The Oppia-Android code coverage tool currently provides only line coverage data. It does not include information on function or branch coverage.

3. **Kotlin inline functions:** With JaCoCo coverage gaps, Kotlin inline functions may be inaccurately reported as uncovered in coverage reports. (See tracking issue [#5501](https://github.com/oppia/oppia-android/issues/5501))

4. **Line and Partial Coverages:** The current line coverage analysis in Oppia Android is limited and may not accurately reflect the execution of complex or multi-branch code within a single line, reporting lines as fully covered even if only part of the logic within those lines is executed, leading to potentially misleading coverage data. (See tracking issue [#5503](https://github.com/oppia/oppia-android/issues/5503))

5. **Flow Interrupting Statements:** The coverage reports may inaccurately reflect the coverage of flow-interrupting statements (e.g., exitProcess(1), assertion failures, break). These lines may be marked as uncovered even when executed, due to JaCoCo's limitations in tracking code execution after abrupt control flow interruptions. (See tracking issue [#5506](https://github.com/oppia/oppia-android/issues/5506))

6. **Uncovered Last Curly Brace in Kotlin:** The last curly brace of some Kotlin functions may be reported as uncovered, even when the function is fully executed during tests. This issue requires further investigation to determine if it's due to incomplete test execution or dead code generated by the Kotlin compiler. (See tracking issue [#5523](https://github.com/oppia/oppia-android/issues/5523))