"""
Tests for libraries providing authentication with Firebase functionality.
"""

load("//:oppia_android_test.bzl", "oppia_android_test")

oppia_android_test(
    name = "AuthenticationControllerTest",
    srcs = ["AuthenticationControllerTest.kt"],
    custom_package = "org.oppia.android.domain.auth",
    test_class = "org.oppia.android.domain.auth.AuthenticationControllerTest",
    test_manifest = "//domain:test_manifest",
    deps = [
        "//:dagger",
        "//domain/src/main/java/org/oppia/android/domain/auth:auth_module",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger/analytics:prod_module",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//testing/src/main/java/org/oppia/android/testing/time:test_module",
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_mockito_mockito-core",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
    ],
)

oppia_android_test(
    name = "AuthenticationModuleTest",
    srcs = ["AuthenticationModuleTest.kt"],
    custom_package = "org.oppia.android.domain.auth",
    test_class = "org.oppia.android.domain.auth.AuthenticationModuleTest",
    test_manifest = "//domain:test_manifest",
    deps = [
        "//:dagger",
        "//domain/src/main/java/org/oppia/android/domain/auth:auth_module",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_mockito_mockito-core",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/logging/firebase:debug_module",
    ],
)

oppia_android_test(
    name = "FirebaseAuthWrapperImplTest",
    srcs = ["FirebaseAuthWrapperImplTest.kt"],
    custom_package = "org.oppia.android.domain.auth",
    test_class = "org.oppia.android.domain.auth.FirebaseAuthWrapperImplTest",
    test_manifest = "//domain:test_manifest",
    deps = [
        "//:dagger",
        "//domain/src/main/java/org/oppia/android/domain/auth:auth_module",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger/analytics:prod_module",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//testing/src/main/java/org/oppia/android/testing/time:test_module",
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_mockito_mockito-core",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
    ],
)
