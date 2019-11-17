# TODO(BenHenning): Restrict visibility once this is broken into smaller packages.
package(
    default_visibility = ["//visibility:public"],
)

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_android_library")

# TODO(BenHenning): Break this library into smaller pieces.
kt_android_library(
    name = "utility_lib",
    srcs = glob(["java/**"]),
    manifest = "AndroidManifest.xml",
    deps = [
        #"@maven//androidx/appcompat:appcompat"
        "@maven//com/google/dagger",
        "@maven//javax/inject",
        #"@maven//:androidx_appcompat_appcompat",
        #"@maven//:androidx_appcompat_appcompat",
        #"@maven//:androidx_lifecycle_lifecycle_livedata_ktx",
        #"@maven//:com_github_bumptech_glide_glide",
        #"@maven//:com_google_dagger",
    ],
)
