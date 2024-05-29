"""
Utilities corresponding to caching.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "asset_repository",
    srcs = [
        "AssetRepository.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//third_party:com_google_protobuf_protobuf-javalite",
    ],
)

kt_android_library(
    name = "annotations",
    srcs = [
        "LoadImagesFromAssets.kt",
        "LoadLessonProtosFromAssets.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//third_party:javax_inject_javax_inject",
    ],
)

kt_android_library(
    name = "impl",
    srcs = [
        "AssetRepositoryImpl.kt",
    ],
    visibility = [
        "//utility/src/main/java/org/oppia/android/util/caching/testing:__pkg__",
    ],
    deps = [
        ":asset_repository",
        "//third_party:com_google_protobuf_protobuf-javalite",
        "//utility/src/main/java/org/oppia/android/util/logging:console_logger",
    ],
)

kt_android_library(
    name = "caching_prod_module",
    srcs = [
        "CachingModule.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        ":annotations",
        ":asset_repository",
        "//:dagger",
    ],
)

kt_android_library(
    name = "asset_prod_module",
    srcs = [
        "AssetModule.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        ":impl",
        "//:dagger",
    ],
)
