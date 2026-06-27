#!/bin/bash

# INSTRUCTIONS
# This script will run all script checks locally to make
# sure that all script checks will still pass when run on
# CI
#
# To run this check run the script from the Oppia-android root folder:
#
#	bash scripts/static_checks.sh
#

# LINT CHECKS
# Run Java lint check
FAILURES=0
bash scripts/checkstyle_lint_check.sh || FAILURES=$((FAILURES+1))
echo ""

# Run Kotlin lint check
bash scripts/ktlint_lint_check.sh || FAILURES=$((FAILURES+1))
echo ""

# Run protobuf lint checks
bash scripts/buf_lint_check.sh || FAILURES=$((FAILURES+1))
echo ""

# Download Buildifier in oppia-android-tools folder (pre-requisite for buildifier checks)
echo "********************************"
echo "Downloading buildifier"
echo "********************************"
cd ../oppia-android-tools/
bash ../oppia-android/scripts/buildifier_download.sh
cd ../oppia-android/
echo ""

# Run Bazel Build file lint checks (buildifier checks)
bash scripts/buildifier_lint_check.sh || FAILURES=$((FAILURES+1))
echo ""


# SCRIPT CHECKS
# These checks run on Bazel. Ensure Bazel is installed and configured correctly.

# Run regex pattern checks
echo "********************************"
echo "Running regex pattern checks"
echo "********************************"
bazel run //scripts:regex_pattern_validation_check -- $(pwd) || FAILURES=$((FAILURES+1))
echo ""

# Run XML Syntax check validation
echo "********************************"
echo "Running XML Syntax validation checks"
echo "********************************"
bazel run //scripts:xml_syntax_check -- $(pwd) || FAILURES=$((FAILURES+1))
echo ""

# Run TextView Style check
echo "********************************"
echo "Running TextView style validation checks"
echo "********************************"
bazel run //scripts:check_textview_styles -- $(pwd) || FAILURES=$((FAILURES+1))
echo ""

# Run Testfile Presence Check
echo "********************************"
echo "Running Testfile presence checks"
echo "********************************"
bazel run //scripts:test_file_check -- $(pwd) || FAILURES=$((FAILURES+1))
echo ""

# Run Accessibility label Check
echo "********************************"
echo "Running Accessibility label checks"
echo "********************************"
bazel run //scripts:accessibility_label_check -- $(pwd) scripts/assets/accessibility_label_exemptions.pb app/src/main/AndroidManifest.xml || FAILURES=$((FAILURES+1))
echo ""

# Run KDoc Validation Check
echo "********************************"
echo "Running KDoc validation checks"
echo "********************************"
bazel run //scripts:kdoc_validity_check -- $(pwd) scripts/assets/kdoc_validity_exemptions.pb || FAILURES=$((FAILURES+1))
echo ""

# Run String resource validation check
echo "********************************"
echo "Running resource validation checks"
echo "********************************"
bazel run //scripts:string_resource_validation_check -- $(pwd) || FAILURES=$((FAILURES+1))
echo ""

# THIRD PARTY DEPENDENCY CHECKS
# These are checks for third party dependencies

# Maven Repin Check
echo "********************************"
echo "Running Maven repin checks"
echo "********************************"
REPIN=1 bazel run @unpinned_maven//:pin || FAILURES=$((FAILURES+1))
echo ""

# Maven Dependencies Update Check
echo "********************************"
echo "Running maven dependencies update checks"
echo "********************************"
bazel run //scripts:maven_dependencies_list_check -- $(pwd) third_party/maven_install.json scripts/assets/maven_dependencies.pb || FAILURES=$((FAILURES+1))
echo ""

# License Texts Check
echo "********************************"
echo "Running license texts checks"
echo "********************************"
bazel run //scripts:license_texts_check -- $(pwd)/app/src/main/res/values/third_party_dependencies.xml || FAILURES=$((FAILURES+1))
echo ""

# TODO checks.
# Open issue correctness checks.
echo "********************************"
echo "Running TODO correctness checks"
echo "********************************"
bazel run //scripts:todo_open_check -- $(pwd) scripts/assets/todo_open_exemptions.pb || FAILURES=$((FAILURES+1))
echo ""

if [ "$FAILURES" -gt 0 ]; then
    echo "$FAILURES check(s) failed. Please fix the above errors."
    exit 1
else
    echo "All checks passed."
    exit 0
fi
