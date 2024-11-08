"""
Tests for logging utilities.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")
load("//:oppia_android_test.bzl", "oppia_android_test")

kt_android_library(
    name = "sync_status_manager_test_base",
    testonly = True,
    srcs = ["SyncStatusManagerTestBase.kt"],
    visibility = [
        "//testing/src/test/java/org/oppia/android/testing/logging:__pkg__",
    ],
    deps = [
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/data:data_provider_test_monitor",
        "//testing/src/main/java/org/oppia/android/testing/networking:network_connection_test_util",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//utility/src/main/java/org/oppia/android/util/logging:sync_status_manager",
    ],
)

oppia_android_test(
    name = "CurrentAppScreenNameIntentDecoratorTest",
    srcs = ["CurrentAppScreenNameIntentDecoratorTest.kt"],
    custom_package = "org.oppia.android.util.logging",
    test_class = "org.oppia.android.util.logging.CurrentAppScreenNameIntentDecoratorTest",
    test_manifest = "//utility:test_manifest",
    deps = [
        "//:dagger",
        "//third_party:androidx_test_ext_junit",
        "//third_party:androidx_test_ext_truth",
        "//third_party:com_google_truth_truth",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/logging:current_app_screen_name_intent_decorator",
    ],
)

oppia_android_test(
    name = "EventBundleCreatorTest",
    srcs = ["EventBundleCreatorTest.kt"],
    custom_package = "org.oppia.android.util.logging",
    test_class = "org.oppia.android.util.logging.EventBundleCreatorTest",
    test_manifest = "//utility:test_manifest",
    deps = [
        "//:dagger",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/junit:oppia_parameterized_test_runner",
        "//testing/src/main/java/org/oppia/android/testing/junit:parameterized_robolectric_test_runner",
        "//third_party:androidx_test_ext_junit",
        "//third_party:androidx_test_ext_truth",
        "//third_party:com_google_truth_truth",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/logging:event_bundle_creator",
    ],
)

oppia_android_test(
    name = "SyncStatusManagerImplTest",
    srcs = ["SyncStatusManagerImplTest.kt"],
    custom_package = "org.oppia.android.util.logging",
    test_class = "org.oppia.android.util.logging.SyncStatusManagerImplTest",
    test_manifest = "//utility:test_manifest",
    deps = [
        ":sync_status_manager_test_base",
        "//:dagger",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/data:data_provider_test_monitor",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//testing/src/main/java/org/oppia/android/testing/time:test_module",
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_truth",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/locale:prod_module",
        "//utility/src/main/java/org/oppia/android/util/logging:prod_module",
        "//utility/src/main/java/org/oppia/android/util/networking:debug_module",
    ],
)

oppia_android_test(
    name = "ConsoleLoggerTest",
    srcs = ["ConsoleLoggerTest.kt"],
    custom_package = "org.oppia.android.util.logging",
    test_class = "org.oppia.android.util.logging.ConsoleLoggerTest",
    test_manifest = "//utility:test_manifest",
    deps = [
        ":sync_status_manager_test_base",
        "//:dagger",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/data:data_provider_test_monitor",
        "//testing/src/main/java/org/oppia/android/testing/junit:oppia_parameterized_test_runner",
        "//testing/src/main/java/org/oppia/android/testing/junit:parameterized_robolectric_test_runner",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//testing/src/main/java/org/oppia/android/testing/time:test_module",
        "//third_party:androidx_test_ext_junit",
        "//third_party:androidx_test_ext_truth",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/locale:prod_module",
        "//utility/src/main/java/org/oppia/android/util/locale/testing:test_module",
        "//utility/src/main/java/org/oppia/android/util/logging:event_bundle_creator",
        "//utility/src/main/java/org/oppia/android/util/logging:prod_module",
        "//utility/src/main/java/org/oppia/android/util/networking:debug_module",
    ],
)

oppia_android_test(
    name = "EventTypeToHumanReadableNameConverterTest",
    srcs = ["EventTypeToHumanReadableNameConverterTest.kt"],
    custom_package = "org.oppia.android.util.logging",
    test_class = "org.oppia.android.util.logging.EventTypeToHumanReadableNameConverterTest",
    test_manifest = "//utility:test_manifest",
    deps = [
        "//:dagger",
        "//testing",
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_truth",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
    ],
)
