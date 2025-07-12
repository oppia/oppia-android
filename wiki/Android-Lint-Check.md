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
- [Exemption File Management](#exemption-file-management)
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
- `[path_to_proto_binary]`: Optional relative path to the exemption .pb file (defaults to `scripts/assets/android_lint_exemptions.pb`)
- `[--group_by_severity]`: Optional flag to group issues by severity level
- `[--processTimeout=<minutes>]`: Optional process timeout in minutes (defaults to 10 minutes)

Example: `bazel run //scripts:android_lint_check -- $(pwd) scripts/assets/android_lint_exemptions.pb --group_by_severity --processTimeout=20`

The script can also be analyzed through the **Static Checks CI workflow** for automated code quality monitoring in continuous integration.

## Understanding the Lint Report

The Android Lint Analysis Tool generates detailed reports that can be organized in two ways: **grouped by severity** or **grouped by file path**. The report format is controlled by the `--group_by_severity` flag.

Each issue in the report contains the following information:
- **Issue ID**: The unique identifier of the lint issue
- **Severity**: The severity level of the issue (Fatal, Error, Warning, Information)
- **Error Line**: The specific line of code that caused the issue (with visual indicator `~~~~~~~`)
- **Category**: The category to which the issue belongs (e.g., Correctness, Performance, Internationalization)
- **Priority**: The importance level assigned to the issue (1-10 scale)
- **Summary**: A brief summary title of the issue
- **Message**: A short description of the issue
- **Explanation**: A detailed explanation of the issue and potential solutions

# Issue Severities and Categories

The Android Lint Analysis Tool categorizes issues into four severity levels and multiple categories:

## Severity Levels

- **FATAL**: Critical issues that must be fixed immediately. These typically represent security vulnerabilities or severe bugs that could cause app crashes or data loss.
- **ERROR**: Important issues that should be addressed before release. These include API compatibility problems, resource issues, or correctness problems.
- **WARNING**: Issues that should be reviewed and ideally fixed. These include performance problems, usability issues, or minor correctness issues.
- **INFORMATION**: Informational issues that provide suggestions for improvement. These are typically code style or best practice recommendations.

### Summary Header
The report starts with a severity count summary and total issues:
```
Fatal: 0
Error: 2
Warning: 1
Information: 0
Total Issues: 3
```

### Grouped by File Path

When the `--group_by_severity` flag is not used, issues are organized by file path, making it easier to focus on specific files:

```
================================================================================
FILE: /path/to/app/src/main/java/org/oppia/android/app/utility/ClickableAreasImage.kt (1 issues)
================================================================================

Issue #1: NewApi
  Severity: Error
  Line: 45
  Error Line: clickableAreas.forEach { clickableArea ->
  Category: Correctness
  Priority: 6
  Summary: Calling new methods on older versions
  Message: Call requires API level 24 (current min is 21): `java.lang.Iterable#forEach`
  Explanation: This check scans through all the Android API calls in the application and warns about any calls that are not available on **all** versions targeted by this application (according to its minimum SDK attribute in the manifest)...
------------------------------------------------------------

================================================================================
FILE: /path/to/app/src/main/res/layout/activity_main.xml (1 issues)
================================================================================

Issue #1: HardcodedText
  Severity: Error
  Line: 12
  Error Line: android:text="Settings"
              ~~~~~~~
  Category: Internationalization
  Priority: 5
  Summary: Hardcoded text
  Message: Hardcoded string "Settings", should use @string resource
  Explanation: Hardcoded strings should not be used in layouts as they make internationalization difficult...
------------------------------------------------------------
```
### Grouped by Severity

When using the `--group_by_severity` flag, issues are organized by their severity levels, making it easy to prioritize fixes based on criticality:

```
================================================================================
 SEVERITY: ERROR (2 issues)
================================================================================

Issue ID: NewApi
  Severity: Error
  File: /path/to/app/src/main/java/org/oppia/android/app/utility/ClickableAreasImage.kt
  Line: 45
  Error Line: clickableAreas.forEach { clickableArea ->
  Category: Correctness
  Priority: 6
  Summary: Calling new methods on older versions
  Message: Call requires API level 24 (current min is 21): `java.lang.Iterable#forEach`
  Explanation: This check scans through all the Android API calls in the application and warns about any calls that are not available on **all** versions targeted by this application (according to its minimum SDK attribute in the manifest)...
------------------------------------------------------------

Issue ID: HardcodedText
  Severity: Error
  File: /path/to/app/src/main/res/layout/activity_main.xml
  Line: 12
  Error Line: android:text="Settings"
              ~~~~~~~
  Category: Internationalization
  Priority: 5
  Summary: Hardcoded text
  Message: Hardcoded string "Settings", should use @string resource
  Explanation: Hardcoded strings should not be used in layouts as they make internationalization difficult...
------------------------------------------------------------

================================================================================
 SEVERITY: WARNING (1 issues)
================================================================================

Issue ID: UnusedResources
  Severity: Warning
  File: /path/to/app/src/main/res/color-sw600dp-land/component_color_shared_tab_icon_color_selector.xml
  Line: 1
  Error Line: <selector xmlns:android="http://schemas.android.com/apk/res/android">
  Category: Performance
  Priority: 3
  Summary: Unused resources
  Message: The resource `R.color.component_color_shared_tab_icon_color_selector` appears to be unused
  Explanation: Unused resources make applications larger and slow down builds...
------------------------------------------------------------
```

### Final Status
The report concludes with a final status:
When the script passes:
The script will **PASS** if the issues reported are at max of severity Warning/Information
```
============================================================
ANDROID LINT CHECK PASSED
```
Or for failures:
The script will **FAIL** if there are issue with severity ERROR/FATAL
```
============================================================
ANDROID LINT CHECK FAILED
```
Or for internal lint issues:
If there is an internal issue while execution of the lint tool the script will fail with an internal issue
```
============================================================
ANDROID LINT CHECK FAILED WITH INTERNAL LINT ISSUES
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
- Extracted AAR directories for lint analysis asistance
- Cache directories for lint tool

**Note**: The contents of the `lint_analysis` directory are primarily for internal use by the script. While these files can be examined for debugging purposes, they should not be modified manually.

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

When the Android Lint Analysis Tool encounters a new lint issue ID that hasn't been mapped in the system, you'll see an error message like:

```
Unknown lint issue ID 'NewIssueId' found during analysis. Please add this issue ID to the LintIssueId enum in the proto definition and update the issueIdMapping in LintAnalysisReporter.
```

This error indicates that Android Lint has detected a new type of issue in the codebase that the tool doesn't recognize yet. To resolve this:

## Steps to Add New Lint Issue ID:

1. **Update the Proto Definition:**
    - Navigate to `scripts/src/java/org/oppia/android/scripts/proto/android_lint.proto`
    - Add the new issue ID to the `LintIssueId` enum

2. **Update the Issue Mapping:**
    - Navigate to `scripts/src/java/org/oppia/android/scripts/lint/LintAnalysisReporter.kt`
    - Add the new issue ID to the `issueIdMapping` in the `LintAnalysisReporter` class

## Example:

If you encounter a new issue ID for example `NewSecurityCheck`, you would:

**In android_lint.proto:**
```protobuf
enum LintIssueId {
  // ... existing issue IDs ...
  NEW_SECURITY_CHECK = 123;
}
```

**In LintAnalysisReporter.kt:**
```kotlin
private val issueIdMapping = mapOf(
  // ... existing mappings ...
  "NewSecurityCheck" to LintIssueId.NEW_SECURITY_CHECK
)
```

# Exemption File Management

The Android Lint Analysis Tool includes intelligent exemption file management that helps maintain clean and accurate exemption configurations. During analysis, the script performs validation of the exemption file to ensure it contains only relevant and necessary exemptions.

## When to add an exemption
New exemptions should be added to the `android_lint_exemptions.textproto` file when:
- If a particular lint issue in a file is not applicable or relevant to the current codebase
- A lint issue is a false positive and does not require fixing hence requiring a suppression

## Redundant Exemption Detection

The script automatically detects and reports **redundant issues** in the exemption file - these are exemptions that are no longer needed because:
- The corresponding lint issues have been fixed in the codebase
- The files or code sections referenced in the exemptions no longer exist
- The exempted issue types are no longer triggered by the current code

## Exemption File Maintenance Prompts

When redundant exemptions are detected, the script logs detailed information about these obsolete entries and prompts the user to remove them from the exemption textproto file.

## Example Redundant Exemption Output

```
Redundant exemptions (no corresponding lint issues found):
Please remove them from scripts/assets/android_lint_exemptions.textproto

File: app/src/main/res/layout/obsolete_layout.xml
  - UnusedResources
  - ContentDescription
File: app/src/main/java/com/example/MainActivity.kt
  - NewApi
```

## Recommended Action

When the script identifies redundant exemptions, it's recommended to:
1. Review the reported redundant exemptions
2. Verify that the issues have indeed been resolved or the files no longer exist
3. Remove the obsolete exemptions from the `android_lint_exemptions.textproto` file
4. Re-run the lint analysis to ensure the exemption file is clean and accurate

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
- **Pass** ✅ if no new lint issues are detected
- **Fail** ❌ if new lint issues are found that need to be addressed
- Provide detailed logs and reports for investigation

## Interpreting CI Failures

When the Static Checks workflow fails due to lint issues:

1. **Check the CI logs** for detailed information about detected issues
2. **Run the script locally** to get a comprehensive report
3. **Address the identified issues** in your code or **exempt** it in the textproto file
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

**A:** False positives can occur due to:
- **False reporting by the lint tool itself**: The Android Lint tool may occasionally report issues that aren't actually problems
- **Misconfiguration**: Project configuration or lint settings may not be properly aligned with the codebase structure

**Recommended actions:**
1. **Verify the issue**: Double-check whether the reported issue is indeed a false positive
2. **Contact an Oppia member**: Reach out to project maintainers for guidance on whether the issue should be exempted
3. **File an issue**: Create a GitHub issue with details about the false positive, including:
    - The specific lint issue ID and message
    - The file and line number where it occurs
    - Why you believe it's a false positive
    - Any relevant context about the code

### Q: The script fails with "Unknown lint issue ID" error. What does this mean?

**A:** This error occurs when the Android Lint tool detects a new type of issue that hasn't been mapped in the system yet. Follow the steps in the [Handling New Lint Issues](#handling-new-lint-issues) section to add the new issue ID to the proto definition and issue mapping.

### Q: How do I interpret the CI failure in the Static Checks workflow?

**A:** When the Static Checks workflow fails due to lint issues:
1. Click on the failed **Static Checks** workflow in the GitHub Actions tab
2. Examine the logs to identify the specific lint issues
3. Run the script locally for a detailed report
4. Fix/Exempt the identified issues in your code
5. Push your changes to trigger the CI again

### Q: The script is reporting issues in files I didn't modify. Why?

**A:** This can happen because:
- **Cross-module dependencies**: Changes in one module may affect lint analysis in dependent modules
- **Resource dependencies**: Modifications to shared resources can trigger issues in files that use those resources

### Q: Can I run the script on a specific module instead of the entire project?

**A:** Currently, the script is designed to perform project-level analysis across all modules (app, domain, data, utility, testing). Module-specific analysis is not supported, as it could miss important cross-module dependencies and issues.

# Limitations of the Android Lint tool

1. **Report Accuracy**: The lint reports are really sensitive to changes in the `LintProjectDescription` utility which also lead to inaccuracies compared to the Gradle version of the lint tool.
2. **Execution Time**: The script scans the entire codebase for issues and takes up to 8-10 minutes for execution due to the comprehensive project-level analysis across all modules.
3. **Exemption Issue IDs**: The script currently supports only a limited number of issue categories and may fail on new categories in the codebase. While this may seem blocking, it helps maintain better oversight of issues by ensuring all issue types are explicitly handled.
