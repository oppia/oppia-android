"""
Library for providing log scheduling functionality to the Oppia android app.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "performance_metrics_log_scheduler",
    srcs = [
        "PerformanceMetricsLogScheduler.kt",
    ],
    deps = [
        ":metric_log_scheduling_worker",
        "//third_party:androidx_work_work-runtime-ktx",
        "//utility/src/main/java/org/oppia/android/util/logging:metric_log_scheduler",
    ],
)

kt_android_library(
    name = "metric_log_scheduling_worker",
    srcs = [
        "MetricLogSchedulingWorker.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//domain/src/main/java/org/oppia/android/domain/oppialogger/analytics:application_lifecycle_observer",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger/analytics:performance_metrics_logger",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger/exceptions:controller",
        "//domain/src/main/java/org/oppia/android/domain/util:extensions",
        "//model/src/main/proto:screens_java_proto_lite",
        "//third_party:androidx_work_work-runtime-ktx",
        "//third_party:org_jetbrains_kotlinx_kotlinx-coroutines-guava",
        "//utility/src/main/java/org/oppia/android/util/logging:console_logger",
        "//utility/src/main/java/org/oppia/android/util/threading:annotations",
    ],
)

kt_android_library(
    name = "metric_log_scheduling_worker_factory",
    srcs = [
        "MetricLogSchedulingWorkerFactory.kt",
    ],
    visibility = ["//domain:__pkg__"],
    deps = [
        ":metric_log_scheduling_worker",
        "//third_party:androidx_work_work-runtime-ktx",
    ],
)

kt_android_library(
    name = "metric_log_scheduler_module",
    srcs = [
        "MetricLogSchedulerModule.kt",
    ],
    visibility = ["//:oppia_prod_module_visibility"],
    deps = [
        ":performance_metrics_log_scheduler",
        "//:dagger",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger:startup_listener",
        "//utility/src/main/java/org/oppia/android/util/logging:metric_log_scheduler",
    ],
)
