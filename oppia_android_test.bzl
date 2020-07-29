load("@rules_jvm_external//:defs.bzl", "artifact")
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_android_library")

def oppia_android_test(name, srcs, test_manifest, custom_package, resource_files,
                       test_class, src_library_name):
  """
  This macro exists as a way to set up a test in Oppia Android to be run with Bazel.
  This macro creates a library for an individual test that is fed as a dependency into an
  android_local_test() rule which configures the test to be run with Roboletric and Bazel.
  """

  kt_android_library(
    name = name + "_lib",
    custom_package = custom_package,
    srcs = srcs,
    resource_files = resource_files,
    manifest = test_manifest,
    deps = [
        ":" + src_library_name,
    ],
  )

  native.android_local_test(
    name = name,
    custom_package = custom_package,
    test_class = test_class,
    manifest = test_manifest,
    deps = [
       ":" + name + "_lib",
       ":dagger",
       "@robolectric//bazel:android-all",
       artifact("org.jetbrains.kotlin:kotlin-reflect"),
       artifact("org.robolectric:robolectric"),
    ],
  )
