"""
Generic utilities for managing languages & locales.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "android_locale_profile",
    srcs = [
        "AndroidLocaleProfile.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        ":oppia_locale",
        "//third_party:javax_inject_javax_inject",
    ],
)

kt_android_library(
    name = "oppia_bidi_formatter",
    srcs = [
        "OppiaBidiFormatter.kt",
    ],
    visibility = [
        "//domain/src/main/java/org/oppia/android/domain/locale:__pkg__",
        "//utility:utility_testing_visibility",
        "//utility/src/main/java/org/oppia/android/util/locale/testing:__pkg__",
    ],
)

kt_android_library(
    name = "oppia_locale",
    srcs = [
        "OppiaLocale.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        ":oppia_locale_context_extensions",
        "//model/src/main/proto:languages_java_proto_lite",
        "//third_party:androidx_annotation_annotation",
    ],
)

kt_android_library(
    name = "impl",
    srcs = [
        "DisplayLocaleImpl.kt",
        "MachineLocaleImpl.kt",
        "OppiaBidiFormatterImpl.kt",
    ],
    visibility = [
        "//domain/src/main/java/org/oppia/android/domain/locale:__pkg__",
        "//utility:utility_testing_visibility",
        "//utility/src/main/java/org/oppia/android/util/locale/testing:__pkg__",
    ],
    deps = [
        ":android_locale_factory",
        ":oppia_bidi_formatter",
        ":oppia_locale",
        "//third_party:androidx_core_core",
        "//third_party:javax_inject_javax_inject",
        "//utility/src/main/java/org/oppia/android/util/data:data_providers",
        "//utility/src/main/java/org/oppia/android/util/system:oppia_clock",
    ],
)

kt_android_library(
    name = "android_locale_factory",
    srcs = [
        "AndroidLocaleFactory.kt",
    ],
    visibility = [
        "//utility:utility_testing_visibility",
    ],
    deps = [
        ":android_locale_profile",
        "//:dagger",
        "//third_party:javax_inject_javax_inject",
        "//third_party:org_jetbrains_kotlinx_kotlinx-coroutines-core",
        "//utility/src/main/java/org/oppia/android/util/threading:annotations",
    ],
)

kt_android_library(
    name = "oppia_locale_context_extensions",
    srcs = [
        "OppiaLocaleContextExtensions.kt",
    ],
    deps = [
        "//model/src/main/proto:languages_java_proto_lite",
    ],
)

kt_android_library(
    name = "prod_module",
    srcs = [
        "LocaleProdModule.kt",
    ],
    visibility = ["//:oppia_prod_module_visibility"],
    deps = [
        ":impl",
        ":oppia_locale",
        "//:dagger",
    ],
)
