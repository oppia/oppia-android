# Android Lint Analysis Tool

## Table of Contents

- [Overview](#overview)
- [Understanding Android Lint](#understanding-android-lint)
- [Why is Android Lint Important?](#why-is-android-lint-important)
- [How to use the Android Lint tool?](#how-to-use-the-android-lint-tool)
    - [Command Line Interface (CLI)](#command-line-interface-cli)
    - [Understanding the Lint Report](#understanding-the-lint-report)
        - [Grouped by Severity](#grouped-by-severity)
        - [Grouped by File Path](#grouped-by-file-path)
- [Issue Severities and Categories](#issue-severities-and-categories)
- [Handling New Lint Issues](#handling-new-lint-issues)
- [Limitations of the Android Lint tool](#limitations-of-the-android-lint-tool)

# Overview

Android Lint is a static analysis tool provided by Android Studio that scans Android project source files to detect potential bugs, security issues, performance problems, usability issues, and other code quality concerns. The Oppia Android Lint Analysis Script provides a comprehensive way to analyze your codebase for Android-specific issues and generate detailed reports that help maintain code quality and follow Android development best practices.

# Understanding Android Lint

Android Lint examines your Android project files and identifies various issues that might affect your app's performance, usability, accessibility, and internationalization. It can detect problems like:

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
  By identifying performance issues, unused resources, and inefficient code patterns, Lint helps optimize your app's performance.
- **Enhances Accessibility:**
  Lint can identify accessibility issues, ensuring your app is usable by people with disabilities.
- **Maintains Security:**
  It detects potential security vulnerabilities and helps maintain secure coding practices.
- **Supports Internationalization:**
  Lint helps ensure your app is properly prepared for localization by identifying hardcoded strings and other internationalization issues.

# How to use the Android Lint tool?

The Oppia Android Lint Analysis Tool can be used through the command line interface to analyze your codebase and generate comprehensive reports.

## Command Line Interface (CLI)

Note: Follow these [Bazel setup instructions](https://github.com/oppia/oppia-android/wiki/Installing-Oppia-Android) if Bazel isn't yet set up in your local development environment.

### Run Android Lint Analysis

```sh
bazel run //scripts:android_lint_check -- <path_to_repository_root> [path_to_proto_binary] [--group_by_severity] [--processTimeout=<minutes>]
```

**Arguments:**
- `<path_to_repository_root>`: The root path of the repository (required)
- `[path_to_proto_binary]`: Optional relative path to the exemption .pb file (defaults to `scripts/assets/android_lint_exemptions.pb`)
- `[--group_by_severity]`: Optional flag to group issues by severity level
- `[--processTimeout=<minutes>]`: Optional process timeout in minutes (defaults to 10 minutes)

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

### Grouped by Severity

When using the `--group_by_severity` flag, issues are organized by their severity levels, making it easy to prioritize fixes based on criticality:

```
================================================================================
ERROR Issues (2 issues found)
================================================================================

Issue ID: NewApi
Severity: Error
Category: Correctness
Priority: 6
Summary: Calling new methods on older versions
File: app/src/main/java/org/oppia/android/app/utility/ClickableAreasImage.kt
Message: Call requires API level 24 (current min is 21): `java.lang.Iterable#forEach`
Error Line: clickableAreas.forEach { clickableArea ->
~~~~~~~
Explanation: This check scans through all the Android API calls in the application 
and warns about any calls that are not available on **all** versions targeted by 
this application (according to its minimum SDK attribute in the manifest)...
------------------------------------------------------------

Issue ID: HardcodedText
Severity: Error
Category: Internationalization
Priority: 5
Summary: Hardcoded text
File: app/src/main/res/layout/activity_main.xml
Message: Hardcoded string "Settings", should use @string resource
Error Line: android:text="Settings"
             ~~~~~~~
Explanation: Hardcoded strings should not be used in layouts as they make 
internationalization difficult...
------------------------------------------------------------

================================================================================
WARNING Issues (1 issues found)
================================================================================

Issue ID: UnusedResources
Severity: Warning
Category: Performance
Priority: 3
Summary: Unused resources
File: app/src/main/res/color-sw600dp-land/component_color_shared_tab_icon_color_selector.xml
Message: The resource `R.color.component_color_shared_tab_icon_color_selector` appears to be unused
Error Line: <selector xmlns:android="http://schemas.android.com/apk/res/android">
^
Explanation: Unused resources make applications larger and slow down builds...
------------------------------------------------------------
```

### Grouped by File Path

When the `--group_by_severity` flag is not used, issues are organized by file path, making it easier to focus on specific files:

```
================================================================================
FILE: app/src/main/java/org/oppia/android/app/utility/ClickableAreasImage.kt (1 issues)
================================================================================

Issue #1: NewApi
Severity: Error
Category: Correctness
Priority: 6
Summary: Calling new methods on older versions
Message: Call requires API level 24 (current min is 21): `java.lang.Iterable#forEach`
Error Line: clickableAreas.forEach { clickableArea ->
~~~~~~~
Explanation: This check scans through all the Android API calls in the application 
and warns about any calls that are not available on **all** versions targeted by 
this application (according to its minimum SDK attribute in the manifest)...
------------------------------------------------------------

================================================================================
FILE: app/src/main/res/color-sw600dp-land/component_color_shared_tab_icon_color_selector.xml (1 issues)
================================================================================

Issue #1: UnusedResources
Severity: Warning
Category: Performance
Priority: 3
Summary: Unused resources
Message: The resource `R.color.component_color_shared_tab_icon_color_selector` appears to be unused
Error Line: <selector xmlns:android="http://schemas.android.com/apk/res/android">
^
Explanation: Unused resources make applications larger and slow down builds...
------------------------------------------------------------
```

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

When the Android Lint Analysis Tool encounters a new lint issue ID that hasn't been mapped in the system, you'll see an error message like:

```
Unknown lint issue ID 'NewIssueId' found during analysis. Please add this issue ID to the LintIssueId enum in the proto definition and update the issueIdMapping in LintAnalysisReporter.
```

This error indicates that Android Lint has detected a new type of issue in the codebase that the tool doesn't recognize yet. To resolve this:

### Steps to Add New Lint Issue ID:

1. **Update the Proto Definition:**
    - Navigate to `scripts/src/java/org/oppia/android/scripts/proto/android_lint.proto`
    - Add the new issue ID to the `LintIssueId` enum

2. **Update the Issue Mapping:**
    - Navigate to `scripts/src/java/org/oppia/android/scripts/lint/LintAnalysisReporter.kt`
    - Add the new issue ID to the `issueIdMapping` in the `LintAnalysisReporter` class

### Example:

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

# Limitations of the Android Lint tool

1. **Report Accuracy**: The lint reports are really sensitive to changes in the `LintProjectDescription` utility which also lead to inaccuracies compared to the Gradle version of the lint tool.
2. **Execution Time**: The script scans the entire codebase for issues and takes up to 8-10 minutes for execution.
3. **Exemption Issue IDs**: The script currently supports only a limited number of issue categories and may fail on new categories in the codebase. While this may seem blocking, it helps maintain better oversight of issues by ensuring all issue types are explicitly handled.
