"""
Testing utilities corresponding asset caching utilities.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "caching_test_module",
    testonly = True,
    srcs = [
        "CachingTestModule.kt",
    ],
    visibility = ["//:oppia_testing_visibility"],
    deps = [
        "//:dagger",
        "//utility/src/main/java/org/oppia/android/util/caching:annotations",
    ],
)

kt_android_library(
    name = "asset_test_no_op_module",
    testonly = True,
    srcs = [
        "AssetTestNoOpModule.kt",
    ],
    visibility = ["//:oppia_testing_visibility"],
    deps = [
        ":test_asset_repository",
        "//:dagger",
    ],
)

kt_android_library(
    name = "fake_asset_repository",
    testonly = True,
    srcs = [
        "FakeAssetRepository.kt",
    ],
    visibility = ["//:oppia_testing_visibility"],
    deps = [
        "//third_party:com_google_protobuf_protobuf-javalite",
        "//utility/src/main/java/org/oppia/android/util/caching:asset_repository",
        "//utility/src/main/java/org/oppia/android/util/caching:impl",
    ],
)

kt_android_library(
    name = "test_asset_repository",
    testonly = True,
    srcs = [
        "TestNoOpAssetRepository.kt",
    ],
    visibility = ["//:oppia_testing_visibility"],
    deps = [
        "//third_party:javax_inject_javax_inject",
        "//utility/src/main/java/org/oppia/android/util/caching:asset_repository",
    ],
)
