"""
Kotlin-based scripts to help developers or perform continuous integration tasks.
"""

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary", "kt_jvm_library")
load("@rules_java//java:defs.bzl", "java_binary")
load(
    "//scripts:script_assets.bzl",
    "generate_accessibility_label_assets_list_from_text_protos",
    "generate_kdoc_validity_assets_list_from_text_protos",
    "generate_maven_assets_list_from_text_protos",
    "generate_regex_assets_list_from_text_protos",
    "generate_test_file_assets_list_from_text_protos",
    "generate_todo_assets_list_from_text_protos",
)

# Visibility for libraries that should be accessible to script tests.
package_group(
    name = "oppia_script_test_visibility",
    packages = [
        "//scripts/src/javatests/...",
    ],
)

# Visibility for libraries that have binaries defined at this package & tests.
package_group(
    name = "oppia_script_binary_visibility",
    includes = [
        ":oppia_script_test_visibility",
    ],
    packages = [
        "//scripts",
    ],
)

# Visibility for libraries that should be accessible to other script packages & tests.
package_group(
    name = "oppia_script_library_visibility",
    includes = [
        ":oppia_script_test_visibility",
    ],
    packages = ["//scripts/src/java/..."],
)

kt_jvm_binary(
    name = "compute_aab_differences",
    testonly = True,
    data = ["@androidsdk//:aapt2_binary"],
    main_class = "org.oppia.android.scripts.apkstats.ComputeAabDifferencesKt",
    runtime_deps = [
        "//scripts/src/java/org/oppia/android/scripts/apkstats:compute_aab_differences_lib",
    ],
)

kt_jvm_binary(
    name = "compute_affected_tests",
    testonly = True,
    main_class = "org.oppia.android.scripts.ci.ComputeAffectedTestsKt",
    runtime_deps = ["//scripts/src/java/org/oppia/android/scripts/ci:compute_affected_tests_lib"],
)

kt_jvm_binary(
    name = "retrieve_affected_tests",
    testonly = True,
    main_class = "org.oppia.android.scripts.ci.RetrieveAffectedTestsKt",
    runtime_deps = ["//scripts/src/java/org/oppia/android/scripts/ci:retrieve_affected_tests_lib"],
)

# TODO(#3428): Refactor textproto assets to subpackage level.
REGEX_PATTERN_CHECK_ASSETS = generate_regex_assets_list_from_text_protos(
    name = "regex_asset_files",
    file_content_validation_file_names = ["file_content_validation_checks"],
    filepath_pattern_validation_file_names = ["filename_pattern_validation_checks"],
)

MAVEN_ASSETS = generate_maven_assets_list_from_text_protos(
    name = "maven_assets",
    maven_dependency_filenames = ["maven_dependencies"],
)

kt_jvm_library(
    name = "regex_check_assets",
    testonly = True,
    data = REGEX_PATTERN_CHECK_ASSETS,
    visibility = [":oppia_script_test_visibility"],
)

kt_jvm_binary(
    name = "regex_pattern_validation_check",
    testonly = True,
    data = REGEX_PATTERN_CHECK_ASSETS,
    main_class = "org.oppia.android.scripts.regex.RegexPatternValidationCheckKt",
    runtime_deps = [
        "//scripts/src/java/org/oppia/android/scripts/regex:regex_pattern_validation_check_lib",
    ],
)

kt_jvm_binary(
    name = "xml_syntax_check",
    testonly = True,
    main_class = "org.oppia.android.scripts.xml.XmlSyntaxCheckKt",
    runtime_deps = ["//scripts/src/java/org/oppia/android/scripts/xml:xml_syntax_check_lib"],
)

kt_jvm_binary(
    name = "string_language_translation_check",
    testonly = True,
    main_class = "org.oppia.android.scripts.xml.StringLanguageTranslationCheckKt",
    runtime_deps = ["//scripts/src/java/org/oppia/android/scripts/xml:string_language_translation_check_lib"],
)

kt_jvm_binary(
    name = "string_resource_validation_check",
    testonly = True,
    main_class = "org.oppia.android.scripts.xml.StringResourceValidationCheckKt",
    runtime_deps = ["//scripts/src/java/org/oppia/android/scripts/xml:string_resource_validation_check_lib"],
)

TEST_FILE_EXEMPTION_ASSETS = generate_test_file_assets_list_from_text_protos(
    name = "test_file_exemption_assets",
    test_file_exemptions_name = ["test_file_exemptions"],
)

kt_jvm_library(
    name = "test_file_check_assets",
    testonly = True,
    data = TEST_FILE_EXEMPTION_ASSETS,
    visibility = [":oppia_script_test_visibility"],
)

kt_jvm_binary(
    name = "test_file_check",
    testonly = True,
    data = TEST_FILE_EXEMPTION_ASSETS,
    main_class = "org.oppia.android.scripts.testfile.TestFileCheckKt",
    runtime_deps = ["//scripts/src/java/org/oppia/android/scripts/testfile:test_file_check_lib"],
)

kt_jvm_library(
    name = "maven_assets_lib",
    testonly = True,
    data = MAVEN_ASSETS,
    visibility = [":oppia_script_test_visibility"],
)

kt_jvm_binary(
    name = "generate_maven_dependencies_list",
    testonly = True,
    data = MAVEN_ASSETS,
    main_class = "org.oppia.android.scripts.maven.GenerateMavenDependenciesListKt",
    runtime_deps = ["//scripts/src/java/org/oppia/android/scripts/maven:generate_maven_dependencies_list_lib"],
)

kt_jvm_binary(
    name = "maven_dependencies_list_check",
    testonly = True,
    data = MAVEN_ASSETS,
    main_class = "org.oppia.android.scripts.license.MavenDependenciesListCheckKt",
    runtime_deps = ["//scripts/src/java/org/oppia/android/scripts/license:maven_dependencies_list_check_lib"],
)

kt_jvm_binary(
    name = "retrieve_license_texts",
    testonly = True,
    data = MAVEN_ASSETS,
    main_class = "org.oppia.android.scripts.maven.RetrieveLicenseTextsKt",
    runtime_deps = ["//scripts/src/java/org/oppia/android/scripts/maven:retrieve_license_texts_lib"],
)

kt_jvm_binary(
    name = "license_texts_check",
    testonly = True,
    main_class = "org.oppia.android.scripts.license.LicenseTextsCheckKt",
    runtime_deps = ["//scripts/src/java/org/oppia/android/scripts/license:license_texts_check_lib"],
)

ACCESSIBILITY_LABEL_EXEMPTION_ASSETS = generate_accessibility_label_assets_list_from_text_protos(
    name = "accessibility_label_exemption_assets",
    accessibility_label_exemptions_name = ["accessibility_label_exemptions"],
)

kt_jvm_binary(
    name = "accessibility_label_check",
    testonly = True,
    data = ACCESSIBILITY_LABEL_EXEMPTION_ASSETS,
    main_class = "org.oppia.android.scripts.label.AccessibilityLabelCheckKt",
    runtime_deps = [
        "//scripts/src/java/org/oppia/android/scripts/label:accessibility_label_check_lib",
    ],
)

KDOC_VALIDITY_EXEMPTION_ASSETS = generate_kdoc_validity_assets_list_from_text_protos(
    name = "kdoc_validity_exemption_assets",
    kdoc_validity_exemptions_name = ["kdoc_validity_exemptions"],
)

kt_jvm_binary(
    name = "kdoc_validity_check",
    testonly = True,
    data = KDOC_VALIDITY_EXEMPTION_ASSETS,
    main_class = "org.oppia.android.scripts.docs.KdocValidityCheckKt",
    runtime_deps = [
        "//scripts/src/java/org/oppia/android/scripts/docs:kdoc_validity_check_lib",
    ],
)

TODO_OPEN_EXEMPTION_ASSETS = generate_todo_assets_list_from_text_protos(
    name = "todo_exemption_assets",
    todo_exemptions_name = ["todo_open_exemptions"],
)

kt_jvm_binary(
    name = "todo_open_check",
    testonly = True,
    data = TODO_OPEN_EXEMPTION_ASSETS,
    main_class = "org.oppia.android.scripts.todo.TodoOpenCheckKt",
    runtime_deps = [
        "//scripts/src/java/org/oppia/android/scripts/todo:todo_open_check_lib",
    ],
)

kt_jvm_binary(
    name = "todo_issue_resolved_check",
    testonly = True,
    main_class = "org.oppia.android.scripts.todo.TodoIssueResolvedCheckKt",
    runtime_deps = [
        "//scripts/src/java/org/oppia/android/scripts/todo:todo_issue_resolved_check_lib",
    ],
)

kt_jvm_binary(
    name = "todo_issue_comment_check",
    testonly = True,
    main_class = "org.oppia.android.scripts.todo.TodoIssueCommentCheckKt",
    runtime_deps = [
        "//scripts/src/java/org/oppia/android/scripts/todo:todo_issue_comment_check_lib",
    ],
)

kt_jvm_binary(
    name = "wiki_table_of_contents_check",
    testonly = True,
    main_class = "org.oppia.android.scripts.wiki.WikiTableOfContentsCheckKt",
    runtime_deps = [
        "//scripts/src/java/org/oppia/android/scripts/wiki:wiki_table_of_contents_check_lib",
    ],
)

kt_jvm_binary(
    name = "run_coverage",
    testonly = True,
    data = TEST_FILE_EXEMPTION_ASSETS,
    main_class = "org.oppia.android.scripts.coverage.RunCoverageKt",
    runtime_deps = [
        "//scripts/src/java/org/oppia/android/scripts/coverage:run_coverage_lib",
    ],
)

kt_jvm_binary(
    name = "coverage_reporter",
    testonly = True,
    data = TEST_FILE_EXEMPTION_ASSETS,
    main_class = "org.oppia.android.scripts.coverage.reporter.CoverageReporterKt",
    runtime_deps = [
        "//scripts/src/java/org/oppia/android/scripts/coverage/reporter:coverage_reporter_lib",
    ],
)

kt_jvm_binary(
    name = "retrieve_changed_files",
    testonly = True,
    data = TEST_FILE_EXEMPTION_ASSETS,
    main_class = "org.oppia.android.scripts.ci.RetrieveChangedFilesKt",
    runtime_deps = ["//scripts/src/java/org/oppia/android/scripts/ci:retrieve_changed_files_lib"],
)

kt_jvm_binary(
    name = "compute_changed_files",
    testonly = True,
    main_class = "org.oppia.android.scripts.ci.ComputeChangedFilesKt",
    runtime_deps = ["//scripts/src/java/org/oppia/android/scripts/ci:compute_changed_files_lib"],
)

# Note that this is intentionally not test-only since it's used by the app build pipeline. Also,
# this apparently needs to be a java_binary to set up runfiles correctly when executed within a
# Starlark rule as a tool.
java_binary(
    name = "transform_android_manifest",
    main_class = "org.oppia.android.scripts.build.TransformAndroidManifestKt",
    visibility = [
        "//:oppia_binary_visibility",
    ],
    runtime_deps = [
        "//scripts/src/java/org/oppia/android/scripts/build:transform_android_manifest_lib",
    ],
)

java_binary(
    name = "filter_per_language_resources",
    main_class = "org.oppia.android.scripts.build.FilterPerLanguageResourcesKt",
    visibility = [
        "//:oppia_binary_visibility",
    ],
    runtime_deps = [
        "//scripts/src/java/org/oppia/android/scripts/build:filter_per_language_resources_lib",
    ],
)

java_binary(
    name = "decode_user_study_event_string",
    testonly = True,
    main_class = "org.oppia.android.scripts.telemetry.DecodeUserStudyEventStringKt",
    runtime_deps = ["//scripts/src/java/org/oppia/android/scripts/telemetry:decode_user_study_event_string_lib"],
)
