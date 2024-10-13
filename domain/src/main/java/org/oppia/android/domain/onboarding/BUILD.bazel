"""
Utilities for onboarding domain components.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "state_controller",
    srcs = [
        "AppStartupStateController.kt",
        "DeprecationController.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        ":exploration_meta_data_retriever",
        "//data/src/main/java/org/oppia/android/data/persistence:cache_store",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger:oppia_logger",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger/analytics:controller",
        "//model/src/main/proto:deprecation_java_proto_lite",
        "//model/src/main/proto:onboarding_java_proto_lite",
        "//third_party:javax_inject_javax_inject",
        "//utility",
        "//utility/src/main/java/org/oppia/android/util/data:data_provider",
        "//utility/src/main/java/org/oppia/android/util/data:data_providers",
        "//utility/src/main/java/org/oppia/android/util/extensions:bundle_extensions",
        "//utility/src/main/java/org/oppia/android/util/extensions:context_extensions",
    ],
)

kt_android_library(
    name = "retriever_prod_module",
    srcs = [
        "ExpirationMetaDataRetrieverModule.kt",
    ],
    visibility = ["//:oppia_prod_module_visibility"],
    deps = [
        ":exploration_meta_data_retriever",
        ":impl",
        "//:dagger",
    ],
)

kt_android_library(
    name = "exploration_meta_data_retriever",
    srcs = [
        "ExpirationMetaDataRetriever.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
)

kt_android_library(
    name = "impl",
    srcs = [
        "ExpirationMetaDataRetrieverImpl.kt",
    ],
    deps = [
        ":exploration_meta_data_retriever",
        "//third_party:javax_inject_javax_inject",
    ],
)

kt_android_library(
    name = "deprecation_controller",
    srcs = [
        "DeprecationController.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        ":exploration_meta_data_retriever",
        "//data/src/main/java/org/oppia/android/data/persistence:cache_store",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger:oppia_logger",
        "//model/src/main/proto:deprecation_java_proto_lite",
        "//model/src/main/proto:onboarding_java_proto_lite",
        "//third_party:javax_inject_javax_inject",
        "//utility",
        "//utility/src/main/java/org/oppia/android/util/data:data_provider",
        "//utility/src/main/java/org/oppia/android/util/data:data_providers",
        "//utility/src/main/java/org/oppia/android/util/extensions:bundle_extensions",
        "//utility/src/main/java/org/oppia/android/util/extensions:context_extensions",
    ],
)
