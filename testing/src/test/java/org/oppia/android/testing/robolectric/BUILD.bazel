"""
Tests for Robolectric-specific utilities and configurations.
"""

load("//:oppia_android_test.bzl", "oppia_android_test")

oppia_android_test(
    name = "OppiaShadowActivityManagerTest",
    srcs = ["OppiaShadowActivityManagerTest.kt"],
    custom_package = "org.oppia.android.testing.robolectric",
    test_class = "org.oppia.android.testing.robolectric.OppiaShadowActivityManagerTest",
    test_manifest = "//testing:test_manifest",
    deps = [
        "//:dagger",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:oppia_shadow_activity_manager",
        "//third_party:androidx_core_core",
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
    ],
)

oppia_android_test(
    name = "OppiaShadowTrafficStatsTest",
    srcs = ["OppiaShadowTrafficStatsTest.kt"],
    custom_package = "org.oppia.android.testing.robolectric",
    test_class = "org.oppia.android.testing.robolectric.OppiaShadowTrafficStatsTest",
    test_manifest = "//testing:test_manifest",
    deps = [
        "//:dagger",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:oppia_shadow_traffic_stats",
        "//third_party:androidx_core_core",
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
    ],
)

oppia_android_test(
    name = "ShadowBidiFormatterTest",
    srcs = ["ShadowBidiFormatterTest.kt"],
    custom_package = "org.oppia.android.testing.robolectric",
    test_class = "org.oppia.android.testing.robolectric.ShadowBidiFormatterTest",
    test_manifest = "//testing:test_manifest",
    deps = [
        "//:dagger",
        "//testing/src/main/java/org/oppia/android/testing/robolectric:shadow_bidi_formatter",
        "//third_party:androidx_core_core",
        "//third_party:androidx_test_ext_junit",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
        "//third_party:org_robolectric_robolectric",
        "//third_party:robolectric_android-all",
    ],
)
