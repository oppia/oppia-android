load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_android_library")

package(
    default_visibility = ["//app:__pkg__"],
)

kt_android_library(
    name = "app",
    srcs = glob(["java/**/*.kt"]),
    custom_package = "org.oppia.app",
    enable_data_binding = True,
    deps = [
        "@maven//:com_google_android_material_material",
        "@maven//:androidx_constraintlayout_constraintlayout",
        "@maven//:com_chaos_view_pinview",

        "@maven//:com_android_databinding_baseLibrary",
        "@maven//:com_android_databinding_library",
        "@maven//:com_android_databinding_adapters",
        "@maven//:com_android_databinding_viewbinding",

        #"@maven//:androidx_appcompat_appcompat",
        #"@maven//:androidx_lifecycle_lifecycle_livedata_ktx",
        #"@maven//:com_github_bumptech_glide_glide",
        #"@maven//:com_google_dagger_dagger",
        #"@maven//:org_jetbrains_kotlin_kotlin_stdlib_jdk7",
    ],
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
)
