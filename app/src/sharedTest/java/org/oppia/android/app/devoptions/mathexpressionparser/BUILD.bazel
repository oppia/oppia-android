"""
Tests for the math expression parser developer options UI flows.
"""

load("//app:app_test.bzl", "app_test")
load("//app:test_with_resources.bzl", "test_with_resources")

app_test(
    name = "MathExpressionParserActivityTest",
    processed_src = test_with_resources("MathExpressionParserActivityTest.kt"),
    test_class = "org.oppia.android.app.devoptions.mathexpressionparser.MathExpressionParserActivityTest",
    deps = [
        "//:dagger",
        "//app",
        "//app/src/main/java/org/oppia/android/app/application:application_component",
        "//app/src/main/java/org/oppia/android/app/application:application_injector",
        "//app/src/main/java/org/oppia/android/app/application:application_injector_provider",
        "//app/src/main/java/org/oppia/android/app/application:common_application_modules",
        "//app/src/main/java/org/oppia/android/app/application/testing:testing_build_flavor_module",
        "//app/src/main/java/org/oppia/android/app/translation/testing:test_module",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/espresso:edit_text_input_action",
        "//testing/src/main/java/org/oppia/android/testing/junit:initialize_default_locale_rule",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//testing/src/main/java/org/oppia/android/testing/time:test_module",
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/accessibility:test_module",
        "//utility/src/main/java/org/oppia/android/util/caching/testing:caching_test_module",
        "//utility/src/main/java/org/oppia/android/util/logging/firebase:debug_module",
        "//utility/src/main/java/org/oppia/android/util/networking:debug_module",
    ],
)

app_test(
    name = "MathExpressionParserFragmentTest",
    processed_src = test_with_resources("MathExpressionParserFragmentTest.kt"),
    test_class = "org.oppia.android.app.devoptions.mathexpressionparser.MathExpressionParserFragmentTest",
    deps = [
        "//:dagger",
        "//app",
        "//app/src/main/java/org/oppia/android/app/application:application_component",
        "//app/src/main/java/org/oppia/android/app/application:application_injector",
        "//app/src/main/java/org/oppia/android/app/application:application_injector_provider",
        "//app/src/main/java/org/oppia/android/app/application:common_application_modules",
        "//app/src/main/java/org/oppia/android/app/application/testing:testing_build_flavor_module",
        "//app/src/main/java/org/oppia/android/app/testing/activity:test_activity",
        "//app/src/main/java/org/oppia/android/app/translation/testing:test_module",
        "//testing",
        "//testing/src/main/java/org/oppia/android/testing/espresso:edit_text_input_action",
        "//testing/src/main/java/org/oppia/android/testing/junit:initialize_default_locale_rule",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:test_module",
        "//testing/src/main/java/org/oppia/android/testing/threading:test_module",
        "//testing/src/main/java/org/oppia/android/testing/time:test_module",
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/accessibility:test_module",
        "//utility/src/main/java/org/oppia/android/util/caching/testing:caching_test_module",
        "//utility/src/main/java/org/oppia/android/util/logging/firebase:debug_module",
        "//utility/src/main/java/org/oppia/android/util/networking:debug_module",
    ],
)
