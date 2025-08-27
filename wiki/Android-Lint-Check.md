# Android Lint Analysis Tool

## Table of Contents

- [Overview](#overview)
- [Understanding Android Lint](#understanding-android-lint)
- [Why is Android Lint Important?](#why-is-android-lint-important)
- [How to use the Android Lint tool?](#how-to-use-the-android-lint-tool)
    - [Understanding the Lint Report](#understanding-the-lint-report)
        - [Grouped by Severity](#grouped-by-severity)
        - [Grouped by File Path](#grouped-by-file-path)
- [Script Working Directory and Temporary Files](#script-working-directory-and-temporary-files)
- [Issue Severities and Categories](#issue-severities-and-categories)
- [Handling New Lint Issues](#handling-new-lint-issues)
- [False Positive Detection](#false-positive-detection)
- [Exemption File Management](#exemption-file-management)
- [Unused Enum Mappings Detection](#unused-enum-mappings-detection)
- [CI Integration and Static Checks](#ci-integration-and-static-checks)
- [Troubleshooting and FAQ](#troubleshooting-and-faq)
- [Limitations of the Android Lint tool](#limitations-of-the-android-lint-tool)

# Overview

Android Lint is a static analysis tool provided by Android Studio that scans Android project source files to detect potential bugs, security issues, performance problems, usability issues, and other code quality concerns. The Oppia Android Lint Analysis Script provides a comprehensive way to analyze your codebase for Android-specific issues and generate detailed reports that help maintain code quality and follow Android development best practices.

The script performs project-level analysis, examining all modules in the project to provide a holistic view of code quality issues across the entire codebase.

# Understanding Android Lint

Android Lint examines your Android project files and identifies various issues that might affect the app's performance, usability, accessibility, and internationalization. It can detect problems like:

- Unused resources
- Missing translations
- Security vulnerabilities
- Performance bottlenecks
- Accessibility issues
- API usage problems

Let's look at a simple example. Consider a layout file with a hardcoded string:

```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Hello World!" />
```

Android Lint would flag this as a **HardcodedText** issue because the text should be defined in a string resource file for proper internationalization:

```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/hello_world" />
```

# Why is Android Lint Important?

- **Prevents Common Mistakes:**
  Lint catches common Android development mistakes early in the development process, preventing bugs from reaching production.
- **Ensures Best Practices:**
  It enforces Android development best practices and helps maintain consistent code quality across the project.
- **Improves App Performance:**
  By identifying performance issues, unused resources, and inefficient code patterns, Lint helps optimize the app's performance.
- **Enhances Accessibility:**
  Lint can identify accessibility issues, ensuring the app is usable by people with disabilities.
- **Maintains Security:**
  It detects potential security vulnerabilities and helps maintain secure coding practices.
- **Supports Internationalization:**
  Lint helps ensure the app is properly prepared for localization by identifying hardcoded strings and other internationalization issues.

# How to use the Android Lint tool?

The Oppia Android Lint Analysis Script can be used through the command line interface to analyze the codebase and generate comprehensive reports. The reports for the script can also be analyzed through static checks CI workflow.

## Command Line Interface (CLI)

Note: Follow these [Bazel setup instructions](https://github.com/oppia/oppia-android/wiki/Installing-Oppia-Android) if Bazel isn't yet set up in your local development environment.

### Run Android Lint Analysis

```sh
bazel run //scripts:android_lint_check -- <path_to_repository_root> [path_to_proto_binary] [--group_by_severity] [--processTimeout=<minutes>]
```
Example: `bazel run //scripts:android_lint_check -- $(pwd)`

**Arguments:**
- `<path_to_repository_root>`: The root path of the repository (required)
- `[--proto=<path_to_proto_binary>]`: Optional relative path to the exemption .pb file (defaults to `scripts/assets/android_lint_exemptions.pb`)
- `[--group_by_severity]`: Optional flag to group issues by severity level
- `[--processTimeout=<minutes>]`: Optional process timeout in minutes (defaults to 10 minutes)

Example: `bazel run //scripts:android_lint_check -- $(pwd) --proto=scripts/assets/android_lint_exemptions.pb --group_by_severity --processTimeout=20`

The script can also be analyzed through the **Static Checks CI workflow** for automated code quality monitoring in continuous integration.

## Understanding the Lint Report

The Android Lint Analysis Tool generates detailed reports that can be organized in two ways: **grouped by severity** or **grouped by file path**. The report format is controlled by the `--group_by_severity` flag.

### Report Summary Header

The report starts with a clear failure/success indicator and severity count summary:

```
LINT CHECKS FAILED. Please fix the issues below.

Redundant Exemptions: 2
Fatal: 1
Error: 4
Warning: 1
Total Issues: 8
```

The script will show:
- **Pass** status for issues of Warning/Information severity only
- **Fail** status for any Fatal/Error severity issues
- **Fail** status if redundant exemptions or unused enum mappings are detected

### Redundant Exemptions Section

If redundant exemptions are detected, they are displayed at the top of the report:

```
Redundant exemptions (no corresponding lint issues found):
Please remove them from scripts/assets/android_lint_exemptions.textproto

File: app/src/main/java/com/example/MainActivity.kt
  - DUPLICATE_STRINGS
  - BACK_BUTTON
```

## Grouped by File Path

When the `--group_by_severity` flag is not used (default behavior), issues are organized by file path, making it easier to focus on specific files:

```
================================================================================
FILE: app/src/main/java/com/example/MainActivity.kt (3 issues)
================================================================================

Issue 1 of 3: CUSTOM_SPLASH_SCREEN (Category: Correctness)
  Severity: Fatal
  Line: 25
  Error Line: setTheme(R.style.SplashTheme)
  Message: Custom splash screen implementation may not work correctly on Android 12+
  Explanation:
    Starting with Android 12, the platform provides a new splash screen API that
    replaces custom splash screen implementations. Custom splash screens may not
    display correctly or may cause performance issues.

    Consider migrating to the new SplashScreen API provided by the
    androidx.core:core-splashscreen library for better compatibility and
    performance.
------------------------------------------------------------
Issue 2 of 3: NEW_API (Category: Correctness)
  Severity: Error (FALSE POSITIVE)
  Line: 15
  Error Line: items.forEach { item ->
  Message: Call requires API level 24 (current min is 21): `java.lang.Iterable#forEach`
  Workaround: Use safeForEach from IterableExtensions.kt instead of directly calling forEach to avoid known lint false positives on API < 24.
```

## Grouped by Severity

When using the `--group_by_severity` flag, issues are organized by their severity levels, making it easy to prioritize fixes based on criticality:

```
================================================================================
 SEVERITY: ERROR (2 issues)
================================================================================

Issue 1 of 2: NEW_API (Category: Correctness)
  Severity: Error
  File: /path/to/app/src/main/java/org/oppia/android/app/utility/ClickableAreasImage.kt
  Line: 45
  Error Line: clickableAreas.forEach { clickableArea ->
  Message: Call requires API level 24 (current min is 21): `java.lang.Iterable#forEach`
  Explanation:
    This check scans through all the Android API calls in the application and warns about any calls that are not available on **all** versions targeted by this application...
```

# Script Working Directory and Temporary Files

During execution, the Android Lint Analysis Tool creates a temporary intermediary directory called **`lint_analysis`** within the project root. This directory serves as the working space for the script's analysis process and contains various files and subdirectories essential for lint execution.

## Contents of lint_analysis Directory

The temporary directory includes the following key files and subdirectories:

### Primary Analysis Files
- **`lint-report.xml`**: The main XML report generated by the Android Lint tool containing all detected issues with detailed information
- **`lint-project-description.xml`**: XML file describing the project structure, modules, and configuration used by the lint tool.

### Supporting Directories
The script also creates multiple subdirectories within `lint_analysis` that assist in the analysis process:
- Module-specific temporary directories for each analyzed module (app, domain, data, utility, testing)
- Extracted AAR directories for lint analysis assistance
- Cache directories for lint tool

**Note**: The contents of the `lint_analysis` directory are primarily for internal use by the script. While these files can be examined for debugging purposes, they should not be modified manually.

# Issue Severities and Categories

The Android Lint Analysis Tool categorizes issues into four severity levels and multiple categories:

## Severity Levels

- **FATAL**: Critical issues that must be fixed immediately. These typically represent security vulnerabilities or severe bugs that could cause app crashes or data loss.
- **ERROR**: Important issues that should be addressed before release. These include API compatibility problems, resource issues, or correctness problems.
- **WARNING**: Issues that should be reviewed and ideally fixed. These include performance problems, usability issues, or minor correctness issues.
- **INFORMATION**: Informational issues that provide suggestions for improvement. These are typically code style or best practice recommendations.

## Issue Categories

Android Lint organizes issues into various categories based on their nature. For a comprehensive list of all available issue categories and their descriptions, refer to the [Android Lint Checks Documentation](https://googlesamples.github.io/android-custom-lint-rules/checks/severity.md.html).

Common categories include:

- **Correctness**: Issues related to code correctness and potential bugs
- **Performance**: Issues that may impact app performance
- **Internationalization**: Issues related to localization and text handling
- **Security**: Security-related vulnerabilities and concerns
- **Usability**: User experience and interface issues
- **Accessibility**: Issues that affect app accessibility
- **Compliance**: Issues related to Google Play Store policies

# Handling New Lint Issues

When the Android Lint Analysis Tool encounters a new lint issue ID that hasn't been mapped in the system, the script will filter out these unknown issues but won't report them in the output. To handle new lint issues that appear in your codebase:

## Steps to Add New Lint Issue ID:

1. **Update the Proto Definition:**
    - Navigate to `scripts/src/java/org/oppia/android/scripts/proto/android_lint.proto`
    - Add the new issue ID to the `LintIssueId` enum

2. **Update the Issue Mapping:**
    - Navigate to `scripts/src/java/org/oppia/android/scripts/lint/LintAnalysisReporter.kt`
    - Add the new issue ID to the `issueIdMapping` map

## Example:

If you need to add a new issue ID for example `NewSecurityCheck`:

**In android_lint.proto:**
```protobuf
enum LintIssueId {
  // ... existing issue IDs ...
  NEW_SECURITY_CHECK = 123;
}
```

**In LintAnalysisReporter.kt:**
```kotlin
private val issueIdMapping: Map<String, LintIssueId> = mapOf(
  // ... existing mappings ...
  "NewSecurityCheck" to LintIssueId.NEW_SECURITY_CHECK
)
```
These updates are required if the new issue ID needs to be included in the exemption file.

# False Positive Detection

The Android Lint Analysis Tool includes built-in detection of known false positive issues. When a false positive is detected, it's clearly marked in the report output:

```
Issue 2 of 3: NEW_API (Category: Correctness)
  Severity: Error (FALSE POSITIVE)
  Line: 15
  Error Line: items.forEach { item ->
  Message: Call requires API level 24 (current min is 21): `java.lang.Iterable#forEach`
  Workaround: Use safeForEach from IterableExtensions.kt instead of directly calling forEach to avoid known lint false positives on API < 24.
```

## Adding New False Positives

To add recognition of new false positive patterns, update the `falsePositiveIssues` set in `LintAnalysisReporter.kt`:

```kotlin
private val falsePositiveIssues: Set<FalsePositiveIssue> = setOf(
  FalsePositiveIssue(
    issueId = "IssueId",
    message = "Specific message pattern to match",
    severity = LintSeverity.ERROR,
    workaroundMessage = "Explanation of the workaround or why this is a false positive"
  )
)
```

# Exemption File Management

The Android Lint Analysis Tool includes intelligent exemption file management that helps maintain clean and accurate exemption configurations. During analysis, the script performs validation of the exemption file to ensure it contains only relevant and necessary exemptions.

## When to add an exemption
New exemptions should be added to the `android_lint_exemptions.textproto` file when:
- If a particular lint issue in a file is not applicable or relevant to the current codebase
- A lint issue is a false positive and does not require fixing hence requiring a suppression

## Redundant Exemption Detection

The script automatically detects and reports **redundant exemptions** in the exemption file - these are exemptions that are no longer needed because:
- The corresponding lint issues have been fixed in the codebase
- The files or code sections referenced in the exemptions no longer exist
- The exempted issue types are no longer triggered by the current code

## Exemption File Maintenance Prompts

When redundant exemptions are detected, the script logs detailed information about these obsolete entries and prompts the user to remove them from the exemption textproto file. The script will **fail** when redundant exemptions are detected, forcing cleanup of the exemption file.

## Example Redundant Exemption Output

```
Redundant exemptions (no corresponding lint issues found):
Please remove them from scripts/assets/android_lint_exemptions.textproto

File: app/src/main/java/com/example/MainActivity.kt
  - DUPLICATE_STRINGS
  - BACK_BUTTON
```

## Recommended Action

When the script identifies redundant exemptions, you must:
1. Review the reported redundant exemptions
2. Verify that the issues have indeed been resolved or the files no longer exist
3. Remove the obsolete exemptions from the `android_lint_exemptions.textproto` file
4. Re-run the lint analysis to ensure the exemption file is clean and accurate

# Unused Enum Mappings Detection

The Android Lint Analysis Tool automatically detects when issue IDs are defined in the system but no corresponding lint issues are found in the codebase. This helps maintain a clean and accurate mapping system.

## Unused Enum Detection Output

When unused enum mappings are detected, the script will fail with output like:

```
============================================================
UNUSED ENUM MAPPINGS DETECTED:
The following issue IDs are defined in issueIdMapping but no corresponding lint issues were found.
Please remove them from the LintIssueId enum and issueIdMapping:

  - KeyboardInaccessibleWidget -> KEYBOARD_INACCESSIBLE_WIDGET
  - LabelFor -> LABEL_FOR
  - ObsoleteSdkInt -> OBSOLETE_SDK_INT
  - RedundantLabel -> REDUNDANT_LABEL
  - Typos -> TYPOS

============================================================
ANDROID LINT CHECK FAILED
```

## When This Occurs

Unused enum mappings are detected when:
- Issue IDs are defined in the `issueIdMapping` but aren't found in any actual lint reports
- Lint rules have been deprecated or are no longer triggered by the current codebase
- Issue types have been renamed or consolidated in newer versions of Android Lint

## Resolving Unused Enum Mappings

To resolve unused enum mappings:

1. **Remove from Proto Definition**: Delete the unused enum values from `android_lint.proto`
2. **Remove from Mapping**: Delete the unused entries from `issueIdMapping` in `LintAnalysisReporter.kt`
3. **Verify**: Re-run the lint analysis to ensure the mapping is clean

**Note**: The `LintError` issue ID is exempt from unused enum detection as it's used for internal lint tool errors.

# CI Integration and Static Checks

The Android Lint Analysis Tool is fully integrated into the Oppia Android project's continuous integration pipeline through the **Static Checks CI workflow**. This integration ensures that code quality is automatically monitored for every pull request and commit.

## Static Checks Workflow Integration

The lint analysis is part of the automated static checks that run on GitHub Actions. When you submit a pull request or push changes to the repository, the CI system automatically triggers the Android Lint Analysis Tool to:

- Analyze all modified files and their dependencies
- Generate comprehensive lint reports
- Check for new lint issues introduced by the changes
- Validate that no exempted issues have been resolved (redundant exemptions)
- Ensure code quality standards are maintained across all modules

## Viewing CI Results

You can monitor the results of the Android Lint Analysis in the GitHub Actions interface:

The CI workflow will:
- **Pass** ✅ if no new lint issues are detected (Warning/Information severity only)
- **Fail** ❌ if new lint issues are found that need to be addressed (Fatal/Error severity)
- **Fail** ❌ if redundant exemptions or unused enum mappings are detected
- Provide detailed logs and reports for investigation

## Interpreting CI Failures

When the Static Checks workflow fails due to lint issues:

1. **Check the CI logs** for detailed information about detected issues
2. **Run the script locally** to get a comprehensive report
3. **Address the identified issues** in your code, **exempt** them in the textproto file, or **clean up** redundant exemptions/unused mappings
4. **Re-run the CI** by pushing your fixes

The CI integration ensures that the main branch always maintains high code quality standards while providing immediate feedback to contributors about potential issues in their changes.

# Troubleshooting and FAQ

This section addresses common issues and questions that contributors may encounter when using the Android Lint Analysis Tool.

## Frequently Asked Questions

### Q: The script is timing out during execution. What should I do?

**A:** The script has a default timeout of 10 minutes, but slower systems may require more time. Use the `--processTimeout` flag to increase the timeout duration:

```sh
bazel run //scripts:android_lint_check -- $(pwd) --processTimeout=20
```

This example increases the timeout to 20 minutes. Adjust the value based on your system's performance.

### Q: A lint issue appears to be a false positive. How should I handle this?

**A:** The script includes built-in false positive detection for known cases. If you encounter a new false positive:

1. **Verify the issue**: Double-check whether the reported issue is indeed a false positive
2. **Check if it's already detected**: Look for the "(FALSE POSITIVE)" marker in the output
3. **Contact an Oppia member**: Reach out to project maintainers for guidance on whether the issue should be exempted or added to the false positive list
4. **File an issue**: Create a GitHub issue with details about the false positive, including:
    - The specific lint issue ID and message
    - The file and line number where it occurs
    - Why you believe it's a false positive
    - Any relevant context about the code

### Q: The script reports "redundant exemptions" or "unused enum mappings". What does this mean?

**A:**
- **Redundant exemptions**: These are exemptions in the textproto file for issues that no longer exist in the codebase. Remove them from `android_lint_exemptions.textproto`.
- **Unused enum mappings**: These are issue IDs defined in the system but not found in any actual lint issues. Remove them from both the proto definition and the `issueIdMapping`.

Both conditions will cause the script to fail and must be resolved.

### Q: How do I interpret the CI failure in the Static Checks workflow?

**A:** When the Static Checks workflow fails due to lint issues:
1. Click on the failed **Static Checks** workflow in the GitHub Actions tab
2. Examine the logs to identify the specific lint issues
3. Run the script locally for a detailed report
4. Fix the identified issues, clean up exemptions, or update mappings
5. Push your changes to trigger the CI again

### Q: The script is reporting issues in files I didn't modify. Why?

**A:** This can happen because:
- **Cross-module dependencies**: Changes in one module may affect lint analysis in dependent modules
- **Resource dependencies**: Modifications to shared resources can trigger issues in files that use those resources
- **Project-level analysis**: The script analyzes the entire project, not just modified files

### Q: Can I run the script on a specific module instead of the entire project?

**A:** Currently, the script is designed to perform project-level analysis across all modules (app, domain, data, utility, testing). Module-specific analysis is not supported, as it could miss important cross-module dependencies and issues.

### Q: Why don't I see certain lint issues that appear in Android Studio?

**A:** The script uses a specific set of mapped issue IDs. Unknown issue IDs are filtered out and not reported. If you need to include new issue types, follow the steps in the [Handling New Lint Issues](#handling-new-lint-issues) section.

# Limitations of the Android Lint tool

1. **Report Accuracy**: The lint reports are sensitive to changes in the `LintProjectDescription` utility which can lead to inaccuracies compared to the Gradle version of the lint tool.

2. **Execution Time**: The script scans the entire codebase for issues and takes up to 8-10 minutes for execution due to the comprehensive project-level analysis across all modules.

3. **Limited Issue Coverage**: The script exemption system currently supports only a predefined set of issue categories through the `issueIdMapping`. Unknown issue IDs are filtered out and not reported. While this may seem limiting, it helps maintain better oversight of issues by ensuring all issue types are explicitly handled.

4. **False Positive Detection**: Currently only recognizes a limited set of known false positive patterns. New false positives require manual addition to the system.
