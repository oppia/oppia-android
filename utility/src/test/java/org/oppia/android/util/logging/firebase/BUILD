"""
Tests for logging utility implementations backed by Firebase.
"""

load("//:oppia_android_test.bzl", "oppia_android_test")

oppia_android_test(
    name = "LogReportingModuleTest",
    srcs = ["LogReportingModuleTest.kt"],
    custom_package = "org.oppia.android.util.logging.firebase",
    test_class = "org.oppia.android.util.logging.firebase.LogReportingModuleTest",
    test_manifest = "//utility:test_manifest",
    deps = [
        "//:dagger",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//testing/src/main/java/org/oppia/android/testing/time:test_module",
        "//third_party:androidx_test_ext_junit",
        "//third_party:androidx_test_ext_truth",
        "//third_party:com_google_truth_truth",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/locale:prod_module",
        "//utility/src/main/java/org/oppia/android/util/logging/firebase:prod_module",
        "//utility/src/main/java/org/oppia/android/util/networking:debug_module",
    ],
)

oppia_android_test(
    name = "DebugFirestoreEventLoggerImplTest",
    srcs = ["DebugFirestoreEventLoggerImplTest.kt"],
    custom_package = "org.oppia.android.util.logging.firebase",
    test_class = "org.oppia.android.util.logging.firebase.DebugFirestoreEventLoggerImplTest",
    test_manifest = "//utility:test_manifest",
    deps = [
        "//:dagger",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//testing/src/main/java/org/oppia/android/testing/time:test_module",
        "//third_party:androidx_test_ext_junit",
        "//third_party:androidx_test_ext_truth",
        "//third_party:com_google_truth_truth",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/locale/testing:test_module",
        "//utility/src/main/java/org/oppia/android/util/logging/firebase:debug_module",
        "//utility/src/main/java/org/oppia/android/util/logging/firebase:prod_module",
        "//utility/src/main/java/org/oppia/android/util/networking:debug_module",
    ],
)
