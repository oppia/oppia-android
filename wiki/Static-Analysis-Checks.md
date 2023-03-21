# Background
Static analysis is a method of debugging by examining source code before a program is run. It’s done by analyzing a set of code against a set (or multiple sets) of coding rules.

The primary benefit is that it increases our ability to reliably enforce best practices. This is because humans make mistakes. Humans can unknowingly miss some places while reviewing, but with static analysis, this scenario will never happen.

It takes time for developers to do manual code reviews. Automated tools are much faster.
Static code checking helps to detect problems early on by pinpointing exactly where the error is in the code before a reviewer even looks at it.

<br />

<p align="center">
<img width="800" align="top" alt="Development flow without static analysis figure" src="https://user-images.githubusercontent.com/55937724/129643738-0ac235fc-32ae-4f92-8265-3024d97b597c.png" />

<br /><div align="center"><em>Development flow without static analysis</em></div>
</p>

<br /><br /><br />

<p align="center">
<img width="800" align="top" alt="Development flow with static analysis figure" src="https://user-images.githubusercontent.com/55937724/129644475-73d75380-8cd2-45a6-a4ea-9e4c3484cd82.png" />

<br /><div align="center"><em>Development flow with static analysis</em></div>
</p>

<br />

We can see how a static analysis tool (in this case a linter) simplified the flow and saved the time of both the developer and the reviewer which was wasted earlier in correcting the lint errors.

This page outlines the static analysis checks implemented in Oppia Android, instructions for how to utilize & maintain them, and instructions for introducing new checks to help enforce best practices as the codebase continues to scale.

# Static analysis checks in Oppia Android

## RegexPatternValidation Check

### Generic regex pattern matching against file names
This check ensures that there are no prohibited file path patterns present in the repository.

#### Purpose & adding new patterns
This check is needed so as to prevent a particular type of file from being added into a wrong directory.

For example: If we want to prevent activities from being added into the any directory except testing and app, then we have to add its regex pattern in the [scripts/assets/filename_pattern_validation_checks.textproto](https://github.com/oppia/oppia-android/blob/2da95a53928bc989f5959fbac211f7f7ca0a753f/scripts/assets/filename_pattern_validation_checks.textproto) file like this:

```
filename_checks {
  prohibited_filename_regex: "^((?!(app|testing)).)+/src/main/.+?Activity.kt"
  failure_message: "Activities cannot be placed outside the app or testing module"
}
```

#### Fixing failures
In general, failures for this check should be fixed by moving the file to the correct directory. In cases where that can’t happen or the check is wrong, please:

1. Add the file as an exemption in [scripts/assets/filename_pattern_validation_checks.textproto](https://github.com/oppia/oppia-android/blob/2da95a53928bc989f5959fbac211f7f7ca0a753f/scripts/assets/filename_pattern_validation_checks.textproto) for the corresponding failing check in <textproto_file_path>, e.g.:
```
filename_checks {
  prohibited_filename_regex: "^((?!(app|testing)).)+/src/main/.+?Activity.kt"
  failure_message: "Activities cannot be placed outside the app or testing module"
  exempted_file_name: "testing/src/main/SampleActivity.kt" 
}
```
2. Add an explanation to your PR description detailing why this exemption is correct

### Generic regex pattern matching against file contents
This check ensures that there are no prohibited file contents present in the repository.

#### Purpose & adding new patterns
This check is needed so as to prevent the use of any prohibited content in a file which allows the team to introduce specific best practices checks.

For example: If we want to prevent the use of support library in the repository, then we have to add its regex pattern in the [scripts/assets/file_content_validation_checks.textproto](https://github.com/oppia/oppia-android/blob/2da95a53928bc989f5959fbac211f7f7ca0a753f/scripts/assets/file_content_validation_checks.textproto) file like this:

```
file_content_checks {
  filename_regex: ".+?.kt"
  prohibited_content_regex: "^import .+?support.+?$"
  failure_message: "AndroidX should be used instead of the support library"
}
```

#### Fixing failures
In general, failures for this check should be fixed by not using the prohibited content in the repository (the error message of the failure should explain what should be used, instead).

In cases where that can’t happen or the check is wrong, please:

1. Add the file as an exemption in [scripts/assets/file_content_validation_checks.textproto](https://github.com/oppia/oppia-android/blob/2da95a53928bc989f5959fbac211f7f7ca0a753f/scripts/assets/file_content_validation_checks.textproto) for the particular check which is failing.
For example: a file which has relative path to root as app/src/main/java/org/oppia/android/home/SampleActivity.kt, should be added as follows to the corresponding failing check:
```
file_content_checks {
  filename_regex: ".+?.kt"
  prohibited_content_regex: "^import .+?support.+?$"
  failure_message: "AndroidX should be used instead of the support library"
  exempted_file_name: "app/src/main/java/org/oppia/android/home/SampleActivity.kt"
}
```
2. Add an explanation to your PR description detailing why this exemption is correct

## XML syntax check
This check ensures that all the XML files present in the repository are syntactically correct.

### Purpose
There are no linters to evaluate XML correctness. This check performs a fast check of correctness before any attempts to build the app will complete, allowing for a faster feedback cycle for potential XML issues.

### Fixing failures
To fix failures for this check: correct the syntax of the XML file which is failing the check.

## Test file presence check
This check ensures that all the production (file which is not a test file) Kotlin files must have a corresponding test file present.

### Purpose
To ensure that we are not missing tests for any production file being added to the codebase, this check is needed. It helps us to ensure production files have corresponding tests and reminds the contributor to add tests for new code added.

### Fixing failures
In general, failures for this check should be fixed by: adding a corresponding test file which has tests for the file which fails this check. Note that: the file name of the added test file must be file_name without extension + “Test.kt”.
For example: For a file named as “SampleFragment.kt” failing this check, the name of the added test file should be “SampleFragmentTest.kt”.

In cases where a test can’t be added, or the check is wrong, please:

1. Add it as an exemption by providing its relative path to root in [script/assets/test_file_exemptions.textproto](https://github.com/oppia/oppia-android/blob/2da95a53928bc989f5959fbac211f7f7ca0a753f/scripts/assets/test_file_exemptions.textproto).
Also, note that the file paths in the textproto file are maintained in lexicographical order. While adding any new file, please add it only at the correct lexicographical position, so that the list remains sorted. For example if we want to add the 'ActivityComponent.kt' file to the exemption list, at the correct lexicographical position in the textproto file add:
```
exempted_file_path: "app/src/main/java/org/oppia/android/app/activity/ActivityComponent.kt"
```
2. Add an explanation to your PR description detailing why this exemption is correct.

Following are the cases where its valid to have test file exemptions:
1. Interface files
2. Files with only constants defined (no logic)

## Accessibility label check
This check ensures that activities are defined with accessibility labels.

### Purpose
For users requiring accessibility assistance (e.g. using a screen reader), activity labels are very important since they are read by screen readers to provide context on where the user is within the app. Suppose, on the button click, the app transits to a new activity. An unassisted user will likely face no barriers in understanding that a new screen/activity has opened, but accessibility users need an indication so that they can be notified that the transition to a new activity has been successfully made. For this reason, all activities must be defined with accessibility labels.

### Fixing failures
If it’s a production activity, add a label.

If it’s a test activity or an activity that can’t/shouldn’t have a label, add an exemption by providing its relative path to root in [script/assets/accessibility_label_exemptions.textproto](https://github.com/oppia/oppia-android/blob/2da95a53928bc989f5959fbac211f7f7ca0a753f/scripts/assets/accessibility_label_exemptions.textproto). If an exemption is added, please include a rationale in your PR description explaining why this exemption is correct.
Also, note that the exemptions in the textproto file are maintained in lexicographical order. While adding any new Activity, please add it only at the correct lexicographical position, so that the list remains sorted.

For example if we want to add the 'RecentlyPlayedActivity' to the exemption list, add:
```
exempted_activity: "app/src/main/java/org/oppia/android/app/home/HomeActivity"
```
at the correct lexicographical position in the textproto file.

## KDoc validity check
This check ensures that all non-private declarations/members are documented with KDocs.

### Purpose
To ensure that we are not missing the documentation for any non-private decorations or members, this check is needed. Documentation is very important as it helps team members understand specific contexts within the codebase. Therefore, we should document as much as we can. Further, to ensure consistency we generally expect that all non-private members within Kotlin files are documented (per our [style guide](https://developer.android.com/kotlin/style-guide#usage)).

### Fixing failures
To fix failures for this check is to add a KDoc at the member which is missing it. Please refer to the failure logs to get the exact location (file path and line number) of the failure. For guidance on how to write KDocs, please look at our [style guide](https://developer.android.com/kotlin/style-guide).

## TODO open checks
This check ensures that every TODO present in the codebase corresponds to open issues on GitHub and is also correctly formatted as per the convention.

### Purpose
To avoid scenarios where a TODO was added not corresponding to an open issue on GitHub, this check is particularly needed. Having a corresponding issue for a TODO helps us to track its progress. Also, in order to maintain consistency we want all the TODOs to be formatted as per the convention.

### Fixing failures

#### TODO formatting failure
To fix the formatting failure, please make sure that the added TODO is strictly as per this format (please pay attention to the whitespaces):

Kotlin & Java files:
```
// TODO(#ISSUE_NUMBER): <todo_description>
```
Shell & BUILD/Bazel files:
```
# TODO(#ISSUE_NUMBER): <todo_description>
```
XML files:
```
<!-- TODO(#ISSUE_NUMBER): <todo_description> -->
```
#### TODO Open issue failure
To fix this failure: there are 3 ways:
1. Repurpose the TODO to a new issue.
2. If the TODO has been resolved then please remove it from the repository.
3. Reopen the issue.
#### Case when using ‘TODO’ keyword for documentation purposes
If it’s a case where a ‘TODO’ keyword has been used for documentation purposes or if it's not meant to correspond to a future work, then please add an exemption for it. Add a new TODO exemption in the [scripts/assets/todo_open_exemptions.textproto](https://github.com/oppia/oppia-android/blob/2da95a53928bc989f5959fbac211f7f7ca0a753f/scripts/assets/todo_open_exemptions.textproto).
Example:
```
todo_open_exemption {
  exempted_file_path: <relative_path_to_file>,
  line_number: <line_number_where_the_todo_is_present>
}
```

## TODO issue resolved check
The check ensures that a TODO issue is not closed until all of its corresponding TODO items are resolved.

### Purpose
We need this check to avoid scenarios like missing a TODO item not being resolved and its issue being mistakenly closed, since this should result in confusing contexts where a team member looking up a particular TODO from code sees that the issue is closed and isn’t sure whether the issue has actually been resolved. If a TODO issue is closed without resolving all of its TODO items, then it will be reopened automatically by the GitHub actions bot with a comment containing the unresolved TODOs.

### Fixing failures
To fix the failures for this check: resolve the TODO items and then close the issue. If the TODO items are not resolved the issue will remain in the open state.

## Java lint check
// TODO([#3690](https://github.com/oppia/oppia-android/issues/3690)): Complete static checks Wiki

## Kotlin lint check
// TODO([#3690](https://github.com/oppia/oppia-android/issues/3690)): Complete static checks Wiki

## Protobuf lint check
// TODO([#3690](https://github.com/oppia/oppia-android/issues/3690)): Complete static checks Wiki

## Bazel lint check
// TODO([#3690](https://github.com/oppia/oppia-android/issues/3690)): Complete static checks Wiki

# How to add a new Static analysis check
// TODO([#3690](https://github.com/oppia/oppia-android/issues/3690)): Complete static checks Wiki
