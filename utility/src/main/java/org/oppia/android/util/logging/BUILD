"""
General purpose utilities for logging.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "annotations",
    srcs = [
        "LoggingAnnotations.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//third_party:javax_inject_javax_inject",
    ],
)

kt_android_library(
    name = "console_logger",
    srcs = [
        "ConsoleLogger.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        ":annotations",
        ":log_level",
        "//:dagger",
        "//model/src/main/proto:event_logger_java_proto_lite",
        "//third_party:javax_inject_javax_inject",
        "//third_party:org_jetbrains_kotlinx_kotlinx-coroutines-core",
        "//utility/src/main/java/org/oppia/android/util/locale:oppia_locale",
        "//utility/src/main/java/org/oppia/android/util/threading:annotations",
    ],
)

kt_android_library(
    name = "console_logger_injector",
    srcs = [
        "ConsoleLoggerInjector.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        ":console_logger",
    ],
)

kt_android_library(
    name = "console_logger_injector_provider",
    srcs = [
        "ConsoleLoggerInjectorProvider.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        ":console_logger_injector",
    ],
)

kt_android_library(
    name = "current_app_screen_name_intent_decorator",
    srcs = [
        "CurrentAppScreenNameIntentDecorator.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//model/src/main/proto:screens_java_proto_lite",
        "//utility/src/main/java/org/oppia/android/util/extensions:bundle_extensions",
    ],
)

kt_android_library(
    name = "event_bundle_creator",
    srcs = [
        "EventBundleCreator.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        ":event_type_to_human_readable_name_converter",
        "//model/src/main/proto:event_logger_java_proto_lite",
        "//model/src/main/proto:performance_metrics_event_logger_java_proto_lite",
        "//third_party:javax_inject_javax_inject",
        "//utility",
        "//utility/src/main/java/org/oppia/android/util/extensions:context_extensions",
    ],
)

kt_android_library(
    name = "event_logger",
    srcs = [
        "AnalyticsEventLogger.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//model/src/main/proto:event_logger_java_proto_lite",
    ],
)

kt_android_library(
    name = "exception_logger",
    srcs = [
        "ExceptionLogger.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
)

kt_android_library(
    name = "log_level",
    srcs = [
        "LogLevel.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
)

kt_android_library(
    name = "event_type_to_human_readable_name_converter",
    srcs = [
        "EventTypeToHumanReadableNameConverter.kt",
    ],
    deps = [
        "//model/src/main/proto:event_logger_java_proto_lite",
        "//third_party:javax_inject_javax_inject",
    ],
)

kt_android_library(
    name = "prod_module",
    srcs = [
        "LoggerModule.kt",
        "SyncStatusModule.kt",
    ],
    visibility = ["//:oppia_prod_module_visibility"],
    deps = [
        ":annotations",
        ":log_level",
        ":sync_status_manager_impl",
        "//:dagger",
        "//third_party:javax_inject_javax_inject",
    ],
)

kt_android_library(
    name = "log_uploader",
    srcs = [
        "LogUploader.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//third_party:androidx_work_work-runtime",
        "//third_party:androidx_work_work-runtime-ktx",
    ],
)

kt_android_library(
    name = "metric_log_scheduler",
    srcs = [
        "MetricLogScheduler.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//third_party:androidx_work_work-runtime",
        "//third_party:androidx_work_work-runtime-ktx",
    ],
)

kt_android_library(
    name = "sync_status_manager",
    srcs = [
        "SyncStatusManager.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//model/src/main/proto:event_logger_java_proto_lite",
        "//utility/src/main/java/org/oppia/android/util/data:data_provider",
    ],
)

kt_android_library(
    name = "sync_status_manager_impl",
    srcs = [
        "SyncStatusManagerImpl.kt",
    ],
    visibility = ["//testing/src/main/java/org/oppia/android/testing/logging:__pkg__"],
    deps = [
        ":sync_status_manager",
        "//model/src/main/proto:event_logger_java_proto_lite",
        "//utility/src/main/java/org/oppia/android/util/data:async_data_subscription_manager",
        "//utility/src/main/java/org/oppia/android/util/data:data_providers",
        "//utility/src/main/java/org/oppia/android/util/networking:network_connection_util",
    ],
)
