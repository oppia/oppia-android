"""
Tests for domain components pertaining to onboarding the user to the app.
"""

load("//:oppia_android_test.bzl", "oppia_android_test")

oppia_android_test(
    name = "AppStartupStateControllerTest",
    srcs = ["AppStartupStateControllerTest.kt"],
    custom_package = "org.oppia.android.domain.onboarding",
    test_class = "org.oppia.android.domain.onboarding.AppStartupStateControllerTest",
    test_manifest = "//domain:test_manifest",
    deps = [
        "//:dagger",
        "//domain",
        "//domain/src/main/java/org/oppia/android/domain/onboarding:retriever_prod_module",
        "//domain/src/main/java/org/oppia/android/domain/onboarding:state_controller",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger:prod_module",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger/analytics:prod_module",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/data:data_provider_test_monitor",
        "//testing/src/main/java/org/oppia/android/testing/junit:oppia_parameterized_test_runner",
        "//testing/src/main/java/org/oppia/android/testing/junit:parameterized_robolectric_test_runner",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_mockito_mockito-core",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/caching:asset_prod_module",
        "//utility/src/main/java/org/oppia/android/util/locale:prod_module",
        "//utility/src/main/java/org/oppia/android/util/logging:prod_module",
        "//utility/src/main/java/org/oppia/android/util/networking:debug_module",
        "//utility/src/main/java/org/oppia/android/util/system:prod_module",
    ],
)

oppia_android_test(
    name = "DeprecationControllerTest",
    srcs = ["DeprecationControllerTest.kt"],
    custom_package = "org.oppia.android.domain.onboarding",
    test_class = "org.oppia.android.domain.onboarding.DeprecationControllerTest",
    test_manifest = "//domain:test_manifest",
    deps = [
        "//:dagger",
        "//domain",
        "//domain/src/main/java/org/oppia/android/domain/onboarding:deprecation_controller",
        "//domain/src/main/java/org/oppia/android/domain/onboarding:retriever_prod_module",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger:prod_module",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger/analytics:prod_module",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/data:data_provider_test_monitor",
        "//testing/src/main/java/org/oppia/android/testing/junit:oppia_parameterized_test_runner",
        "//testing/src/main/java/org/oppia/android/testing/junit:parameterized_robolectric_test_runner",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_mockito_mockito-core",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/locale:prod_module",
        "//utility/src/main/java/org/oppia/android/util/logging:prod_module",
        "//utility/src/main/java/org/oppia/android/util/networking:debug_module",
        "//utility/src/main/java/org/oppia/android/util/system:prod_module",
    ],
)
