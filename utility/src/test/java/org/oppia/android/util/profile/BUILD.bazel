"""
Tests for profile utilities.
"""

load("//:oppia_android_test.bzl", "oppia_android_test")

oppia_android_test(
    name = "CurrentUserProfileIdIntentDecoratorTest",
    srcs = ["CurrentUserProfileIdIntentDecoratorTest.kt"],
    custom_package = "org.oppia.android.util.profile",
    test_class = "org.oppia.android.util.profile.CurrentUserProfileIdIntentDecoratorTest",
    test_manifest = "//utility:test_manifest",
    deps = [
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_extensions_truth-liteproto-extension",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/profile:current_user_profile_id_intent_decorator",
    ],
)

oppia_android_test(
    name = "DirectoryManagementUtilTest",
    srcs = ["DirectoryManagementUtilTest.kt"],
    custom_package = "org.oppia.android.util.profile",
    test_class = "org.oppia.android.util.profile.DirectoryManagementUtilTest",
    test_manifest = "//utility:test_manifest",
    deps = [
        "//:dagger",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/time:test_module",
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/logging:prod_module",
        "//utility/src/main/java/org/oppia/android/util/profile:directory_management_util",
    ],
)

oppia_android_test(
    name = "ProfileNameValidatorTest",
    srcs = ["ProfileNameValidatorTest.kt"],
    custom_package = "org.oppia.android.util.profile",
    test_class = "org.oppia.android.util.profile.ProfileNameValidatorTest",
    test_manifest = "//utility:test_manifest",
    deps = [
        "//:dagger",
        "//testing/src/main/java/org/oppia/android/testing/junit:oppia_parameterized_test_runner",
        "//testing/src/main/java/org/oppia/android/testing/junit:parameterized_robolectric_test_runner",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//testing/src/main/java/org/oppia/android/testing/time:test_module",
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/locale:prod_module",
        "//utility/src/main/java/org/oppia/android/util/logging:prod_module",
        "//utility/src/main/java/org/oppia/android/util/profile:profile_name_validator",
    ],
)
