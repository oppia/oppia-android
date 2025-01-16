# TODO(#1532): Rename file to 'BUILD' post-Gradle.
"""
This library contains fake objects used for testing as well as tests for these objects.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")
load("//testing:testing_test.bzl", "testing_test")

# Source files that have been migrated to their own package-based libraries. Files added to this
# list will automatically be excluded from the top-level utility library. It's recommended to use
# globs here to ensure new files added to migrated packages don't accidentally get included in the
# top-level module library.
MIGRATED_PROD_FILES = glob([
    "src/main/java/org/oppia/android/testing/math/*.kt",
    "src/main/java/org/oppia/android/testing/mockito/*.kt",
    "src/main/java/org/oppia/android/testing/networking/*.kt",
    "src/test/java/org/oppia/android/testing/platformparameter/*.kt",
    "src/main/java/org/oppia/android/testing/robolectric/*.kt",
    "src/main/java/org/oppia/android/testing/threading/*.kt",
    "src/main/java/org/oppia/android/testing/time/*.kt",
])

# Library for general-purpose testing fakes.
kt_android_library(
    name = "testing",
    testonly = True,
    srcs = glob(
        ["src/main/java/org/oppia/android/testing/**/*.kt"],
        exclude = MIGRATED_PROD_FILES,
    ),
    custom_package = "org.oppia.android.testing",
    manifest = "src/main/AndroidManifest.xml",
    visibility = ["//visibility:public"],
    deps = [
        "//domain",
        "//domain/src/main/java/org/oppia/android/domain/classify:interactions_module",
        "//domain/src/main/java/org/oppia/android/domain/profile:profile_management_controller",
        "//model/src/main/proto:performance_metrics_event_logger_java_proto_lite",
        "//testing/src/main/java/org/oppia/android/testing/data:async_result_subject",
        "//testing/src/main/java/org/oppia/android/testing/data:data_provider_test_monitor",
        "//testing/src/main/java/org/oppia/android/testing/firebase:test_authentication_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_coroutine_dispatchers",
        "//testing/src/main/java/org/oppia/android/testing/time:fake_oppia_clock",
        "//third_party:androidx_core_core-ktx",
        "//third_party:androidx_fragment_fragment",
        "//third_party:androidx_lifecycle_lifecycle-livedata-ktx",
        "//third_party:androidx_test_espresso_espresso-accessibility",
        "//third_party:androidx_test_espresso_espresso-contrib",
        "//third_party:androidx_test_espresso_espresso-core",
        "//third_party:androidx_test_espresso_espresso-intents",
        "//third_party:androidx_test_ext_junit",
        "//third_party:androidx_test_runner",
        "//third_party:com_google_android_material_material",
        "//third_party:com_google_truth_truth",
        "//third_party:com_squareup_retrofit2_converter-moshi",
        "//third_party:com_squareup_retrofit2_retrofit",
        "//third_party:com_squareup_retrofit2_retrofit-mock",
        "//third_party:junit_junit",
        "//third_party:moshi",
        "//third_party:nl_dionsegijn_konfetti",
        "//third_party:org_jetbrains_kotlin_kotlin-reflect",
        "//third_party:org_jetbrains_kotlinx_kotlinx-coroutines-test",
        "//third_party:org_mockito_mockito-core",
        "//third_party:org_robolectric_robolectric",
        "//utility",
        "//utility/src/main/java/org/oppia/android/util/logging/performancemetrics:performance_metrics_assessor",
        "//utility/src/main/java/org/oppia/android/util/logging/performancemetrics:performance_metrics_event_logger",
        "//utility/src/main/java/org/oppia/android/util/parser/image:glide_image_loader_module",
        "//utility/src/main/java/org/oppia/android/util/parser/image:image_loader",
        "//utility/src/main/java/org/oppia/android/util/parser/image:image_parsing_module",
        "//utility/src/main/java/org/oppia/android/util/parser/image:test_glide_image_loader",
    ],
)

# keep sorted
TEST_DEPS = [
    ":assertion_helpers",
    ":testing",
    "//:dagger",
    "//data/src/main/java/org/oppia/android/data/backends/gae:network_config_prod_module",
    "//domain",
    "//domain/src/main/java/org/oppia/android/domain/classify:interactions_module",
    "//domain/src/main/java/org/oppia/android/domain/classify/rules/algebraicexpressioninput:algebraic_expression_input_rule_module",
    "//domain/src/main/java/org/oppia/android/domain/classify/rules/continueinteraction:continue_module",
    "//domain/src/main/java/org/oppia/android/domain/classify/rules/dragAndDropSortInput:drag_and_drop_sort_input_module",
    "//domain/src/main/java/org/oppia/android/domain/classify/rules/fractioninput:fraction_input_module",
    "//domain/src/main/java/org/oppia/android/domain/classify/rules/imageClickInput:image_click_input_module",
    "//domain/src/main/java/org/oppia/android/domain/classify/rules/itemselectioninput:item_selection_input_module",
    "//domain/src/main/java/org/oppia/android/domain/classify/rules/mathequationinput:math_equation_input_rule_module",
    "//domain/src/main/java/org/oppia/android/domain/classify/rules/multiplechoiceinput:multiple_choice_input_module",
    "//domain/src/main/java/org/oppia/android/domain/classify/rules/numberwithunits:number_with_units_rule_module",
    "//domain/src/main/java/org/oppia/android/domain/classify/rules/numericexpressioninput:numeric_expression_input_rule_module",
    "//domain/src/main/java/org/oppia/android/domain/classify/rules/numericinput:numeric_input_rule_module",
    "//domain/src/main/java/org/oppia/android/domain/classify/rules/ratioinput:ratio_input_module",
    "//domain/src/main/java/org/oppia/android/domain/classify/rules/textinput:text_input_rule_module",
    "//domain/src/main/java/org/oppia/android/domain/onboarding/testing:retriever_test_module",
    "//domain/src/main/java/org/oppia/android/domain/oppialogger:prod_module",
    "//domain/src/main/java/org/oppia/android/domain/oppialogger/analytics:prod_module",
    "//domain/src/main/java/org/oppia/android/domain/profile:profile_management_controller",
    "//model/src/main/proto:performance_metrics_event_logger_java_proto_lite",
    "//testing/src/main/java/org/oppia/android/testing/data:async_result_subject",
    "//testing/src/main/java/org/oppia/android/testing/data:data_provider_test_monitor",
    "//testing/src/main/java/org/oppia/android/testing/espresso:text_input_action",
    "//testing/src/main/java/org/oppia/android/testing/network",
    "//testing/src/main/java/org/oppia/android/testing/network:test_module",
    "//testing/src/main/java/org/oppia/android/testing/platformparameter:test_constants",
    "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
    "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
    "//testing/src/main/java/org/oppia/android/testing/time:test_module",
    "//third_party:androidx_lifecycle_lifecycle-livedata-ktx",
    "//third_party:androidx_test_espresso_espresso-accessibility",
    "//third_party:androidx_test_espresso_espresso-contrib",
    "//third_party:androidx_test_espresso_espresso-core",
    "//third_party:androidx_test_espresso_espresso-intents",
    "//third_party:androidx_test_ext_junit",
    "//third_party:androidx_test_runner",
    "//third_party:com_google_truth_truth",
    "//third_party:com_squareup_retrofit2_converter-moshi",
    "//third_party:com_squareup_retrofit2_retrofit",
    "//third_party:com_squareup_retrofit2_retrofit-mock",
    "//third_party:junit_junit",
    "//third_party:moshi",
    "//third_party:org_jetbrains_kotlin_kotlin-reflect",
    "//third_party:org_jetbrains_kotlinx_kotlinx-coroutines-test",
    "//third_party:org_mockito_mockito-core",
    "//third_party:robolectric_android-all",
    "//utility/src/main/java/org/oppia/android/util/caching:asset_prod_module",
    "//utility/src/main/java/org/oppia/android/util/caching/testing:caching_test_module",
    "//utility/src/main/java/org/oppia/android/util/locale:prod_module",
    "//utility/src/main/java/org/oppia/android/util/logging:prod_module",
    "//utility/src/main/java/org/oppia/android/util/logging/performancemetrics:performance_metrics_assessor",
    "//utility/src/main/java/org/oppia/android/util/logging/performancemetrics:performance_metrics_event_logger",
    "//utility/src/main/java/org/oppia/android/util/networking:debug_module",
    "//utility/src/main/java/org/oppia/android/util/system:prod_module",
]

# Qualified file paths for test classes that have been migrated over to their own packages &
# shouldn't be defined as module-level tests.
# keep sorted
MIGRATED_TESTS = glob([
    "src/test/java/org/oppia/android/testing/networking/*.kt",
    "src/test/java/org/oppia/android/testing/threading/*.kt",
])

[testing_test(
    name = test_file_path,
    filtered_tests = MIGRATED_TESTS,
    deps = TEST_DEPS,
) for test_file_path in glob(["src/test/java/org/oppia/android/testing/**/*Test.kt"])]

kt_android_library(
    name = "assertion_helpers",
    testonly = True,
    srcs = ["src/main/java/org/oppia/android/testing/AssertionHelpers.kt"],
    visibility = ["//visibility:public"],
    deps = ["//third_party:junit_junit"],
)

filegroup(
    name = "test_manifest",
    srcs = ["src/test/AndroidManifest.xml"],
    visibility = ["//:oppia_testing_visibility"],
)
