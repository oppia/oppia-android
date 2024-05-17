"""
Tests for survey domain components.
"""

load("//:oppia_android_test.bzl", "oppia_android_test")

oppia_android_test(
    name = "SurveyGatingControllerTest",
    srcs = ["SurveyGatingControllerTest.kt"],
    custom_package = "org.oppia.android.domain.survey",
    test_class = "org.oppia.android.domain.survey.SurveyGatingControllerTest",
    test_manifest = "//domain:test_manifest",
    deps = [
        "//:dagger",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger:prod_module",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger/analytics:prod_module",
        "//domain/src/main/java/org/oppia/android/domain/survey:gating_controller",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/platformparameter:test_module",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//testing/src/main/java/org/oppia/android/testing/time:test_module",
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/caching:asset_prod_module",
        "//utility/src/main/java/org/oppia/android/util/locale:prod_module",
        "//utility/src/main/java/org/oppia/android/util/logging:prod_module",
        "//utility/src/main/java/org/oppia/android/util/networking:debug_module",
    ],
)

oppia_android_test(
    name = "SurveyControllerTest",
    srcs = ["SurveyControllerTest.kt"],
    custom_package = "org.oppia.android.domain.survey",
    test_class = "org.oppia.android.domain.survey.SurveyControllerTest",
    test_manifest = "//domain:test_manifest",
    deps = [
        "//:dagger",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger:prod_module",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger/analytics:prod_module",
        "//domain/src/main/java/org/oppia/android/domain/survey:survey_controller",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//testing/src/main/java/org/oppia/android/testing/time:test_module",
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/caching:asset_prod_module",
        "//utility/src/main/java/org/oppia/android/util/locale:prod_module",
        "//utility/src/main/java/org/oppia/android/util/logging:prod_module",
        "//utility/src/main/java/org/oppia/android/util/networking:debug_module",
    ],
)

oppia_android_test(
    name = "SurveyProgressControllerTest",
    srcs = ["SurveyProgressControllerTest.kt"],
    custom_package = "org.oppia.android.domain.survey",
    test_class = "org.oppia.android.domain.survey.SurveyProgressControllerTest",
    test_manifest = "//domain:test_manifest",
    deps = [
        "//:dagger",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger:prod_module",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger/analytics:prod_module",
        "//domain/src/main/java/org/oppia/android/domain/survey:survey_controller",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/logging:event_log_subject",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//testing/src/main/java/org/oppia/android/testing/time:test_module",
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/caching:asset_prod_module",
        "//utility/src/main/java/org/oppia/android/util/locale:prod_module",
        "//utility/src/main/java/org/oppia/android/util/logging:prod_module",
        "//utility/src/main/java/org/oppia/android/util/networking:debug_module",
    ],
)
