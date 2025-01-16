# TODO(#1532): Rename file to 'BUILD' post-Gradle.
"""
Library for providing top-level HTTP networking support for Oppia's App Engine backend.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "network_interceptors",
    srcs = [
        "JsonPrefixNetworkInterceptor.kt",
        "NetworkApiKey.kt",
        "NetworkLoggingInterceptor.kt",
        "RemoteAuthNetworkInterceptor.kt",
    ],
    visibility = [
        "//:oppia_api_visibility",
        "//:oppia_testing_visibility",
    ],
    deps = [
        ":constants",
        ":network_config_annotations",
        "//:dagger",
        "//model/src/main/proto:arguments_java_proto_lite",
        "//model/src/main/proto:event_logger_java_proto_lite",
        "//third_party:com_squareup_okhttp3_okhttp",
        "//third_party:javax_inject_javax_inject",
        "//third_party:org_jetbrains_kotlinx_kotlinx-coroutines-core",
        "//utility/src/main/java/org/oppia/android/util/extensions:context_extensions",
        "//utility/src/main/java/org/oppia/android/util/logging:prod_module",
        "//utility/src/main/java/org/oppia/android/util/threading:annotations",
    ],
)

kt_android_library(
    name = "network_config_prod_module",
    srcs = [
        "NetworkConfigProdModule.kt",
    ],
    visibility = ["//:oppia_prod_module_visibility"],
    deps = [
        ":network_config_annotations",
        "//:dagger",
    ],
)

kt_android_library(
    name = "constants",
    srcs = [
        "Constants.kt",
    ],
)

kt_android_library(
    name = "network_config_annotations",
    srcs = [
        "BaseUrl.kt",
        "XssiPrefix.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//third_party:javax_inject_javax_inject",
    ],
)

kt_android_library(
    name = "oppia_retrofit",
    srcs = [
        "OppiaRetrofit.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//third_party:javax_inject_javax_inject",
    ],
)

kt_android_library(
    name = "prod_module",
    srcs = [
        "NetworkModule.kt",
    ],
    visibility = ["//:oppia_prod_module_visibility"],
    deps = [
        ":network_config_annotations",
        ":network_interceptors",
        ":oppia_retrofit",
        "//:dagger",
        "//data/src/main/java/org/oppia/android/data/backends/gae/api",
        "//third_party:com_google_guava_guava",
        "//third_party:com_squareup_retrofit2_converter-moshi",
    ],
)
