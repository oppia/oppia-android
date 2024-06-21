"""
UI utilities for for managing languages & locales.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

# Resource shim needed so that AppLanguageResourceHandler can build in both Gradle & Bazel.
genrule(
    name = "update_AppLanguageResourceHandler",
    srcs = ["AppLanguageResourceHandler.kt"],
    outs = ["AppLanguageResourceHandler_updated.kt"],
    cmd = """
    cat $(SRCS) |
    sed 's/import org.oppia.android.R/import org.oppia.android.app.R/g' > $(OUTS)
    """,
)

kt_android_library(
    name = "app_language_locale_handler",
    srcs = [
        "AppLanguageLocaleHandler.kt",
    ],
    visibility = [
        "//app:app_testing_visibility",
        "//testing/src/test/java/org/oppia/android/testing/junit:__pkg__",
    ],
    deps = [
        "//domain/src/main/java/org/oppia/android/domain/locale:locale_controller",
    ],
)

kt_android_library(
    name = "activity_language_locale_handler",
    srcs = [
        "ActivityLanguageLocaleHandler.kt",
    ],
    visibility = [
        "//app:app_testing_visibility",
        "//testing/src/test/java/org/oppia/android/testing/junit:__pkg__",
    ],
    deps = [
        ":app_language_locale_handler",
        "//:dagger",
        "//app/src/main/java/org/oppia/android/app/activity:activity_scope",
        "//domain/src/main/java/org/oppia/android/domain/locale:locale_controller",
    ],
)

kt_android_library(
    name = "app_language_resource_handler",
    srcs = [
        "AppLanguageResourceHandler_updated.kt",
    ],
    visibility = ["//app:app_visibility"],
    deps = [
        ":app_language_locale_handler",
        "//app:resources",
        "//model/src/main/proto:profile_java_proto_lite",
        "//third_party:androidx_appcompat_appcompat",
        "//utility/src/main/java/org/oppia/android/util/locale:oppia_locale",
    ],
)

kt_android_library(
    name = "app_language_watcher_mixin",
    srcs = [
        "AppLanguageWatcherMixin.kt",
    ],
    visibility = ["//app:app_visibility"],
    deps = [
        ":activity_language_locale_handler",
        ":activity_recreator",
        ":app_language_locale_handler",
        "//domain/src/main/java/org/oppia/android/domain/profile:profile_management_controller",
        "//domain/src/main/java/org/oppia/android/domain/translation:translation_controller",
    ],
)

kt_android_library(
    name = "activity_recreator",
    srcs = [
        "ActivityRecreator.kt",
    ],
    visibility = [
        "//app/src/main/java/org/oppia/android/app/translation/testing:__pkg__",
    ],
    deps = [
        "//third_party:androidx_appcompat_appcompat",
    ],
)

kt_android_library(
    name = "impl",
    srcs = [
        "ActivityRecreatorImpl.kt",
    ],
    deps = [
        ":activity_recreator",
        "//third_party:javax_inject_javax_inject",
    ],
)

kt_android_library(
    name = "prod_module",
    srcs = [
        "ActivityRecreatorProdModule.kt",
    ],
    visibility = ["//:oppia_prod_module_visibility"],
    deps = [
        ":activity_recreator",
        ":impl",
        "//:dagger",
    ],
)

kt_android_library(
    name = "app_language_activity_injector",
    srcs = [
        "AppLanguageActivityInjector.kt",
    ],
    visibility = [
        "//app/src/main/java/org/oppia/android/app/activity:__pkg__",
    ],
    deps = [
        ":activity_language_locale_handler",
        ":app_language_resource_handler",
        ":app_language_watcher_mixin",
    ],
)

kt_android_library(
    name = "app_language_activity_injector_provider",
    srcs = [
        "AppLanguageActivityInjectorProvider.kt",
    ],
    visibility = [
        "//app:__pkg__",
        "//app/src/main/java/org/oppia/android/app/activity:__pkg__",
    ],
    deps = [
        ":activity_language_locale_handler",
        ":app_language_activity_injector",
    ],
)

kt_android_library(
    name = "app_language_application_injector",
    srcs = [
        "AppLanguageApplicationInjector.kt",
    ],
    visibility = [
        "//app/src/main/java/org/oppia/android/app/activity:__pkg__",
        "//app/src/main/java/org/oppia/android/app/application:__pkg__",
    ],
    deps = [
        ":app_language_locale_handler",
    ],
)

kt_android_library(
    name = "app_language_application_injector_provider",
    srcs = [
        "AppLanguageApplicationInjectorProvider.kt",
    ],
    visibility = [
        "//app/src/main/java/org/oppia/android/app/activity:__pkg__",
        "//app/src/main/java/org/oppia/android/app/application:__pkg__",
    ],
    deps = [
        ":app_language_application_injector",
    ],
)
