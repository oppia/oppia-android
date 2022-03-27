"""
Tests for the components used to render LaTeX math expressions.
"""

load("//:oppia_android_test.bzl", "oppia_android_test")

oppia_android_test(
    name = "MathModelTest",
    srcs = ["MathModelTest.kt"],
    custom_package = "org.oppia.android.util.parser.math",
    test_class = "org.oppia.android.util.parser.math.MathModelTest",
    test_manifest = "//utility:test_manifest",
    deps = [
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:robolectric_android-all",
        "//utility/src/main/java/org/oppia/android/util/parser/math:math_latex_model",
    ],
)
