load("@rules_jvm_external//:defs.bzl", "artifact")
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_android_library")

def oppia_android_test(name, srcs, test_manifest, custom_package, test_class, src_library_name,
                       data=None, resource_files=None):

    kt_android_library(
        name = name + "_lib",
        custom_package = custom_package,
        srcs = srcs,
        resource_files = resource_files,
        manifest = test_manifest,
        deps = [
            ":" + src_library_name,
        ],
        data = data,
    )

    native.android_local_test(
       name = name + "_test_lib",
       custom_package = custom_package,
       test_class = test_class,
       manifest = test_manifest,
       deps = [
           ":" + src_library_name,
           ":" + name + "_lib",
           "@robolectric//bazel:android-all",
           "@maven//:org_robolectric_robolectric",
           artifact("org.jetbrains.kotlin:kotlin-reflect"),
       ],
       data = data
     )
