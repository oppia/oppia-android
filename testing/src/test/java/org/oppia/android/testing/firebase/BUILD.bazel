"""
Tests for test-only Firebase utilities.
"""

load("//:oppia_android_test.bzl", "oppia_android_test")

oppia_android_test(
    name = "FakeFirebaseAuthWrapperImplTest",
    srcs = ["FakeFirebaseAuthWrapperImplTest.kt"],
    custom_package = "org.oppia.android.testing.firebase",
    test_class = "org.oppia.android.testing.firebase.FakeFirebaseAuthWrapperImplTest",
    test_manifest = "//testing:test_manifest",
    deps = [
        "//:dagger",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger/analytics:prod_module",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//testing/src/main/java/org/oppia/android/testing/time:test_module",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
    ],
)

oppia_android_test(
    name = "TestAuthenticationModuleTest",
    srcs = ["TestAuthenticationModuleTest.kt"],
    custom_package = "org.oppia.android.testing.firebase",
    test_class = "org.oppia.android.testing.firebase.TestAuthenticationModuleTest",
    test_manifest = "//testing:test_manifest",
    deps = [
        "//:dagger",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger/analytics:prod_module",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//testing/src/main/java/org/oppia/android/testing/time:test_module",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
    ],
)
