load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_android_library")

package(
    default_visibility = ["//utility:__pkg__"],
)

kt_android_library(
    name = "util",
    srcs = glob(["java/**/*.kt"]),
    custom_package = "org.oppia.util",
    deps = [
        "@maven//:androidx_appcompat_appcompat",
        "@maven//:androidx_lifecycle_lifecycle_livedata_ktx",
        "@maven//:com_github_bumptech_glide_glide",
        "@maven//:com_google_dagger_dagger",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib_jdk7",
    ],
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
)
