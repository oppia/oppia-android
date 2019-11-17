# TODO(BenHenning): Restrict visibility once this is broken into smaller packages.
package(
    default_visibility = ["//visibility:public"],
)

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_android_library")

# TODO(BenHenning): Break this library into smaller pieces.
kt_android_library(
    name = "domain_lib",
    srcs = glob(["java/**"]),
    manifest = "AndroidManifest.xml",
)
